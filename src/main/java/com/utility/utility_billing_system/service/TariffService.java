package com.utility.utility_billing_system.service;

import com.utility.utility_billing_system.dto.tariff.TariffRequest;
import com.utility.utility_billing_system.dto.tariff.TariffResponse;
import com.utility.utility_billing_system.dto.tariff.TariffTierRequest;
import com.utility.utility_billing_system.entity.Tariff;
import com.utility.utility_billing_system.entity.TariffTier;
import com.utility.utility_billing_system.enums.MeterType;
import com.utility.utility_billing_system.enums.StatusType;
import com.utility.utility_billing_system.exception.BusinessRuleException;
import com.utility.utility_billing_system.exception.ResourceNotFoundException;
import com.utility.utility_billing_system.mapper.EntityMapper;
import com.utility.utility_billing_system.repository.TariffRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for tariff (pricing schedule) management.
 * <p>
 * Tariffs are versioned per meter type and support either a flat rate per unit
 * or tiered consumption brackets. Bill generation uses the effective tariff for the billing date.
 */
@Slf4j
@Service
public class TariffService {

    private final TariffRepository tariffRepository;

    public TariffService(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    @Transactional(readOnly = true)
    /** Lists all tariff versions across meter types. */
    public List<TariffResponse> getAllTariffs() {
        return tariffRepository.findAll().stream()
                .map(EntityMapper::toTariffResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    /** Retrieves a tariff with its tier definitions. */
    public TariffResponse getTariffById(Long id) {
        return EntityMapper.toTariffResponse(findTariff(id));
    }

    @Transactional(readOnly = true)
    /** Lists active tariffs for a specific meter type. */
    public List<TariffResponse> getTariffsByMeterType(MeterType meterType) {
        return tariffRepository.findByMeterTypeAndStatus(meterType, StatusType.ACTIVE).stream()
                .map(EntityMapper::toTariffResponse)
                .toList();
    }

    @Transactional
    /** Creates a new tariff version; auto-increments version number per meter type. */
    public TariffResponse createTariff(TariffRequest request) {
        validateTariffRequest(request);

        int nextVersion = tariffRepository
                .findTopByMeterTypeAndStatusOrderByVersionDesc(request.getMeterType(), StatusType.ACTIVE)
                .map(t -> t.getVersion() + 1)
                .orElse(1);

        Tariff tariff = Tariff.builder()
                .version(nextVersion)
                .meterType(request.getMeterType())
                .flatRate(request.getFlatRate())
                .fixedServiceCharge(request.getFixedServiceCharge())
                .vatPercentage(request.getVatPercentage())
                .latePaymentPenaltyPercentage(request.getLatePaymentPenaltyPercentage())
                .effectiveFrom(request.getEffectiveFrom())
                .status(request.getStatus() != null ? request.getStatus() : StatusType.ACTIVE)
                .tiers(new ArrayList<>())
                .build();

        if (request.getTiers() != null) {
            for (TariffTierRequest tierRequest : request.getTiers()) {
                TariffTier tier = TariffTier.builder()
                        .tariff(tariff)
                        .minConsumption(tierRequest.getMinConsumption())
                        .maxConsumption(tierRequest.getMaxConsumption())
                        .rate(tierRequest.getRate())
                        .build();
                tariff.getTiers().add(tier);
            }
        }

        Tariff saved = tariffRepository.save(tariff);
        log.info("Tariff version {} created for {}", saved.getVersion(), saved.getMeterType());
        return EntityMapper.toTariffResponse(saved);
    }

    /** Finds the highest-version active tariff effective on the given billing date. */
    public Tariff findEffectiveTariff(MeterType meterType, LocalDate billingDate) {
        return tariffRepository
                .findTopByMeterTypeAndStatusAndEffectiveFromLessThanEqualOrderByVersionDesc(
                        meterType, StatusType.ACTIVE, billingDate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No effective tariff found for " + meterType + " on " + billingDate));
    }

    /** Loads a tariff entity or throws ResourceNotFoundException. */
    public Tariff findTariff(Long id) {
        return tariffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff not found with id: " + id));
    }

    private void validateTariffRequest(TariffRequest request) {
        boolean hasFlatRate = request.getFlatRate() != null;
        boolean hasTiers = request.getTiers() != null && !request.getTiers().isEmpty();

        if (!hasFlatRate && !hasTiers) {
            throw new BusinessRuleException("Tariff must have either a flat rate or tier-based rates");
        }
    }
}
