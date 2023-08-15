package org.example.order.service.domain.domain;

import org.example.order.service.domain.domain.entity.Order;
import org.example.order.service.domain.domain.entity.Restaurant;
import org.example.order.service.domain.domain.event.OrderCancelledEvent;
import org.example.order.service.domain.domain.event.OrderCreatedEvent;
import org.example.order.service.domain.domain.event.OrderPaidEvent;

import java.util.List;

public interface OrderDomainService {

    OrderCreatedEvent validateAndInitiateOrder(Order order, Restaurant restaurant);

    OrderPaidEvent payOrder(Order order);

    void approveOrder(Order order);

    OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages);

    void cancelOrder(Order order, List<String> failureMessages);
}
