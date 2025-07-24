import com.rabbitmq.client.*;

public class InjurySubscriber {
	    private static final String username = "VSiN"; // Enter your username here
    private static final String password = "830e10bcd72f798de67e"; // Enter your password here
    
    private static final String QUEUE_NAME = username + ".InjuryQueue"; // Use the pre-defined queue name here

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("ec2-3-223-33-62.compute-1.amazonaws.com");
        factory.setPort(5671);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost("InjuryFeed");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        System.out.println(" [*] Waiting for messages in queue: " + QUEUE_NAME + ". To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}
