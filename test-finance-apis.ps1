$base = "http://localhost:8080"
$results = @()

function Test-Api($name, $scriptBlock) {
    try {
        $r = & $scriptBlock
        $script:results += [PSCustomObject]@{ Test = $name; Status = "PASS"; Detail = $r }
    } catch {
        $status = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { "ERR" }
        $body = try { $_.ErrorDetails.Message } catch { $_.Exception.Message }
        $script:results += [PSCustomObject]@{ Test = $name; Status = "FAIL ($status)"; Detail = $body }
    }
}

# Admin login to ensure finance test user exists with known password
$adminLogin = @{ email = "admin@utility.com"; password = "Admin@123" } | ConvertTo-Json
$admin = Invoke-RestMethod -Uri "$base/api/auth/login" -Method POST -ContentType "application/json" -Body $adminLogin
$adminHeaders = @{ Authorization = "Bearer $($admin.data.token)" }

$financeEmail = "finance@utility.com"
$financePassword = "finance123"

try {
    $body = @{
        fullName = "Finance Tester"
        email = $financeEmail
        phoneNumber = "0788444444"
        password = $financePassword
        status = "ACTIVE"
        roles = @("ROLE_FINANCE")
    } | ConvertTo-Json
    Invoke-RestMethod -Uri "$base/api/users" -Method POST -Headers $adminHeaders -ContentType "application/json" -Body $body | Out-Null
    $results += [PSCustomObject]@{ Test = "Setup: create finance user"; Status = "PASS"; Detail = $financeEmail }
} catch {
    $body = @{
        fullName = "Finance Tester"
        email = $financeEmail
        phoneNumber = "0788444444"
        password = $financePassword
        status = "ACTIVE"
        roles = @("ROLE_FINANCE")
    } | ConvertTo-Json
    $users = (Invoke-RestMethod -Uri "$base/api/users" -Headers $adminHeaders).data
    $financeUser = $users | Where-Object { $_.email -eq $financeEmail }
    if ($financeUser) {
        Invoke-RestMethod -Uri "$base/api/users/$($financeUser.id)" -Method PUT -Headers $adminHeaders -ContentType "application/json" -Body $body | Out-Null
        $results += [PSCustomObject]@{ Test = "Setup: reset finance user password"; Status = "PASS"; Detail = $financeEmail }
    } else {
        $financeEmail = "eric@utility.com"
        $results += [PSCustomObject]@{ Test = "Setup: using existing finance user"; Status = "WARN"; Detail = "Using $financeEmail - password may fail" }
    }
}

# Finance login
Test-Api "POST /api/auth/login (finance)" {
    $body = @{ email = $financeEmail; password = $financePassword } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$base/api/auth/login" -Method POST -ContentType "application/json" -Body $body
    $script:token = $r.data.token
    $script:headers = @{ Authorization = "Bearer $token" }
    "userId=$($r.data.userId) roles=$($r.data.roles -join ',')"
}

# Finance should NOT access user management
Test-Api "GET /api/users (should fail 403)" {
    try {
        Invoke-RestMethod -Uri "$base/api/users" -Headers $headers
        throw "Expected 403 but succeeded"
    } catch {
        if ([int]$_.Exception.Response.StatusCode -eq 403) { "Correctly denied" }
        else { throw }
    }
}

# Tariffs
Test-Api "POST /api/tariffs (ELECTRICITY tier-based)" {
    $body = @{
        meterType = "ELECTRICITY"
        fixedServiceCharge = 1000
        vatPercentage = 18
        latePaymentPenaltyPercentage = 5
        effectiveFrom = "2026-01-01"
        status = "ACTIVE"
        tiers = @(
            @{ minConsumption = 0; maxConsumption = 100; rate = 3.00 }
            @{ minConsumption = 101; maxConsumption = 300; rate = 5.00 }
            @{ minConsumption = 301; maxConsumption = $null; rate = 7.00 }
        )
    } | ConvertTo-Json -Depth 5
    $r = Invoke-RestMethod -Uri "$base/api/tariffs" -Method POST -Headers $headers -ContentType "application/json" -Body $body
    $script:elecTariffId = $r.data.id
    "id=$($r.data.id) version=$($r.data.version) tiers=$($r.data.tiers.Count)"
}

Test-Api "POST /api/tariffs (WATER v2 - new version)" {
    $body = @{
        meterType = "WATER"
        flatRate = 3.00
        fixedServiceCharge = 600
        vatPercentage = 18
        latePaymentPenaltyPercentage = 5
        effectiveFrom = "2026-07-01"
        status = "ACTIVE"
    } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$base/api/tariffs" -Method POST -Headers $headers -ContentType "application/json" -Body $body
    "id=$($r.data.id) version=$($r.data.version)"
}

Test-Api "GET /api/tariffs" { "count=$((Invoke-RestMethod -Uri "$base/api/tariffs" -Headers $headers).data.Count)" }
Test-Api "GET /api/tariffs/{id}" { (Invoke-RestMethod -Uri "$base/api/tariffs/$elecTariffId" -Headers $headers).data.meterType }
Test-Api "GET /api/tariffs/meter-type/ELECTRICITY" { "count=$((Invoke-RestMethod -Uri "$base/api/tariffs/meter-type/ELECTRICITY" -Headers $headers).data.Count)" }

# Read operational data
Test-Api "GET /api/customers" { "count=$((Invoke-RestMethod -Uri "$base/api/customers" -Headers $headers).data.Count)" }
Test-Api "GET /api/meters" { "count=$((Invoke-RestMethod -Uri "$base/api/meters" -Headers $headers).data.Count)" }
Test-Api "GET /api/meter-readings" { "count=$((Invoke-RestMethod -Uri "$base/api/meter-readings" -Headers $headers).data.Count)" }

