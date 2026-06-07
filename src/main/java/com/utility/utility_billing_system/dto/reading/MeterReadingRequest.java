package com.utility.utility_billing_system.dto.reading;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for recording a monthly meter reading.
 * <p>
 * Current reading must exceed previous reading; one reading per meter per month.
 */
@Getter
@Setter
public class MeterReadingRequest {

    @NotNull
    private Long meterId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal previousReading;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal currentReading;

    @NotNull
    private LocalDate readingDate;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer readingMonth;

    @NotNull
    @Min(2000)
    private Integer readingYear;
}
