package org.example;

import com.rabbitmq.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AccountingService {
    private static final String ACCOUNTING_QUEUE = "accounting.queue";
    private static final String INVENTORY_QUEUE = "inventory.queue";
    private static final String PAYMENT_QUEUE = "payment.queue";

    public static void main(String[] args) {
        try {
            Channel channel = RabbitMQConnection.createChannel();
            RabbitMQConnection.setupQueues(channel);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                ObjectMapper objectMapper = new ObjectMapper();
                String message = new String(delivery.getBody(), "UTF-8");
                OrderMessage orderMessage = objectMapper.readValue(message, OrderMessage.class);

                System.out.println("Accounting Service received: " + orderMessage);

                // Simulate accounting processing
                boolean accountingSuccess = processAccounting(orderMessage);

                if (accountingSuccess) {
                    String nextMessage = objectMapper.writeValueAsString(orderMessage);
                    channel.basicPublish("", INVENTORY_QUEUE, null, nextMessage.getBytes());
                    System.out.println("Accounting Service published to Inventory Queue: " + nextMessage);
                } else {
                    String rollbackMessage = objectMapper.writeValueAsString(orderMessage);
                    channel.basicPublish("", PAYMENT_QUEUE, null, rollbackMessage.getBytes());
                    System.out.println("Accounting Service rollback to Payment Service: " + rollbackMessage);
                }
            };

            channel.basicConsume(ACCOUNTING_QUEUE, true, deliverCallback, consumerTag -> {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean processAccounting(OrderMessage orderMessage) {
        System.out.println("Processing Accounting: " + orderMessage);
        return Math.random() > 0.5;
    }
}
