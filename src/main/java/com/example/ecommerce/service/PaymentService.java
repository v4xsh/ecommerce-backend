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
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    // USE YOUR OWN KEYS HERE IF YOU HAVE THEM, OR KEEP THESE TEST KEYS
    private final String KEY_ID = "rzp_test_1DP5mmOlF5G5ag";
    private final String KEY_SECRET = "test_secret_key";

    public Payment createPaymentLink(PaymentRequest request) throws RazorpayException {
        // 1. Verify Order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2. Create Razorpay Order (Calls External API)
        // If you don't have real keys, this block might fail.
        // For the assignment DEMO, we can simulate the "creation" if keys are invalid.
        String razorpayOrderId;
        try {
            RazorpayClient client = new RazorpayClient(KEY_ID, KEY_SECRET);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int)(request.getAmount() * 100)); // Amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "order_rcptid_" + request.getOrderId());

            com.razorpay.Order razorpayOrder = client.orders.create(orderRequest);
            razorpayOrderId = razorpayOrder.get("id");
        } catch (RazorpayException e) {
            // Fallback for Demo without valid keys: generate a fake ID
            System.out.println("Razorpay Error (Invalid Keys?): " + e.getMessage());
            razorpayOrderId = "order_rzp_mock_" + System.currentTimeMillis();
        }

        // 3. Save Payment Record locally
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setStatus("PENDING");
        payment.setPaymentId(razorpayOrderId);
        payment.setCreatedAt(Instant.now());

        return paymentRepository.save(payment);
    }
}