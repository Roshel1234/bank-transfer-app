
package com.example.bankapp.config;

import com.example.bankapp.model.Customer;
import com.example.bankapp.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) {

        
        if (customerRepository.count() == 0) {

            Customer customer1 = new Customer();
            customer1.setFullName("Kamal");
            customer1.setEmail("kamal@gmail.com");
            customer1.setAccountNumber("100001");
            customer1.setBalance(new BigDecimal("50000.00"));

            customerRepository.save(customer1);

            Customer customer2 = new Customer();
            customer2.setFullName("Nimali");
            customer2.setEmail("nimali@gmail.com");
            customer2.setAccountNumber("100002");
            customer2.setBalance(new BigDecimal("25000.00"));

            customerRepository.save(customer2);

            Customer customer3 = new Customer();
            customer3.setFullName("Saman");
            customer3.setEmail("saman@gmail.com");
            customer3.setAccountNumber("100003");
            customer3.setBalance(new BigDecimal("10000.00"));

            customerRepository.save(customer3);

            System.out.println("Sample customers inserted successfully!");
        }
    }
}
