package org.example;
import com.rabbitmq.client.*;

public class InventoryService {
    private static final String INVENTORY_QUEUE = "inventory.queue";
    private static final String ACCOUNTING_QUEUE = "accounting.queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQConnection.createChannel();
        RabbitMQConnection.setupQueues(channel);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("Inventory Service received: " + message);

            // Simulate inventory update
            boolean inventorySuccess = updateInventory(message);

            if (inventorySuccess) {
                System.out.println("Inventory Service successfully processed: " + message);
            } else {
                // If inventory fails, rollback the accounting
                String rollbackMessage = "Inventory failed. Rolling back Accounting: " + message;
                channel.basicPublish("", ACCOUNTING_QUEUE, null, rollbackMessage.getBytes());
                System.out.println("Inventory Service rollback: " + rollbackMessage);
            }
        };

        channel.basicConsume(INVENTORY_QUEUE, true, deliverCallback, consumerTag -> {});
    }

    // Simulate inventory update logic
    private static boolean updateInventory(String message) {
        System.out.println("Updating Inventory for: " + message);
        return true;  // Simulate inventory update success
    }
}
