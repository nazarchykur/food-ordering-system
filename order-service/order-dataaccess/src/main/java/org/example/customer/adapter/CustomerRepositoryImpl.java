package org.example.customer.adapter;

import lombok.RequiredArgsConstructor;
import org.example.customer.mapper.CustomerDataAccessMapper;
import org.example.customer.repository.CustomerJpaRepository;
import org.example.order.service.domain.entity.Customer;
import org.example.order.service.domain.ports.output.repository.CustomerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerJpaRepository customerJpaRepository;
    private final CustomerDataAccessMapper customerDataAccessMapper;

    @Override
    public Optional<Customer> findCustomer(UUID customerId) {
        return customerJpaRepository.findById(customerId)
                .map(customerDataAccessMapper::customerEntityToCustomer);
    }
}
