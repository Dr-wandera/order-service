package com.wanderaTech.order_service.Service;

import com.wanderaTech.common_events.NotificationEvent.OrderItemEvent;
import com.wanderaTech.common_events.NotificationEvent.OrderPlacedEvent;
import com.wanderaTech.common_events.PaymentEvent.OrderPaymentEvent;
import com.wanderaTech.common_events.productEvent.StockReduceEvent;
import com.wanderaTech.order_service.Client.CartClient;
import com.wanderaTech.order_service.Enum.OrderStatus;
import com.wanderaTech.order_service.KafkaConfig.NotificationProducer;
import com.wanderaTech.order_service.KafkaConfig.PaymentRequestProducer;
import com.wanderaTech.order_service.KafkaConfig.ReduceStockProducer;
import com.wanderaTech.order_service.Model.Order;
import com.wanderaTech.order_service.Model.OrderItem;
import com.wanderaTech.order_service.OrderDto.*;
import com.wanderaTech.order_service.Repository.OrderRepository;
import com.wanderaTech.order_service.Repository.UserSnapShotRepository;
import com.wanderaTech.order_service.UsersReplicaModel.UsersSnapShot;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImplementation implements OrderServiceInterface {
    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final ReduceStockProducer reduceStockProducer;
    private final NotificationProducer notificationProducer;
    private final PaymentRequestProducer paymentRequestProducer;
    private final UserSnapShotRepository userSnapShotRepository;

    @Transactional
    @Override
    public OrderResponse placeOrder(OrderRequest orderRequest) {

        //generate order number
        String orderNumber = generateOrderNumber();

        //check order number to avoid multiple save of order with the same order Number
        if(orderRepository.existsByOrderNumber(orderNumber)){
            log.warn("Order already exists: {}", orderNumber);
            return toDto(orderRepository.findByOrderNumber(orderNumber).get());
        }

        // get customer email from the customerSnapShot saved in the order service.  (Customer replica)
        UsersSnapShot usersSnapShot = userSnapShotRepository.findByUserId((orderRequest.getUserId()))
                .orElseThrow(() -> new RuntimeException("customerId not available"));

        String email = usersSnapShot.getEmail();
        String firstName = usersSnapShot.getFirstName();

        log.info("Order placement has started ");
        //  Fetch cart items from cart service (web client)
        List<CartItem> cartItems = cartClient.getCartItems(orderRequest.getUserId());
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }


        //  Calculate total amount of the items  added in the cart
        double total = cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        log.info("Order total amount has started  the amount is {}", total);


        //  Create Order entity
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUserId(orderRequest.getUserId());
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setDeliveryAddress(orderRequest.getDeliveryAddress());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setTotalAmount(total);
        order.setOrderDate(LocalDateTime.now());

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProductId(cartItem.getProductId());
                    orderItem.setProductName(cartItem.getProductName());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(cartItem.getPrice());
                    orderItem.setUserId(cartItem.getUserId());
                    orderItem.setOrder(order);
                    return orderItem;
                })
                .toList();

        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        log.info("Order  has saved successfully as PENDING  {}", savedOrder);

        //sends payment event
        paymentRequestProducer.sendOrderPlacementEvent(
                new OrderPaymentEvent(
                        savedOrder.getOrderNumber(),
                        savedOrder.getTotalAmount(),
                        orderRequest.getPhoneNumber()
                )
        );

        //this sends Kafka event to notification service to notify customer of item bought if the order status change to pay
        if (savedOrder.getOrderStatus().equals(OrderStatus.PAID)) {
            notificationProducer.sendOrderPlacedNotificationToCustomer(
                    OrderPlacedEvent.builder()
                            .orderNumber(savedOrder.getOrderNumber())
                            .customerId(savedOrder.getCustomerId())
                            .email(email)
                            .firstName(firstName)
                            .totalAmount(savedOrder.getTotalAmount())
                            .createdAt(savedOrder.getOrderDate())

                            //this  convert orderItem (entity) to order item event  (DTO for kafka)
                            .items(
                                    savedOrder.getItems().stream()
                                            .map(item -> OrderItemEvent.builder()
                                                    .productId(item.getProductId())
                                                    .productName(item.getProductName())
                                                    .quantity(item.getQuantity())
                                                    .price(item.getPrice())
                                                    .build())
                                            .toList()
                            )
                            .build()
            );


            // 3. Publish Kafka event   to reduce  product stock that is in the order item (iterate)
            for (OrderItem item : savedOrder.getItems()) {
                reduceStockProducer.sendReduceStockAfterProductPurchase(
                        new StockReduceEvent(
                                item.getProductId(),
                                item.getQuantity()

                        )
                );
                log.info("Kafka event sent  successfully to inventory to reduce product stock {}", savedOrder.getOrderNumber());
            }


            //  Clear cart after order placed in cart-service
            cartClient.clearCart(orderRequest.getCustomerId());
            log.info("Cart items cleared  successfully of customer id {}", orderRequest.getCustomerId());

        }
        return toDto(savedOrder);
    }
    @Override
    public List<OrderResponse> getOrdersByCustomer(String customerId) {

        List<Order> orders = orderRepository.findAllByCustomerId(customerId);

        if (orders.isEmpty()) {
            throw new RuntimeException("You have no order by now");
        }

        log.info("This is your orders ");

        return orders.stream()
                .map(this::toDto)
                .toList();

    }

    //converting entity to dto
    private OrderResponse toDto(Order savedOrder) {
        OrderResponse response=new OrderResponse();
        response.setOrderNumber(savedOrder.getOrderNumber());
        response.setTotalAmount(savedOrder.getTotalAmount());
        response.setItems(savedOrder.getItems());
        return response;
    }


    private final SecureRandom random = new SecureRandom();

    private String generateOrderNumber() {
        String orderNumber;
        do {
            int number = 10000000 + random.nextInt(90000000);
            orderNumber = String.valueOf(number);
        } while (orderRepository.existsByOrderNumber(orderNumber));

        return orderNumber;
    }


}
