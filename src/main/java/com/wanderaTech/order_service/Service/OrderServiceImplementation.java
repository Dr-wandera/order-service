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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
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

    // Logged-in user places order
    @Transactional
    @Override
    public OrderResponse placeOrder(String userId,OrderRequest orderRequest) {

        //generate order number
        String orderNumber = generateOrderNumber();

        //check order number to avoid multiple save of order with the same order Number
        if(orderRepository.existsByOrderNumber(orderNumber)){
            log.warn("Order already exists: {}", orderNumber);
            return toDto(orderRepository.findByOrderNumber(orderNumber).get());
        }

        // get user email from the userSnapShot saved in the order service.  (user replica)
        UsersSnapShot usersSnapShot = userSnapShotRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("customerId not available"));

        String email = usersSnapShot.getEmail();
        String firstName = usersSnapShot.getFirstName();

        log.info("Order placement has started ");
        //  Fetch cart items from cart service (web client)
        List<CartItem> cartItems = cartClient.getCartItems(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }


        //  Calculate total amount of the items  added in the cart
        double total = cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        log.info("Order total amount is {}", total);


        //  Create Order entity
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUserId(userId);
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setDeliveryAddress(orderRequest.getDeliveryAddress());
        order.setOrderStatus(OrderStatus.PENDING); //status will be changed  to paid if payment is done successful
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

        //sends notification event to customer of item bought if the status is turned paid
        if (savedOrder.getOrderStatus().equals(OrderStatus.PAID)) {
            notificationProducer.sendOrderPlacedNotificationToCustomer(
                    OrderPlacedEvent.builder()
                            .orderNumber(savedOrder.getOrderNumber())
                            .userId(savedOrder.getUserId())
                            .email(email)
                            .firstName(firstName)
                            .totalAmount(savedOrder.getTotalAmount())
                            .createdAt(savedOrder.getOrderDate())

                            //  convert orderItem (entity) to order item event  (DTO for Kafka)
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

            // 3. Publish reduce stock event   to inventory (iterate) Kafka
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
            cartClient.clearCart(userId);
            log.info("Cart items cleared  successfully of customer id {}", userId);

        }

        return toDto(savedOrder);
    }

    //method  that return order a user has placed
    @Cacheable(value = "ORDER_DATA", key = "#userId")    @Override
    public List<OrderResponse> getOrdersByCustomer(String userId) {

        List<Order> orders = orderRepository.findAllByUserId(userId);

        if (orders.isEmpty()) {
            throw new RuntimeException("You have no order by now");
        }

        log.info("This is your orders ");

        return orders.stream()
                .map(this::toDto)
                .toList();

    }

    // method that return order based  on the date placed
    @Override
    public List<OrderResponse> getAllOrdersUnderDate(LocalDate orderDate, int size, int page) {

        Pageable pageable=PageRequest.of(page,size);

        Page<Order> orderPage=orderRepository.findByOrderDate(orderDate,pageable);

        //return order response in pages
               return orderPage.getContent()
                .stream()
                .map(order -> new OrderResponse(
                       order.getOrderNumber(),
                       order.getTotalAmount(),
                        order.getItems()
               ))
                .toList();

    }

//    @Override
//    public List<OrderResponse> getAllOrdersUnderDate(LocalDate orderDate, int size, int page) {
//
//        Pageable pageable= (Pageable) PageRequest.of(page,size);
//
//
//        Page<Order> productsPage =
//                orderRepository.findByOrderDate(orderDate,pageable);
//
//        return productsPage.getContent()
//                .stream()
//                .map(product -> new ProductResponse(
//                        product.getProductName(),
//                        product.getProductDescription(),
//                        product.getPrice()
//                ))
//                .toList();
//    }
//
//    }


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
