package com.example.demo;

import com.example.demo.config.AdminProperties;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableConfigurationProperties(AdminProperties.class)
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AdminProperties adminProperties
    ) {
        return args -> {
            String adminUsername = adminProperties.getUsername();
            String adminPassword = adminProperties.getPassword();
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                User admin = new User(adminUsername, passwordEncoder.encode(adminPassword), "ROLE_ADMIN");
                userRepository.save(admin);
                System.out.println("Default admin user created: " + adminUsername);
            }
        };
    }
}