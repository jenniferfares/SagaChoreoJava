package org.example;

import com.rabbitmq.client.*;

public class OrderService {
    private static final String ORDER_QUEUE = "order.queue";
    private static final String PAYMENT_QUEUE = "payment.queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQConnection.createChannel();
        RabbitMQConnection.setupQueues(channel);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("Order Service received: " + message);

            // Simulate order processing
            boolean orderSuccess = processOrder(message);

            if (orderSuccess) {
                String nextMessage = "Order Created: " + message;
                channel.basicPublish("", PAYMENT_QUEUE, null, nextMessage.getBytes());
                System.out.println("Order Service published to Payment Queue: " + nextMessage);
            } else {
                System.out.println("Order Service failed. No rollback needed (it's the start of the saga).");
            }
        };

        channel.basicConsume(ORDER_QUEUE, true, deliverCallback, consumerTag -> {});
    }

    // Simulate order creation logic
    private static boolean processOrder(String message) {
        System.out.println("Processing Order: " + message);
        return true;  // Simulate order success
    }
}
