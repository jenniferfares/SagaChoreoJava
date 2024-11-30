package org.example;
import com.rabbitmq.client.*;

public class PaymentService {
    private static final String PAYMENT_QUEUE = "payment.queue";
    private static final String ACCOUNTING_QUEUE = "accounting.queue";
    private static final String ORDER_QUEUE = "order.queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMQConnection.createChannel();
        RabbitMQConnection.setupQueues(channel);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("Payment Service received: " + message);

            // Simulate payment processing
            boolean paymentSuccess = processPayment(message);

            if (paymentSuccess) {
                String nextMessage = "Payment Processed: " + message;
                channel.basicPublish("", ACCOUNTING_QUEUE, null, nextMessage.getBytes());
                System.out.println("Payment Service published to Accounting Queue: " + nextMessage);
            } else {
                // If payment fails, roll back order by sending a message to Order Service
                String rollbackMessage = "Payment failed. Rolling back Order: " + message;
                channel.basicPublish("", ORDER_QUEUE, null, rollbackMessage.getBytes());
                System.out.println("Payment Service rollback: " + rollbackMessage);
            }
        };

        channel.basicConsume(PAYMENT_QUEUE, true, deliverCallback, consumerTag -> {});
    }

    // Simulate payment processing logic
    private static boolean processPayment(String message) {
        System.out.println("Processing Payment: " + message);
        return Math.random() > 0.5;  // Simulate a random failure (50% chance)
    }
}
