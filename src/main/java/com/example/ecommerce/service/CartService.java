package com.example.ecommerce.service;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    public CartItem addToCart(AddToCartRequest request) {
        // 1. Validate Product Exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 2. Check Stock
        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        // 3. Check if item already in cart
        CartItem existingItem = cartRepository.findByUserIdAndProductId(request.getUserId(), request.getProductId());

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            return cartRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUserId(request.getUserId());
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            return cartRepository.save(newItem);
        }
    }

    public List<CartItem> getCartItems(String userId) {
        List<CartItem> items = cartRepository.findByUserId(userId);

        // Populate the Transient "product" field for the response
        for (CartItem item : items) {
            Optional<Product> p = productRepository.findById(item.getProductId());
            p.ifPresent(item::setProduct);
        }
        return items;
    }

    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }
}