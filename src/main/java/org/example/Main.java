package org.example;

import com.rabbitmq.client.Channel;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    public static void main(String[] args) {
        try {
            // Create a new channel and set up the queues
            Channel channel = RabbitMQConnection.createChannel();
            RabbitMQConnection.setupQueues(channel);

            // Create the order message in JSON format
            ObjectMapper objectMapper = new ObjectMapper();
            OrderMessage orderMessage = new OrderMessage(12345, 67890, new String[]{"Item1", "Item2"});
            String orderMessageJson = objectMapper.writeValueAsString(orderMessage);

            // Send the JSON message to the order queue
            channel.basicPublish("", "order.queue", null, orderMessageJson.getBytes());
            System.out.println("Main: Published to Order Queue: " + orderMessageJson);

            // Close the channel
            channel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
