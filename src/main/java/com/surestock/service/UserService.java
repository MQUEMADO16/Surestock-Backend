package com.surestock.service;

import com.surestock.dto.auth.EmployeeCreationRequestDTO;
import com.surestock.model.Business;
import com.surestock.model.Role;
import com.surestock.model.User;
import com.surestock.repository.BusinessRepository;
import com.surestock.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * REQUIRED by Spring Security.
     * This connects your database User entity to the AuthenticationManager.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Fetch our custom "User" entity from the DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Map it to the Spring Security "UserDetails" object
        // We use the fully qualified name to avoid conflict with our own "User" class
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    @Transactional
    public User registerOwner(String email, String rawPassword, String businessName) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }

        Business newBusiness = new Business();
        newBusiness.setName(businessName);
        newBusiness = businessRepository.save(newBusiness);

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(encoder.encode(rawPassword));
        newUser.setRole(Role.OWNER);
        newUser.setBusinessId(newBusiness.getId());

        return userRepository.save(newUser);
    }

    @Transactional
    public User createEmployee(Long businessId, @NotNull EmployeeCreationRequestDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }

        User newEmployee = new User();
        newEmployee.setEmail(dto.getEmail());
        newEmployee.setPassword(encoder.encode(dto.getPassword()));
        newEmployee.setRole(Role.EMPLOYEE);
        newEmployee.setBusinessId(businessId);

        return userRepository.save(newEmployee);
    }

    public List<User> getAllEmployees(Long businessId) {
        // Assuming your UserRepository extends JpaRepository or CrudRepository
        // You might need to add `List<User> findByBusinessId(Long businessId);` to your repository interface if it's not there.
        // Assuming findByBusinessId exists based on standard naming conventions or previous context.
        return userRepository.findByBusinessId(businessId);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}