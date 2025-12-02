package com.surestock.service;

import com.surestock.model.Business;
import com.surestock.model.Role;
import com.surestock.model.User;
import com.surestock.repository.BusinessRepository;
import com.surestock.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Registers a new Business Owner. This is an atomic operation that creates
     * a new Business entity and links the new User to the generated Business ID.
     * * @param email The user's login email (must be unique).
     * @param rawPassword The plain text password.
     * @param businessName The name of the new business tenant.
     * @return The newly created User object.
     */
    @Transactional
    public User registerOwner(String email, String rawPassword, String businessName) {
        // Check for duplicate email (Security/Integrity check)
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }

        // Create the Business (Database generates the unique Business ID)
        Business newBusiness = new Business();
        newBusiness.setName(businessName);
        newBusiness = businessRepository.save(newBusiness);

        // Create the User linked to that Business ID
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(encoder.encode(rawPassword));
        newUser.setRole(Role.OWNER);
        newUser.setBusinessId(newBusiness.getId());

        return userRepository.save(newUser);
    }

    /**
     * Creates a new Employee and assigns them to the specified Business ID.
     * This method is called by the Owner (admin).
     * @param businessId The ID of the owner's business (from security context).
     * @param dto The employee details.
     * @return The newly created Employee User object.
     */
    @Transactional
    public User createEmployee(Long businessId, com.surestock.dto.auth.@NotNull EmployeeCreationRequestDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }

        User newEmployee = new User();
        newEmployee.setEmail(dto.getEmail());
        newEmployee.setPassword(encoder.encode(dto.getPassword()));
        newEmployee.setRole(Role.EMPLOYEE); // Role is always EMPLOYEE here
        newEmployee.setBusinessId(businessId); // Business ID is assigned securely

        return userRepository.save(newEmployee);
    }

    public User authenticate(String email, String rawPassword) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                // Returns HTTP 401 Unauthorized (better than 404/500 for security)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        // Check password hash
        if (!encoder.matches(rawPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return user;
    }

    /**
     * Retrieves a user by email. Used primarily by Controllers to pull user data
     * from the security principal (session).
     * * @param email The user's email.
     * @return The User object.
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /**
     * Retrieves a user by their unique ID. Used by the UserController for security
     * checks when deleting employees.
     * * @param id The user's unique ID.
     * @return The User object.
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /**
     * Deletes a user record (used for deleting an employee).
     * * @param id The ID of the user to delete.
     */
    public void deleteUser(Long id) {
        // We assume integrity checks (role, business ID) are done in the Controller.
        userRepository.deleteById(id);
    }
}