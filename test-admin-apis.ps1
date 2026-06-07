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

$loginBody = @{ email = "admin@utility.com"; password = "Admin@123" } | ConvertTo-Json
$login = Invoke-RestMethod -Uri "$base/api/auth/login" -Method POST -ContentType "application/json" -Body $loginBody
$token = $login.data.token
$headers = @{ Authorization = "Bearer $token" }
$results += [PSCustomObject]@{ Test = "POST /api/auth/login"; Status = "PASS"; Detail = "userId=$($login.data.userId) roles=$($login.data.roles -join ',')" }

Test-Api "GET /api/users" { (Invoke-RestMethod -Uri "$base/api/users" -Headers $headers).message }

Test-Api "POST /api/users (operator)" {
    $body = @{ fullName = "Test Operator"; email = "operator@utility.com"; phoneNumber = "0788111111"; password = "operator123"; status = "ACTIVE"; roles = @("ROLE_OPERATOR") } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$base/api/users" -Method POST -Headers $headers -ContentType "application/json" -Body $body
    $script:operatorId = $r.data.id
    "id=$($r.data.id) email=$($r.data.email)"
}

Test-Api "GET /api/users/{id}" { (Invoke-RestMethod -Uri "$base/api/users/$operatorId" -Headers $headers).data.fullName }
Test-Api "PUT /api/users/{id}/status" { (Invoke-RestMethod -Uri "$base/api/users/$operatorId/status?status=ACTIVE" -Method PUT -Headers $headers).message }

Test-Api "GET /api/customers" { (Invoke-RestMethod -Uri "$base/api/customers" -Headers $headers).message }

Test-Api "POST /api/customers" {
    $body = @{ fullName = "John Doe"; nationalId = "NAT-TEST-001"; email = "john@test.com"; phoneNumber = "0788222222"; address = "Kigali"; status = "ACTIVE" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$base/api/customers" -Method POST -Headers $headers -ContentType "application/json" -Body $body
    $script:customerId = $r.data.id
    "id=$($r.data.id)"
}

Test-Api "GET /api/customers/{id}" { (Invoke-RestMethod -Uri "$base/api/customers/$customerId" -Headers $headers).data.fullName }
Test-Api "PUT /api/customers/{id}/status" { (Invoke-RestMethod -Uri "$base/api/customers/$customerId/status?status=ACTIVE" -Method PUT -Headers $headers).message }

Test-Api "GET /api/meters" { (Invoke-RestMethod -Uri "$base/api/meters" -Headers $headers).message }

Test-Api "POST /api/meters" {
    $body = @{ meterNumber = "MTR-TEST-001"; meterType = "WATER"; installationDate = "2026-01-15"; status = "ACTIVE"; customerId = $customerId } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$base/api/meters" -Method POST -Headers $headers -ContentType "application/json" -Body $body
    $script:meterId = $r.data.id
    "id=$($r.data.id)"
}

Test-Api "GET /api/meters/customer/{id}" { "count=$((Invoke-RestMethod -Uri "$base/api/meters/customer/$customerId" -Headers $headers).data.Count)" }

Test-Api "POST /api/tariffs" {
    $body = @{ meterType = "WATER"; flatRate = 2.50; fixedServiceCharge = 500; vatPercentage = 18; latePaymentPenaltyPercentage = 5; effectiveFrom = "2026-01-01"; status = "ACTIVE" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$base/api/tariffs" -Method POST -Headers $headers -ContentType "application/json" -Body $body
    $script:tariffId = $r.data.id
    "id=$($r.data.id) version=$($r.data.version)"
}

Test-Api "GET /api/tariffs" { "count=$((Invoke-RestMethod -Uri "$base/api/tariffs" -Headers $headers).data.Count)" }
Test-Api "GET /api/tariffs/meter-type/WATER" { "count=$((Invoke-RestMethod -Uri "$base/api/tariffs/meter-type/WATER" -Headers $headers).data.Count)" }

Test-Api "POST /api/meter-readings" {
    $body = @{ meterId = $meterId; previousReading = 100; currentReading = 150; readingDate = "2026-06-01"; readingMonth = 6; readingYear = 2026 } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$base/api/meter-readings" -Method POST -Headers $headers -ContentType "application/json" -Body $body
    $script:readingId = $r.data.id
    "id=$($r.data.id) consumption=$($r.data.consumption)"
}

Test-Api "GET /api/meter-readings" { "count=$((Invoke-RestMethod -Uri "$base/api/meter-readings" -Headers $headers).data.Count)" }

Test-Api "GET /api/bills" {
    $r = Invoke-RestMethod -Uri "$base/api/bills" -Headers $headers
    if ($r.data.Count -gt 0) { $script:billId = $r.data[0].id; "auto-bill id=$billId total=$($r.data[0].totalAmount)" }
    else { "no bills yet" }
}

if (-not $billId) {
    Test-Api "POST /api/bills/generate" {
        $body = @{ meterReadingId = $readingId; billingMonth = 6; billingYear = 2026 } | ConvertTo-Json
        $r = Invoke-RestMethod -Uri "$base/api/bills/generate" -Method POST -Headers $headers -ContentType "application/json" -Body $body
        $script:billId = $r.data.id
        "id=$($r.data.id) total=$($r.data.totalAmount) status=$($r.data.status)"
    }
}

Test-Api "GET /api/bills/{id}" { $b = (Invoke-RestMethod -Uri "$base/api/bills/$billId" -Headers $headers).data; "status=$($b.status) outstanding=$($b.outstandingBalance)" }
Test-Api "GET /api/bills/customer/{id}" { "count=$((Invoke-RestMethod -Uri "$base/api/bills/customer/$customerId" -Headers $headers).data.Count)" }
Test-Api "POST /api/bills/{id}/penalty" { (Invoke-RestMethod -Uri "$base/api/bills/$billId/penalty" -Method POST -Headers $headers).message }

Test-Api "POST /api/payments (partial)" {
    $body = @{ billId = $billId; amount = 200; reference = "PAY-TEST-1" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$base/api/payments" -Method POST -Headers $headers -ContentType "application/json" -Body $body
    "type=$($r.data.paymentType) remaining=$($r.data.remainingBalance)"
}

Test-Api "GET /api/payments/bill/{id}" { "count=$((Invoke-RestMethod -Uri "$base/api/payments/bill/$billId" -Headers $headers).data.Count)" }

Test-Api "GET /api/notifications/user/1" { "count=$((Invoke-RestMethod -Uri "$base/api/notifications/user/1" -Headers $headers).data.Count)" }

Test-Api "PUT /api/users/{id} (update operator)" {
    $body = @{ fullName = "Test Operator Updated"; email = "operator@utility.com"; phoneNumber = "0788999999"; status = "ACTIVE"; roles = @("ROLE_OPERATOR") } | ConvertTo-Json
    (Invoke-RestMethod -Uri "$base/api/users/$operatorId" -Method PUT -Headers $headers -ContentType "application/json" -Body $body).message
}

$passed = ($results | Where-Object { $_.Status -eq "PASS" }).Count
$failed = ($results | Where-Object { $_.Status -ne "PASS" }).Count
Write-Host "`n=== SUMMARY: $passed passed, $failed failed ===`n"
$results | Format-Table -AutoSize -Wrap
