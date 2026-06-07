package com.utility.utility_billing_system.dto.meter;

import com.utility.utility_billing_system.enums.MeterType;
import com.utility.utility_billing_system.enums.StatusType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request body for creating or updating a utility meter.
 */
@Getter
@Setter
public class MeterRequest {

    @NotBlank
    @Size(max = 50)
    private String meterNumber;

    @NotNull
    private MeterType meterType;

    @NotNull
    private LocalDate installationDate;

    private StatusType status;

    @NotNull
    private Long customerId;
}
