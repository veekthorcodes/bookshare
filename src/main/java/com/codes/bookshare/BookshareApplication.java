package com.codes.bookshare;

import com.codes.bookshare.role.Role;
import com.codes.bookshare.role.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class BookshareApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookshareApplication.class, args);
    }


    @Bean
    CommandLineRunner commandLineRunner(RoleRepository roleRepository) {
        return args -> {
           if(roleRepository.findByName("USER").isEmpty()) {
               roleRepository.save(
                       Role.builder().name("USER").build()
               );
           }
        };
    }
}
