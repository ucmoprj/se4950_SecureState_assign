package edu.ucmo.se4950.securestate.web;

import edu.ucmo.se4950.securestate.core.DocumentStore;
import edu.ucmo.se4950.securestate.core.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SecureStateApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureStateApplication.class, args);
    }

    @Bean
    UserRepository userRepository() { return UserRepository.withDemoUsers(); }

    @Bean
    DocumentStore documentStore() { return new DocumentStore(); }
}
