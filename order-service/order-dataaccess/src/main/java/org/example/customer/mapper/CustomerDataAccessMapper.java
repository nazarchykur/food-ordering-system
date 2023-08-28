package org.example.customer.mapper;

import org.example.customer.entity.CustomerEntity;
import org.example.order.service.domain.entity.Customer;
import org.example.valueobject.CustomerId;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataAccessMapper {

    public Customer customerEntityToCustomer(CustomerEntity customerEntity) {
        return new Customer(new CustomerId(customerEntity.getId()));
    }
}
