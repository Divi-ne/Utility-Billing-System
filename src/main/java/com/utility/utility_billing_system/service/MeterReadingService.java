package com.utility.utility_billing_system.service;

import com.utility.utility_billing_system.dto.reading.MeterReadingRequest;
import com.utility.utility_billing_system.dto.reading.MeterReadingResponse;
import com.utility.utility_billing_system.entity.Meter;
import com.utility.utility_billing_system.entity.MeterReading;
import com.utility.utility_billing_system.enums.StatusType;
import com.utility.utility_billing_system.exception.BusinessRuleException;
import com.utility.utility_billing_system.exception.DuplicateResourceException;
import com.utility.utility_billing_system.exception.ResourceNotFoundException;
import com.utility.utility_billing_system.mapper.EntityMapper;
import com.utility.utility_billing_system.repository.MeterReadingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for recording and retrieving meter consumption readings.
 * <p>
 * Each meter has at most one reading per month. Readings must show increasing
 * values and are prerequisites for bill generation.
 */
@Slf4j
@Service
public class MeterReadingService {

    private final MeterReadingRepository meterReadingRepository;
    private final MeterService meterService;

    public MeterReadingService(MeterReadingRepository meterReadingRepository, MeterService meterService) {
        this.meterReadingRepository = meterReadingRepository;
        this.meterService = meterService;
    }

    @Transactional(readOnly = true)
    /** Lists all meter readings in the system. */
    public List<MeterReadingResponse> getAllReadings() {
        return meterReadingRepository.findAll().stream()
                .map(EntityMapper::toMeterReadingResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    /** Retrieves a reading with computed consumption. */
    public MeterReadingResponse getReadingById(Long id) {
        return EntityMapper.toMeterReadingResponse(findReading(id));
    }

    @Transactional
    /** Records a new monthly reading after validating meter status and reading values. */
    public MeterReadingResponse createReading(MeterReadingRequest request) {
        Meter meter = meterService.findMeter(request.getMeterId());

        if (meter.getStatus() != StatusType.ACTIVE) {
            throw new BusinessRuleException("Meter must be ACTIVE to record a reading");
        }

        if (request.getCurrentReading().compareTo(request.getPreviousReading()) <= 0) {
            throw new BusinessRuleException("Current reading must be greater than previous reading");
        }

        if (meterReadingRepository.existsByMeterIdAndReadingMonthAndReadingYear(
                request.getMeterId(), request.getReadingMonth(), request.getReadingYear())) {
            throw new DuplicateResourceException(
                    "Reading already exists for meter in " + request.getReadingMonth() + "/" + request.getReadingYear());
        }

        MeterReading reading = MeterReading.builder()
                .meter(meter)
                .previousReading(request.getPreviousReading())
                .currentReading(request.getCurrentReading())
                .readingDate(request.getReadingDate())
                .readingMonth(request.getReadingMonth())
                .readingYear(request.getReadingYear())
                .build();

        MeterReading saved = meterReadingRepository.save(reading);
        log.info("Meter reading recorded for meter {} - {}/{}",
                meter.getMeterNumber(), request.getReadingMonth(), request.getReadingYear());
        return EntityMapper.toMeterReadingResponse(saved);
    }

    /** Loads a meter reading entity or throws ResourceNotFoundException. */
    public MeterReading findReading(Long id) {
        return meterReadingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter reading not found with id: " + id));
    }
}
