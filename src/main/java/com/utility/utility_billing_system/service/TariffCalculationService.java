package com.utility.utility_billing_system.service;

import com.utility.utility_billing_system.entity.Tariff;
import com.utility.utility_billing_system.entity.TariffTier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Stateless service for tariff-based charge calculations.
 * <p>
 * Applies flat-rate or tiered pricing to consumption, then computes VAT and
 * late-payment penalties. Used by BillService during bill generation and penalty application.
 */
@Service
public class TariffCalculationService {

    /** Calculates consumption charge using flat rate or tiered pricing. */
    public BigDecimal calculateConsumptionAmount(Tariff tariff, BigDecimal consumption) {
        if (tariff.getFlatRate() != null && tariff.getFlatRate().compareTo(BigDecimal.ZERO) > 0) {
            return consumption.multiply(tariff.getFlatRate()).setScale(2, RoundingMode.HALF_UP);
        }
        return calculateTierAmount(tariff, consumption);
    }

    /** Applies tiered rate brackets to consumption volume. */
    public BigDecimal calculateTierAmount(Tariff tariff, BigDecimal consumption) {
        if (tariff.getTiers() == null || tariff.getTiers().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal remaining = consumption;
        BigDecimal total = BigDecimal.ZERO;

        for (TariffTier tier : tariff.getTiers()) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal tierMin = tier.getMinConsumption();
            BigDecimal tierMax = tier.getMaxConsumption() != null
                    ? tier.getMaxConsumption()
                    : remaining;

            BigDecimal tierRange = tierMax.subtract(tierMin).add(BigDecimal.ONE);
            if (tier.getMaxConsumption() == null) {
                tierRange = remaining;
            } else {
                tierRange = tierRange.min(remaining);
            }

            if (tierRange.compareTo(BigDecimal.ZERO) > 0) {
                total = total.add(tierRange.multiply(tier.getRate()));
                remaining = remaining.subtract(tierRange);
            }
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /** Computes VAT as a percentage of the subtotal. */
    public BigDecimal calculateVat(BigDecimal subtotal, BigDecimal vatPercentage) {
        return subtotal.multiply(vatPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /** Computes late-payment penalty as a percentage of outstanding balance. */
    public BigDecimal calculatePenalty(BigDecimal outstandingBalance, BigDecimal penaltyPercentage) {
        if (outstandingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return outstandingBalance.multiply(penaltyPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
