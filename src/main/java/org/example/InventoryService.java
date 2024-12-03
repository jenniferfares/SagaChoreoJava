package org.example;

import com.rabbitmq.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InventoryService {
    private static final String INVENTORY_QUEUE = "inventory.queue";
    private static final String ACCOUNTING_QUEUE = "accounting.queue";

    public static void main(String[] args) {
        try {
            Channel channel = RabbitMQConnection.createChannel();
            RabbitMQConnection.setupQueues(channel);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                ObjectMapper objectMapper = new ObjectMapper();
                String message = new String(delivery.getBody(), "UTF-8");
                OrderMessage orderMessage = objectMapper.readValue(message, OrderMessage.class);

                System.out.println("Inventory Service received: " + orderMessage);

                // Simulate inventory processing
                boolean inventorySuccess = processInventory(orderMessage);

                if (inventorySuccess) {
                    System.out.println("Inventory Service: Order fully processed: " + orderMessage);
                } else {
                    String rollbackMessage = objectMapper.writeValueAsString(orderMessage);
                    channel.basicPublish("", ACCOUNTING_QUEUE, null, rollbackMessage.getBytes());
                    System.out.println("Inventory Service rollback to Accounting Service: " + rollbackMessage);
                }
            };

            channel.basicConsume(INVENTORY_QUEUE, true, deliverCallback, consumerTag -> {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean processInventory(OrderMessage orderMessage) {
        System.out.println("Processing Inventory: " + orderMessage);
        return Math.random() > 0.5;
    }
}
