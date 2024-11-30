package org.example;
import com.rabbitmq.client.*;

public class RabbitMQConnection {

    // Method to create a RabbitMQ channel
    public static Channel createChannel() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);  // Default RabbitMQ port
        Connection connection = factory.newConnection();
        return connection.createChannel();
    }

    // Method to declare the necessary queues for the saga
    public static void setupQueues(Channel channel) throws Exception {
        // Declare the queues
        channel.queueDeclare("order", false, false, false, null);
        channel.queueDeclare("payment", false, false, false, null);
        channel.queueDeclare("accounting", false, false, false, null);
        channel.queueDeclare("inventory", false, false, false, null);
    }
}
