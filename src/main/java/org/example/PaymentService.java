package org.example;

import com.rabbitmq.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PaymentService {
    private static final String PAYMENT_QUEUE = "payment.queue";
    private static final String ACCOUNTING_QUEUE = "accounting.queue";
    private static final String ORDER_QUEUE = "order.queue";

    public static void main(String[] args) {
        try {
            Channel channel = RabbitMQConnection.createChannel();
            RabbitMQConnection.setupQueues(channel);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                ObjectMapper objectMapper = new ObjectMapper();
                String message = new String(delivery.getBody(), "UTF-8");
                OrderMessage orderMessage = objectMapper.readValue(message, OrderMessage.class);

                System.out.println("Payment Service received: " + orderMessage);

                // Simulate payment processing
                boolean paymentSuccess = processPayment(orderMessage);

                if (paymentSuccess) {
                    String nextMessage = objectMapper.writeValueAsString(orderMessage);
                    channel.basicPublish("", ACCOUNTING_QUEUE, null, nextMessage.getBytes());
                    System.out.println("Payment Service published to Accounting Queue: " + nextMessage);
                } else {
                    String rollbackMessage = objectMapper.writeValueAsString(orderMessage);
                    channel.basicPublish("", ORDER_QUEUE, null, rollbackMessage.getBytes());
                    System.out.println("Payment Service rollback: " + rollbackMessage);
                }
            };

            channel.basicConsume(PAYMENT_QUEUE, true, deliverCallback, consumerTag -> {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean processPayment(OrderMessage orderMessage) {
        System.out.println("Processing Payment: " + orderMessage);
        return Math.random() > 0.5;
    }
}
