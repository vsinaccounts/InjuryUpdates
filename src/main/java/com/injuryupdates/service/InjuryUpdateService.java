package com.injuryupdates.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.injuryupdates.model.InjuryUpdate;
import com.injuryupdates.repository.InjuryUpdateRepository;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InjuryUpdateService {
    
    private static final Logger logger = LoggerFactory.getLogger(InjuryUpdateService.class);
    
    private static final String RABBITMQ_HOST = "ec2-3-223-33-62.compute-1.amazonaws.com";
    private static final int RABBITMQ_PORT = 5671;
    private static final String USERNAME = "VSiN";
    private static final String PASSWORD = "830e10bcd72f798de67e";
    private static final String VIRTUAL_HOST = "InjuryFeed";
    private static final String QUEUE_NAME = USERNAME + ".InjuryQueue";
    
    @Autowired
    private InjuryUpdateRepository repository;
    
    private Connection connection;
    private Channel channel;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostConstruct
    public void initializeRabbitMQConnection() {
        // Try multiple connection strategies
        if (connectWithSsl()) {
            logger.info("✅ Connected to RabbitMQ using SSL");
            return;
        }
        
        if (connectWithPlainConnection()) {
            logger.info("✅ Connected to RabbitMQ using plain connection");
            return;
        }
        
        if (connectToSslPortWithoutSsl()) {
            logger.info("✅ Connected to RabbitMQ using SSL port without SSL");
            return;
        }
        
        logger.error("❌ Failed to connect to RabbitMQ with all methods");
        logger.info("💡 Application will still serve API endpoints with stored data");
    }
    
    private boolean connectWithSsl() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RABBITMQ_HOST);
            factory.setPort(RABBITMQ_PORT);
            factory.setUsername(USERNAME);
            factory.setPassword(PASSWORD);
            factory.setVirtualHost(VIRTUAL_HOST);
            
            // Configure SSL properly
            factory.useSslProtocol("TLSv1.2");
            
            // Set connection timeouts
            factory.setConnectionTimeout(10000);
            factory.setHandshakeTimeout(10000);
            factory.setShutdownTimeout(5000);
            
            // Enable automatic recovery
            factory.setAutomaticRecoveryEnabled(true);
            factory.setNetworkRecoveryInterval(5000);
            
            connection = factory.newConnection();
            channel = connection.createChannel();
            
            logger.info("📡 Listening for messages on queue: {}", QUEUE_NAME);
            startConsuming();
            return true;
            
        } catch (Exception e) {
            logger.warn("SSL connection failed: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean connectWithPlainConnection() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RABBITMQ_HOST);
            factory.setPort(5672); // Try standard AMQP port
            factory.setUsername(USERNAME);
            factory.setPassword(PASSWORD);
            factory.setVirtualHost(VIRTUAL_HOST);
            
            // Set connection timeouts
            factory.setConnectionTimeout(10000);
            factory.setHandshakeTimeout(10000);
            factory.setShutdownTimeout(5000);
            
            // Enable automatic recovery
            factory.setAutomaticRecoveryEnabled(true);
            factory.setNetworkRecoveryInterval(5000);
            
            connection = factory.newConnection();
            channel = connection.createChannel();
            
            logger.info("📡 Listening for messages on queue: {}", QUEUE_NAME);
            startConsuming();
            return true;
            
        } catch (Exception e) {
            logger.warn("Plain connection failed: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean connectToSslPortWithoutSsl() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RABBITMQ_HOST);
            factory.setPort(RABBITMQ_PORT); // Use original SSL port but without SSL
            factory.setUsername(USERNAME);
            factory.setPassword(PASSWORD);
            factory.setVirtualHost(VIRTUAL_HOST);
            
            // Set connection timeouts
            factory.setConnectionTimeout(10000);
            factory.setHandshakeTimeout(10000);
            factory.setShutdownTimeout(5000);
            
            // Enable automatic recovery
            factory.setAutomaticRecoveryEnabled(true);
            factory.setNetworkRecoveryInterval(5000);
            
            connection = factory.newConnection();
            channel = connection.createChannel();
            
            logger.info("📡 Listening for messages on queue: {}", QUEUE_NAME);
            startConsuming();
            return true;
            
        } catch (Exception e) {
            logger.warn("SSL port without SSL failed: {}", e.getMessage());
            return false;
        }
    }
    
    private void startConsuming() {
        try {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                try {
                    String message = new String(delivery.getBody(), "UTF-8");
                    logger.info("Received injury update: {}", message);
                    
                    // Parse JSON and save to database
                    InjuryUpdate injuryUpdate = objectMapper.readValue(message, InjuryUpdate.class);
                    InjuryUpdate savedUpdate = repository.save(injuryUpdate);
                    
                    logger.info("Saved injury update to database with ID: {}", savedUpdate.getId());
                    
                } catch (Exception e) {
                    logger.error("Error processing injury update message: ", e);
                }
            };
            
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
                logger.info("Consumer cancelled: {}", consumerTag);
            });
            
        } catch (Exception e) {
            logger.error("Failed to start consuming messages: ", e);
        }
    }
    
    @PreDestroy
    public void closeConnection() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
            logger.info("RabbitMQ connection closed");
        } catch (Exception e) {
            logger.error("Error closing RabbitMQ connection: ", e);
        }
    }
    
    // Service methods for retrieving data
    public List<InjuryUpdate> getAllInjuryUpdates() {
        return repository.findAllByOrderByReceivedAtDesc();
    }
    
    public List<InjuryUpdate> getInjuryUpdatesByLeague(String league) {
        return repository.findByLeagueOrderByReceivedAtDesc(league);
    }
    
    public List<InjuryUpdate> getInjuryUpdatesByTeam(String team) {
        return repository.findByTeamOrderByReceivedAtDesc(team);
    }
    
    public List<InjuryUpdate> getInjuryUpdatesByPlayer(String playerName) {
        return repository.findByPlayerNameOrderByReceivedAtDesc(playerName);
    }
    
    public List<InjuryUpdate> getInjuryUpdatesByStatus(String status) {
        return repository.findByStatusOrderByReceivedAtDesc(status);
    }
    
    public List<InjuryUpdate> getRecentInjuryUpdates(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        return repository.findByReceivedAtAfterOrderByReceivedAtDesc(cutoffTime);
    }
    
    public List<InjuryUpdate> getInjuryUpdatesByRotation(Integer rotation) {
        return repository.findByRotationOrderByReceivedAtDesc(rotation);
    }
    
    public List<InjuryUpdate> getLatestUpdateForEachPlayer() {
        return repository.findLatestUpdateForEachPlayer();
    }
    
    public List<InjuryUpdate> getUpdatesByLeagueAndTeam(String league, String team) {
        return repository.findRecentUpdatesByLeagueAndTeam(league, team);
    }
    
    public long getTotalUpdateCount() {
        return repository.count();
    }
} 