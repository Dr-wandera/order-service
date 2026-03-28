package com.wanderaTech.order_service.EventConsumer;

import com.wanderaTech.common_events.NotificationEvent.OrderItemEvent;
import com.wanderaTech.common_events.NotificationEvent.SellerNotificationEvent;
import com.wanderaTech.common_events.PaymentEvent.PaymentResultEvent;
import com.wanderaTech.order_service.Enum.OrderStatus;
import com.wanderaTech.order_service.KafkaConfig.NotificationProducer;
import com.wanderaTech.order_service.Model.Order;
import com.wanderaTech.order_service.Model.OrderItem;
import com.wanderaTech.order_service.Repository.OrderRepository;
import com.wanderaTech.order_service.Repository.UserSnapShotRepository;
import com.wanderaTech.order_service.UsersReplicaModel.UsersSnapShot;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentResultEventConsumer {
    private final OrderRepository orderRepository;
    private final NotificationProducer notificationProducer;
    private final UserSnapShotRepository userSnapShotRepository;

    @KafkaListener(
            topics = "payment-topic",
            groupId = "order-group"
    )

    //consume the result of payment from payment service
    public void consumePaymentTopic(PaymentResultEvent event) {

        //find the order number from order
        Optional<Order> optional =
                orderRepository.findByOrderNumber(event.getOrderNumber());

        if (optional.isPresent()) {

            Order order = optional.get();

            if (event.getStatus().equals("PAID")) {

                order.setOrderStatus(OrderStatus.PAID);

                //send notification to customer of item purchased
                sendCustomerNotification(order);

                // send notifications to seller for sale made
                sendSellerNotifications(order);


            } else {

                order.setOrderStatus(OrderStatus.FAILED);
                return;

            }

            orderRepository.save(order);
        }
    }

    //sends notification to customer of item bought after successfully payment
    private void sendCustomerNotification(Order order) {

    }

    //send notification to seller  if the payment status is "paid"
    private void sendSellerNotifications(Order order) {
        // get customer email from the customerSnapShot saved in the order service.  (Customer replica)
        UsersSnapShot usersSnapShot = userSnapShotRepository.findByUserId((order.getUserId()))
                .orElseThrow(()->new RuntimeException("customerId not available"));

        String email = usersSnapShot.getEmail();
        String customerPhoneNumber = usersSnapShot.getPhoneNumber();

        //this group the product of one seller in the order placed
        Map<String, List<OrderItem>> itemsBySeller =
                order.getItems()
                        .stream()
                        .collect(Collectors.groupingBy(OrderItem::getUserId));

        //this iterates over the list of order item to send notification to seller
        for (Map.Entry<String, List<OrderItem>> entry : itemsBySeller.entrySet()) {

            String sellerId = entry.getKey();
            List<OrderItem> sellerItems = entry.getValue();

            //check seller in  users snapShot
              UsersSnapShot userSnapShot=userSnapShotRepository.findByUserId(sellerId)
                    .orElseThrow(() -> new RuntimeException("Seller not found"));

            List<OrderItemEvent> itemEvents = sellerItems.stream()
                    .map(item -> OrderItemEvent.builder()
                            .productId(item.getProductId())
                            .productName(item.getProductName())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .build())
                    .toList();

            // send  the seller notification event to the notification service to notify seller
            notificationProducer.sendOrderPlacedNotificationToSeller(

                    SellerNotificationEvent.builder()
                            .sellerId(sellerId)
                            .sellerEmail(userSnapShot.getEmail())
                            .orderNumber(order.getOrderNumber())
                            .email(email)
                            .phoneNumber(customerPhoneNumber)
                            .items(itemEvents)
                            .createdAt(order.getOrderDate())
                            .build()
            );
        }

    }
}
