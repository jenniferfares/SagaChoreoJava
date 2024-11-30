package org.example;
import com.rabbitmq.client.*;

public class AccountingService {
    private static final String ACCOUNTING_QUEUE = "accounting.queue";
    private static final String INVENTORY_QUEUE = "inventory.queue";
    private static final String PAYMENT_QUEUE = "payment.queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQConnection.createChannel();
        RabbitMQConnection.setupQueues(channel);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("Accounting Service received: " + message);

            // Simulate accounting record creation
            boolean accountingSuccess = processAccounting(message);

            if (accountingSuccess) {
                String nextMessage = "Accounting Record Created: " + message;
                channel.basicPublish("", INVENTORY_QUEUE, null, nextMessage.getBytes());
                System.out.println("Accounting Service published to Inventory Queue: " + nextMessage);
            } else {
                // If accounting fails, rollback the payment
                String rollbackMessage = "Accounting failed. Rolling back Payment: " + message;
                channel.basicPublish("", PAYMENT_QUEUE, null, rollbackMessage.getBytes());
                System.out.println("Accounting Service rollback: " + rollbackMessage);
            }
        };

        channel.basicConsume(ACCOUNTING_QUEUE, true, deliverCallback, consumerTag -> {});
    }

    // Simulate accounting record creation logic
    private static boolean processAccounting(String message) {
        System.out.println("Processing Accounting Record: " + message);
        return true;  // Simulate accounting success
    }
}
