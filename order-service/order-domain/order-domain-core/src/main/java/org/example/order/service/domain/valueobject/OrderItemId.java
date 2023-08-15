package org.example.order.service.domain.valueobject;

import org.example.valueobject.BaseId;

public class OrderItemId extends BaseId<Long> {

    public OrderItemId(Long value) {
        super(value);
    }
}
