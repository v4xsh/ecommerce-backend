package com.example.ecommerce.webhook;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
public class PaymentWebhookController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @PostMapping("/payment")
    public void handlePaymentWebhook(@RequestBody Map<String, Object> payload) {
        System.out.println("Webhook Payload Received: " + payload);

        String event = (String) payload.get("event");

        if ("payment.captured".equals(event)) {
            try {
                // Extract payment details from the payload structure
                Map<String, Object> payloadData = (Map<String, Object>) payload.get("payload");
                Map<String, Object> paymentData = (Map<String, Object>) payloadData.get("payment");
                Map<String, Object> entityData = (Map<String, Object>) paymentData.get("entity");

                String razorpayOrderId = (String) entityData.get("order_id");

                // Log successful capture for monitoring
                System.out.println("Payment Captured for Razorpay Order ID: " + razorpayOrderId);

            } catch (Exception e) {
                System.err.println("Error parsing webhook payload: " + e.getMessage());
            }
        }
    }

    /**
     * Simulation endpoint to manually trigger payment success for development/demo purposes.
     */
    @PostMapping("/simulate-success")
    public String simulateSuccess(@RequestParam String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setStatus("PAID");
        orderRepository.save(order);

        Payment payment = paymentRepository.findByOrderId(orderId);
        if (payment != null) {
            payment.setStatus("SUCCESS");
            paymentRepository.save(payment);
        }

        return "Order " + orderId + " successfully marked as PAID";
    }
}