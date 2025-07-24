import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class TestRabbitMQConnection {
    private static final String RABBITMQ_HOST = "ec2-3-223-33-62.compute-1.amazonaws.com";
    private static final int RABBITMQ_PORT = 5671;
    private static final String USERNAME = "VSiN";
    private static final String PASSWORD = "830e10bcd72f798de67e";
    private static final String VIRTUAL_HOST = "InjuryFeed";
    private static final String QUEUE_NAME = USERNAME + ".InjuryQueue";

    public static void main(String[] args) {
        System.out.println("Testing RabbitMQ Connection...");
        System.out.println("Host: " + RABBITMQ_HOST);
        System.out.println("Port: " + RABBITMQ_PORT);
        System.out.println("Username: " + USERNAME);
        System.out.println("Virtual Host: " + VIRTUAL_HOST);
        System.out.println("Queue Name: " + QUEUE_NAME);
        System.out.println("----------------------------------------");

        Connection connection = null;
        Channel channel = null;

        try {
            // Create connection factory
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RABBITMQ_HOST);
            factory.setPort(RABBITMQ_PORT);
            factory.setUsername(USERNAME);
            factory.setPassword(PASSWORD);
            factory.setVirtualHost(VIRTUAL_HOST);
            
            // Enable SSL/TLS
            factory.useSslProtocol();
            
            // Set connection timeout
            factory.setConnectionTimeout(10000); // 10 seconds
            factory.setHandshakeTimeout(10000); // 10 seconds

            System.out.println("Attempting to connect...");
            connection = factory.newConnection();
            System.out.println("✅ Successfully connected to RabbitMQ!");

            channel = connection.createChannel();
            System.out.println("✅ Channel created successfully!");

            System.out.println("📡 Starting to listen for messages on queue: " + QUEUE_NAME);
            System.out.println("Press Ctrl+C to stop...");

            // Set up consumer
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("📨 [" + java.time.LocalDateTime.now() + "] Received: " + message);
            };

            CancelCallback cancelCallback = consumerTag -> {
                System.out.println("❌ Consumer was cancelled: " + consumerTag);
            };

            channel.basicConsume(QUEUE_NAME, true, deliverCallback, cancelCallback);

            // Keep the program running
            while (true) {
                Thread.sleep(1000);
            }

        } catch (IOException e) {
            System.err.println("❌ IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.err.println("❌ Connection timeout: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("🛑 Program interrupted");
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (channel != null && channel.isOpen()) {
                    channel.close();
                    System.out.println("📄 Channel closed");
                }
                if (connection != null && connection.isOpen()) {
                    connection.close();
                    System.out.println("🔌 Connection closed");
                }
            } catch (Exception e) {
                System.err.println("❌ Error closing connection: " + e.getMessage());
            }
        }
    }
} 