# Bills
Test-Api "GET /api/bills" {
    $r = Invoke-RestMethod -Uri "$base/api/bills" -Headers $headers
    if ($r.data.Count -gt 0) {
        $script:billId = $r.data[0].id
        $script:billOutstanding = $r.data[0].outstandingBalance
        "count=$($r.data.Count) firstBillId=$billId outstanding=$billOutstanding status=$($r.data[0].status)"
    } else { "no bills" }
}

if ($billId) {
    Test-Api "GET /api/bills/{id}" {
        $b = (Invoke-RestMethod -Uri "$base/api/bills/$billId" -Headers $headers).data
        "total=$($b.totalAmount) paid=$($b.paidAmount) outstanding=$($b.outstandingBalance) status=$($b.status)"
    }

    Test-Api "GET /api/bills/customer/{id}" {
        $custId = (Invoke-RestMethod -Uri "$base/api/bills/$billId" -Headers $headers).data.customerId
        "customerId=$custId count=$((Invoke-RestMethod -Uri "$base/api/bills/customer/$custId" -Headers $headers).data.Count)"
    }

    Test-Api "PUT /api/bills/{id}/status?OVERDUE" {
        (Invoke-RestMethod -Uri "$base/api/bills/$billId/status?status=OVERDUE" -Method PUT -Headers $headers).data.status
    }

    Test-Api "POST /api/bills/{id}/penalty" {
        $before = (Invoke-RestMethod -Uri "$base/api/bills/$billId" -Headers $headers).data.totalAmount
        (Invoke-RestMethod -Uri "$base/api/bills/$billId/penalty" -Method POST -Headers $headers).message
        $after = (Invoke-RestMethod -Uri "$base/api/bills/$billId" -Headers $headers).data.totalAmount
        "total before=$before after=$after"
    }

    # Payments - get fresh outstanding
    $bill = (Invoke-RestMethod -Uri "$base/api/bills/$billId" -Headers $headers).data
    $remaining = [decimal]$bill.outstandingBalance

    if ($remaining -gt 100) {
        Test-Api "POST /api/payments (partial)" {
            $amt = [math]::Round($remaining / 2, 2)
            $body = @{ billId = $billId; amount = $amt; reference = "FIN-PARTIAL-001" } | ConvertTo-Json
            $r = Invoke-RestMethod -Uri "$base/api/payments" -Method POST -Headers $headers -ContentType "application/json" -Body $body
            "type=$($r.data.paymentType) remaining=$($r.data.remainingBalance)"
        }
    }

    $bill = (Invoke-RestMethod -Uri "$base/api/bills/$billId" -Headers $headers).data
    $remaining = [decimal]$bill.outstandingBalance

    if ($remaining -gt 0) {
        Test-Api "POST /api/payments (full)" {
            $body = @{ billId = $billId; amount = $remaining; reference = "FIN-FULL-001" } | ConvertTo-Json
            $r = Invoke-RestMethod -Uri "$base/api/payments" -Method POST -Headers $headers -ContentType "application/json" -Body $body
            "type=$($r.data.paymentType) remaining=$($r.data.remainingBalance)"
        }
    }

    Test-Api "GET /api/payments/bill/{id}" {
        "count=$((Invoke-RestMethod -Uri "$base/api/payments/bill/$billId" -Headers $headers).data.Count)"
    }

    Test-Api "GET /api/bills/{id} after payments" {
        $b = (Invoke-RestMethod -Uri "$base/api/bills/$billId" -Headers $headers).data
        "status=$($b.status) outstanding=$($b.outstandingBalance)"
    }
}

# Generate bill if we have a reading without bill
Test-Api "POST /api/bills/generate (if possible)" {
    $readings = (Invoke-RestMethod -Uri "$base/api/meter-readings" -Headers $headers).data
    $bills = (Invoke-RestMethod -Uri "$base/api/bills" -Headers $headers).data
    $reading = $readings | Select-Object -Last 1
    $exists = $bills | Where-Object { $_.meterId -eq $reading.meterId -and $_.billingMonth -eq $reading.readingMonth }
    if ($exists) { "Bill already exists for reading period - skipped" }
    else {
        $body = @{ meterReadingId = $reading.id; billingMonth = $reading.readingMonth; billingYear = $reading.readingYear } | ConvertTo-Json
        try {
            $r = Invoke-RestMethod -Uri "$base/api/bills/generate" -Method POST -Headers $headers -ContentType "application/json" -Body $body
            "generated id=$($r.data.id) total=$($r.data.totalAmount)"
        } catch {
            if ($_.ErrorDetails.Message -match "already exists|Inactive") { $_.ErrorDetails.Message }
            else { throw }
        }
    }
}

# Finance cannot delete customers
Test-Api "DELETE /api/customers/1 (should fail 403)" {
    try {
        Invoke-RestMethod -Uri "$base/api/customers/1" -Method DELETE -Headers $headers
        throw "Expected 403"
    } catch {
        if ([int]$_.Exception.Response.StatusCode -eq 403) { "Correctly denied" }
        else { throw }
    }
}

$passed = ($results | Where-Object { $_.Status -eq "PASS" }).Count
$failed = ($results | Where-Object { $_.Status -like "FAIL*" }).Count
$warn = ($results | Where-Object { $_.Status -eq "WARN" }).Count
Write-Host "`n=== FINANCE TEST SUMMARY: $passed passed, $failed failed, $warn warnings ===`n"
$results | Format-Table -AutoSize -Wrap
