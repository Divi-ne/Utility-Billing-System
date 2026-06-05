package com.utility.utility_billing_system.service;

import com.utility.utility_billing_system.dto.customer.CustomerProfileRequest;
import com.utility.utility_billing_system.dto.customer.CustomerRequest;
import com.utility.utility_billing_system.dto.customer.CustomerResponse;
import com.utility.utility_billing_system.entity.Customer;
import com.utility.utility_billing_system.entity.User;
import com.utility.utility_billing_system.enums.StatusType;
import com.utility.utility_billing_system.exception.DuplicateResourceException;
import com.utility.utility_billing_system.exception.ResourceNotFoundException;
import com.utility.utility_billing_system.mapper.EntityMapper;
import com.utility.utility_billing_system.repository.CustomerRepository;
import com.utility.utility_billing_system.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for customer profile business logic.
 * <p>
 * Manages the customer entity and its optional link to a user account.
 * Enforces unique national ID constraints and synchronizes user profile fields.
 */
@Slf4j
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserService userService;

    public CustomerService(CustomerRepository customerRepository, UserService userService) {
        this.customerRepository = customerRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    /** Loads the customer profile for the currently authenticated user. */
    public CustomerResponse getMyProfile() {
        User user = userService.findUserByEmail(SecurityUtils.getCurrentUserEmail());
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for current user"));
        return EntityMapper.toCustomerResponse(customer);
    }

    @Transactional
    /** Updates profile fields for the authenticated customer and linked user. */
    public CustomerResponse updateMyProfile(CustomerProfileRequest request) {
        User user = userService.findUserByEmail(SecurityUtils.getCurrentUserEmail());
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for current user"));

        validateNationalId(customer.getId(), request.getNationalId());

        customer.setFullName(request.getFullName());
        customer.setNationalId(request.getNationalId());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        log.info("Customer updated own profile: {}", customer.getNationalId());
        return EntityMapper.toCustomerResponse(customer);
    }

    @Transactional
    /** Creates a customer profile linked to a newly registered user (called during signup). */
    public CustomerResponse createProfileForUser(User user, String nationalId, String address) {
        validateNationalId(null, nationalId);

        Customer customer = Customer.builder()
                .fullName(user.getFullName())
                .nationalId(nationalId)
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(address)
                .status(user.getStatus())
                .user(user)
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Customer profile created for user: {}", user.getEmail());
        return EntityMapper.toCustomerResponse(saved);
    }

    @Transactional(readOnly = true)
    /** Returns all customer profiles for staff dashboards. */
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(EntityMapper::toCustomerResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    /** Retrieves a customer by primary key. */
    public CustomerResponse getCustomerById(Long id) {
        return EntityMapper.toCustomerResponse(findCustomer(id));
    }

    @Transactional
    /** Creates a customer profile, optionally linked to an existing user. */
    public CustomerResponse createCustomer(CustomerRequest request) {
        validateNationalId(null, request.getNationalId());

        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .nationalId(request.getNationalId())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .status(request.getStatus() != null ? request.getStatus() : StatusType.ACTIVE)
                .user(resolveUser(request.getUserId()))
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Customer created: {} ({})", saved.getFullName(), saved.getNationalId());
        return EntityMapper.toCustomerResponse(saved);
    }

    @Transactional
    /** Updates all customer fields including optional user link. */
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = findCustomer(id);
        validateNationalId(id, request.getNationalId());

        customer.setFullName(request.getFullName());
        customer.setNationalId(request.getNationalId());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
        }
        customer.setUser(resolveUser(request.getUserId()));

        log.info("Customer updated: {}", customer.getNationalId());
        return EntityMapper.toCustomerResponse(customer);
    }

    @Transactional
    /** Sets customer ACTIVE or INACTIVE status. */
    public CustomerResponse updateStatus(Long id, StatusType status) {
        Customer customer = findCustomer(id);
        customer.setStatus(status);
        log.info("Customer {} status changed to {}", customer.getNationalId(), status);
        return EntityMapper.toCustomerResponse(customer);
    }

    @Transactional
    /** Removes a customer record from the database. */
    public void deleteCustomer(Long id) {
        Customer customer = findCustomer(id);
        customerRepository.delete(customer);
        log.info("Customer deleted: {}", customer.getNationalId());
    }

    /** Loads a customer entity or throws ResourceNotFoundException. */
    public Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    private void validateNationalId(Long id, String nationalId) {
        customerRepository.findByNationalId(nationalId).ifPresent(existing -> {
            if (id == null || !existing.getId().equals(id)) {
                throw new DuplicateResourceException("National ID already exists: " + nationalId);
            }
        });
    }

    private User resolveUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return userService.findUser(userId);
    }
}
