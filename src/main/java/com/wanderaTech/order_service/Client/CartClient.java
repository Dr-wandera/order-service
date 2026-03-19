package com.wanderaTech.order_service.Client;

import com.wanderaTech.order_service.OrderDto.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartClient {

    private final WebClient.Builder webClient;

    public List<CartItem> getCartItems(String customerId) {

        return webClient
                .baseUrl("http://cart-service")
                .build()
                .get()
                .uri("/api/v1/cart/{customerId}", customerId)
                .retrieve()
                .bodyToFlux(CartItem.class)
                .collectList()
                .block();
    }

    public void clearCart(String customerId) {
        webClient
                .baseUrl("http://cart-service")
                .build()
                .delete()
                .uri("/api/v1/cart/{customerId}", customerId)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}

