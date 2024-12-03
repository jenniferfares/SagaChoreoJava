package org.example;

import com.rabbitmq.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OrderService {
    private static final String ORDER_QUEUE = "order.queue";
    private static final String PAYMENT_QUEUE = "payment.queue";

    public static void main(String[] args) {
        try {
            Channel channel = RabbitMQConnection.createChannel();
            RabbitMQConnection.setupQueues(channel);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                ObjectMapper objectMapper = new ObjectMapper();
                String message = new String(delivery.getBody(), "UTF-8");
                OrderMessage orderMessage = objectMapper.readValue(message, OrderMessage.class);

                System.out.println("Order Service received: " + orderMessage);

                // Simulate order processing
                boolean orderSuccess = processOrder(orderMessage);

                if (orderSuccess) {
                    String nextMessage = objectMapper.writeValueAsString(orderMessage);
                    channel.basicPublish("", PAYMENT_QUEUE, null, nextMessage.getBytes());
                    System.out.println("Order Service published to Payment Queue: " + nextMessage);
                } else {
                    System.out.println("Order Service failed. No rollback needed (it's the start of the saga).");
                }
            };

            channel.basicConsume(ORDER_QUEUE, true, deliverCallback, consumerTag -> {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean processOrder(OrderMessage orderMessage) {
        System.out.println("Processing Order: " + orderMessage);
        return true;
    }
}

