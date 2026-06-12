package org.civlabs.civlabsschedulebackend.config;

import lombok.AllArgsConstructor;
import org.civlabs.civlabsschedulebackend.entity.Role;
import org.civlabs.civlabsschedulebackend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role("USER"));
            roleRepository.save(new Role("ADMIN"));
            roleRepository.save(new Role("MODERATOR"));
        }
    }
}