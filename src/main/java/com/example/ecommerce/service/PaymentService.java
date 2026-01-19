package com.example.ecommerce.service;

import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    // Inject credentials from application.properties
    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public Payment createPaymentLink(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String razorpayOrderId;
        try {
            // Use the injected keys here
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int)(request.getAmount() * 100));
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "order_rcptid_" + request.getOrderId());

            com.razorpay.Order razorpayOrder = client.orders.create(orderRequest);
            razorpayOrderId = razorpayOrder.get("id");
        } catch (RazorpayException e) {
            // Simulation fallback if keys are invalid or missing
            razorpayOrderId = "order_rzp_mock_" + System.currentTimeMillis();
        }

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setStatus("PENDING");
        payment.setPaymentId(razorpayOrderId);
        payment.setCreatedAt(Instant.now());

        return paymentRepository.save(payment);
    }
}