package org.example.order.service.domain;

import org.example.order.service.domain.dto.create.CreateOrderCommand;
import org.example.order.service.domain.dto.create.CreateOrderResponse;
import org.example.order.service.domain.dto.create.OrderAddress;
import org.example.order.service.domain.dto.create.OrderItem;
import org.example.order.service.domain.entity.Customer;
import org.example.order.service.domain.entity.Order;
import org.example.order.service.domain.entity.Product;
import org.example.order.service.domain.entity.Restaurant;
import org.example.order.service.domain.exception.OrderDomainException;
import org.example.order.service.domain.mapper.OrderDataMapper;
import org.example.order.service.domain.ports.input.service.OrderApplicationService;
import org.example.order.service.domain.ports.output.repository.CustomerRepository;
import org.example.order.service.domain.ports.output.repository.OrderRepository;
import org.example.order.service.domain.ports.output.repository.RestaurantRepository;
import org.example.valueobject.CustomerId;
import org.example.valueobject.Money;
import org.example.valueobject.OrderId;
import org.example.valueobject.OrderStatus;
import org.example.valueobject.ProductId;
import org.example.valueobject.RestaurantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
class OrderApplicationServiceTest {

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private OrderDataMapper orderDataMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private CreateOrderCommand createOrderCommand;
    private CreateOrderCommand createOrderCommandWrongPrice;
    private CreateOrderCommand createOrderCommandWrongProductPrice;

    private final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID RESTAURANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private final UUID PRODUCT_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private final UUID ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");

    private final BigDecimal PRICE = new BigDecimal("200.00");

    @BeforeEach
    public void init() {
        createOrderCommand = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(OrderAddress.builder()
                        .street("street 1")
                        .postalCode("12345")
                        .city("city 1")
                        .build())
                .price(PRICE)
                .items(List.of(
                        OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(1)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("50.00"))
                                .build(),
                        OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(3)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("150.00"))
                                .build()))
                .build();

        createOrderCommandWrongPrice = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(OrderAddress.builder()
                        .street("street 1")
                        .postalCode("12345")
                        .city("city 1")
                        .build())
                .price(new BigDecimal("250.00")) // 250 is not a valid price
                .items(List.of(
                        OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(1)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("50.00"))
                                .build(),
                        OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(3)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("150.00"))
                                .build()))
                .build();

        createOrderCommandWrongProductPrice = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(OrderAddress.builder()
                        .street("street 1")
                        .postalCode("12345")
                        .city("city 1")
                        .build())
                .price(new BigDecimal("210.00"))
                .items(List.of(
                        OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(1)
                                .price(new BigDecimal("60.00"))
                                .subTotal(new BigDecimal("60.00"))
                                .build(),
                        OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(2)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("150.00"))
                                .build()))
                .build();

        Customer customer = new Customer();
        customer.setId(new CustomerId(CUSTOMER_ID));

        Restaurant restaurantResponse = Restaurant.builder()
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(List.of(
                        new Product(new ProductId(PRODUCT_ID), "product-1", new Money(new BigDecimal("50.00"))),
                        new Product(new ProductId(PRODUCT_ID), "product-2", new Money(new BigDecimal("50.00")))
                ))
                .active(true)
                .build();

        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        order.setId(new OrderId(ORDER_ID));

        when(customerRepository.findCustomer(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
                .thenReturn(Optional.of(restaurantResponse));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
    }

    @Test
    void createOrder_success_V1() {
        CreateOrderResponse createOrderResponse = orderApplicationService.createOrder(createOrderCommand);
        assertThat(createOrderResponse).isNotNull();
        assertThat(createOrderResponse.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(createOrderResponse.getMessage()).isEqualTo("Order created successfully");
        assertThat(createOrderResponse.getOrderTrackingId()).isNotNull();
    }

    @Test
    void createOrder_success_V2() {
        CreateOrderResponse createOrderResponse = orderApplicationService.createOrder(createOrderCommand);

        assertThat(createOrderResponse)
                .isNotNull()
                .satisfies(response -> {
                    assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
                    assertThat(response.getMessage()).isEqualTo("Order created successfully");
                    assertThat(response.getOrderTrackingId()).isNotNull();
                });
    }

    @Test
    void createOrder_WithWrongTotalPrice() {
        assertThatThrownBy(() -> orderApplicationService.createOrder(createOrderCommandWrongPrice))
                .isInstanceOf(OrderDomainException.class)
                .hasMessage("Total price: 250.00 is not equal to Order items total: 200.00!");
    }

    @Test
    void createOrder_WithWrongProductPrice() {
        assertThatThrownBy(() -> orderApplicationService.createOrder(createOrderCommandWrongProductPrice))
                .isInstanceOf(OrderDomainException.class)
                .hasMessage("Order item price: 60.00 is not valid for product " + PRODUCT_ID);
    }

    @Test
    void createOrder_WithPassiveRestaurant() {
        Restaurant restaurantResponse = Restaurant.builder()
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(List.of(
                        new Product(new ProductId(PRODUCT_ID), "product-1", new Money(new BigDecimal("50.00"))),
                        new Product(new ProductId(PRODUCT_ID), "product-2", new Money(new BigDecimal("50.00")))
                ))
                .active(false)
                .build();

        when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
                .thenReturn(Optional.of(restaurantResponse));

        assertThatThrownBy(() -> orderApplicationService.createOrder(createOrderCommand))
                .isInstanceOf(OrderDomainException.class)
                .hasMessage("Restaurant with id " + RESTAURANT_ID + " is currently not active!");

    }
}