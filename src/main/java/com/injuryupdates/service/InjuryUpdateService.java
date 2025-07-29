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
import java.util.ArrayList;

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
    
    // Connection status tracking
    private boolean isConnected = false;
    private String connectionMethod = "NONE";
    private LocalDateTime lastConnectionAttempt;
    private List<String> connectionErrors = new ArrayList<>();
    
    @PostConstruct
    public void initializeRabbitMQConnection() {
        lastConnectionAttempt = LocalDateTime.now();
        connectionErrors.clear();
        
        // Use the same simple connection method as the working standalone Java apps
        if (connectDirectly()) {
            logger.info("✅ Connected to RabbitMQ using direct connection (same as standalone apps)");
            isConnected = true;
            connectionMethod = "DIRECT";
            return;
        }
        
        logger.error("❌ Failed to connect to RabbitMQ with direct method");
        logger.info("💡 Application will still serve API endpoints with stored data");
        isConnected = false;
        connectionMethod = "FAILED";
    }
    
    private boolean connectDirectly() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RABBITMQ_HOST);
            factory.setPort(RABBITMQ_PORT); // Use 5671 same as standalone apps
            factory.setUsername(USERNAME);
            factory.setPassword(PASSWORD);
            factory.setVirtualHost(VIRTUAL_HOST);
            
            // Same simple approach as standalone Java apps - no SSL configuration
            // Set reasonable timeouts
            factory.setConnectionTimeout(30000);
            factory.setHandshakeTimeout(30000);
            factory.setShutdownTimeout(5000);
            
            // Enable automatic recovery
            factory.setAutomaticRecoveryEnabled(true);
            factory.setNetworkRecoveryInterval(5000);
            
            connection = factory.newConnection();
            channel = connection.createChannel();
            
            logger.info("📡 Listening for messages on queue: {} (using same method as standalone apps)", QUEUE_NAME);
            startConsuming();
            return true;
            
        } catch (Exception e) {
            String error = "Direct connection failed: " + e.getMessage();
            logger.error(error, e);
            connectionErrors.add(error);
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

    // Public methods for status checking
    public boolean isRabbitMQConnected() {
        return isConnected && connection != null && connection.isOpen();
    }
    
    public String getConnectionMethod() {
        return connectionMethod;
    }
    
    public LocalDateTime getLastConnectionAttempt() {
        return lastConnectionAttempt;
    }
    
    public List<String> getConnectionErrors() {
        return new ArrayList<>(connectionErrors);
    }
} 