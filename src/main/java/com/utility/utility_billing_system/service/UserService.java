package com.utility.utility_billing_system.service;

import com.utility.utility_billing_system.dto.user.UserRequest;
import com.utility.utility_billing_system.dto.user.UserResponse;
import com.utility.utility_billing_system.dto.user.UserRoleUpdateRequest;
import com.utility.utility_billing_system.entity.Role;
import com.utility.utility_billing_system.entity.User;
import com.utility.utility_billing_system.enums.StatusType;
import com.utility.utility_billing_system.exception.BusinessRuleException;
import com.utility.utility_billing_system.exception.DuplicateResourceException;
import com.utility.utility_billing_system.exception.ResourceNotFoundException;
import com.utility.utility_billing_system.mapper.EntityMapper;
import com.utility.utility_billing_system.repository.RoleRepository;
import com.utility.utility_billing_system.repository.UserRepository;
import com.utility.utility_billing_system.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for system user account management.
 */
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserResponse getMyAccount() {
        return EntityMapper.toUserResponse(findUserByEmail(SecurityUtils.getCurrentUserEmail()));
    }

    @Transactional
    /** Creates a user who can sign in immediately without email OTP confirmation. */
    public UserResponse createUser(UserRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(email)
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(request.getStatus())
                .emailVerified(true)
                .roles(resolveRoles(request.getRoles()))
                .build();

        User saved = userRepository.save(user);
        log.info("Admin created user: {} with roles {}", saved.getEmail(), request.getRoles());
        return EntityMapper.toUserResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(EntityMapper::toUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return EntityMapper.toUserResponse(findUser(id));
    }

    @Transactional
    public UserResponse updateRoles(Long id, UserRoleUpdateRequest request) {
        User user = findUser(id);
        user.setRoles(resolveRoles(request.getRoles()));
        log.info("User {} roles updated to {}", user.getEmail(), request.getRoles());
        return EntityMapper.toUserResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User currentUser = findUserByEmail(SecurityUtils.getCurrentUserEmail());
        if (currentUser.getId().equals(id)) {
            throw new BusinessRuleException("Cannot delete your own account");
        }
        User user = findUser(id);
        userRepository.delete(user);
        log.info("User deleted: {}", user.getEmail());
    }

    public User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private Set<Role> resolveRoles(Set<com.utility.utility_billing_system.enums.RoleType> roleTypes) {
        Set<Role> roles = new HashSet<>();
        for (var roleType : roleTypes) {
            Role role = roleRepository.findByName(roleType)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleType));
            roles.add(role);
        }
        return roles;
    }
}
