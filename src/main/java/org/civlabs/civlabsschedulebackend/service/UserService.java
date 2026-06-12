package org.civlabs.civlabsschedulebackend.service;

import lombok.AllArgsConstructor;
import org.civlabs.civlabsschedulebackend.entity.Role;
import org.civlabs.civlabsschedulebackend.entity.User;
import org.civlabs.civlabsschedulebackend.repository.RoleRepository;
import org.civlabs.civlabsschedulebackend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> "ROLE_" + role.getName())
                        .toArray(String[]::new))
                .accountExpired(false)
                .accountLocked(!user.getIsActive())
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }

    @Transactional
    public void setUserActivity(String username, boolean active) {
        User user = userRepository.findByUsername(username)
                .orElseThrow();

        user.setActive(active);

        userRepository.save(user);
    }

    @Transactional
    public User createUser(String username, String email, String password, Set<String> roleNames) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setIsActive(true);

        // Assign roles
        Set<Role> roles = new HashSet<>();
        if (roleNames == null || roleNames.isEmpty()) {
            // Default role
            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> roleRepository.save(new Role("USER")));
            roles.add(userRole);
        } else {
            for (String roleName : roleNames) {
                Role role = roleRepository.findByName(roleName)
                        .orElseGet(() -> roleRepository.save(new Role(roleName)));
                roles.add(role);
            }
        }
        user.setRoles(roles);

        return userRepository.save(user);
    }


    @Transactional
    public User createUserWithGoogle(String username, String email, String googleId, Set<String> roleNames) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setGoogleId(googleId);
        user.setIsActive(true);

        // Assign roles
        Set<Role> roles = new HashSet<>();
        if (roleNames == null || roleNames.isEmpty()) {
            // Default role
            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> roleRepository.save(new Role("USER")));
            roles.add(userRole);
        } else {
            for (String roleName : roleNames) {
                Role role = roleRepository.findByName(roleName)
                        .orElseGet(() -> roleRepository.save(new Role(roleName)));
                roles.add(role);
            }
        }
        user.setRoles(roles);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUser(Long id, String email) {
        User user = findById(id);
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(email);
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }

    @Transactional
    public User addRoleToUser(String username, String roleName) {
        User user = findByUsername(username);
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    @Transactional
    public User removeRoleFromUser(String username, String roleName) {
        User user = findByUsername(username);
        user.getRoles().removeIf(role -> role.getName().equals(roleName));
        return userRepository.save(user);
    }
}