package org.example.order.service.domain.entity;

import org.example.entity.AggregateRoot;
import org.example.valueobject.CustomerId;

public class Customer extends AggregateRoot<CustomerId> {

    public Customer() {
    }

    public Customer(CustomerId customerId) {
        super.setId(customerId);
    }
}
