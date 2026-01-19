package com.example.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cart_items")
public class CartItem {
    @Id
    private String id;
    private String userId;
    private String productId;
    private Integer quantity;

    // This field is ignored by MongoDB (not saved in DB)
    // But we populate it in the Service so the frontend/Postman sees the details
    @Transient
    private Product product;
}