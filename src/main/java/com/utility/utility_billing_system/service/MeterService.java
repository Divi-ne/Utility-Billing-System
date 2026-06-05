package com.utility.utility_billing_system.service;

import com.utility.utility_billing_system.dto.meter.MeterRequest;
import com.utility.utility_billing_system.dto.meter.MeterResponse;
import com.utility.utility_billing_system.entity.Meter;
import com.utility.utility_billing_system.enums.StatusType;
import com.utility.utility_billing_system.exception.DuplicateResourceException;
import com.utility.utility_billing_system.exception.ResourceNotFoundException;
import com.utility.utility_billing_system.mapper.EntityMapper;
import com.utility.utility_billing_system.repository.MeterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for utility meter registration and maintenance.
 * <p>
 * Meters are assigned to customers and typed as WATER or ELECTRICITY.
 * Only active meters can receive readings and generate bills.
 */
@Slf4j
@Service
public class MeterService {

    private final MeterRepository meterRepository;
    private final CustomerService customerService;

    public MeterService(MeterRepository meterRepository, CustomerService customerService) {
        this.meterRepository = meterRepository;
        this.customerService = customerService;
    }

    @Transactional(readOnly = true)
    /** Lists all registered meters. */
    public List<MeterResponse> getAllMeters() {
        return meterRepository.findAll().stream()
                .map(EntityMapper::toMeterResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    /** Retrieves a meter by ID with customer details. */
    public MeterResponse getMeterById(Long id) {
        return EntityMapper.toMeterResponse(findMeter(id));
    }

    @Transactional(readOnly = true)
    /** Lists all meters assigned to a customer. */
    public List<MeterResponse> getMetersByCustomer(Long customerId) {
        return meterRepository.findByCustomerId(customerId).stream()
                .map(EntityMapper::toMeterResponse)
                .toList();
    }

    @Transactional
    /** Registers a new meter with a unique meter number. */
    public MeterResponse createMeter(MeterRequest request) {
        if (meterRepository.existsByMeterNumber(request.getMeterNumber())) {
            throw new DuplicateResourceException("Meter number already exists: " + request.getMeterNumber());
        }

        Meter meter = Meter.builder()
                .meterNumber(request.getMeterNumber())
                .meterType(request.getMeterType())
                .installationDate(request.getInstallationDate())
                .status(request.getStatus() != null ? request.getStatus() : StatusType.ACTIVE)
                .customer(customerService.findCustomer(request.getCustomerId()))
                .build();

        Meter saved = meterRepository.save(meter);
        log.info("Meter created: {}", saved.getMeterNumber());
        return EntityMapper.toMeterResponse(saved);
    }

    @Transactional
    /** Updates meter details and customer assignment. */
    public MeterResponse updateMeter(Long id, MeterRequest request) {
        Meter meter = findMeter(id);

        meterRepository.findByMeterNumber(request.getMeterNumber()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("Meter number already exists: " + request.getMeterNumber());
            }
        });

        meter.setMeterNumber(request.getMeterNumber());
        meter.setMeterType(request.getMeterType());
        meter.setInstallationDate(request.getInstallationDate());
        if (request.getStatus() != null) {
            meter.setStatus(request.getStatus());
        }
        meter.setCustomer(customerService.findCustomer(request.getCustomerId()));

        log.info("Meter updated: {}", meter.getMeterNumber());
        return EntityMapper.toMeterResponse(meter);
    }

    @Transactional
    /** Sets meter ACTIVE or INACTIVE status. */
    public MeterResponse updateStatus(Long id, StatusType status) {
        Meter meter = findMeter(id);
        meter.setStatus(status);
        log.info("Meter {} status changed to {}", meter.getMeterNumber(), status);
        return EntityMapper.toMeterResponse(meter);
    }

    @Transactional
    /** Removes a meter record. */
    public void deleteMeter(Long id) {
        Meter meter = findMeter(id);
        meterRepository.delete(meter);
        log.info("Meter deleted: {}", meter.getMeterNumber());
    }

    /** Loads a meter entity or throws ResourceNotFoundException. */
    public Meter findMeter(Long id) {
        return meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found with id: " + id));
    }
}
