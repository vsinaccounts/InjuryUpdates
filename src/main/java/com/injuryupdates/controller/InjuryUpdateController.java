package com.injuryupdates.controller;

import com.injuryupdates.model.InjuryUpdate;
import com.injuryupdates.service.InjuryUpdateService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/injury-updates")
@CrossOrigin(origins = "*")
public class InjuryUpdateController {

    @Autowired
    private InjuryUpdateService injuryUpdateService;

    /**
     * Get all injury updates
     */
    @GetMapping
    public ResponseEntity<List<InjuryUpdate>> getAllInjuryUpdates() {
        List<InjuryUpdate> updates = injuryUpdateService.getAllInjuryUpdates();
        return ResponseEntity.ok(updates);
    }

    /**
     * Get injury updates by league
     */
    @GetMapping("/league/{league}")
    public ResponseEntity<List<InjuryUpdate>> getInjuryUpdatesByLeague(@PathVariable String league) {
        List<InjuryUpdate> updates = injuryUpdateService.getInjuryUpdatesByLeague(league.toUpperCase());
        return ResponseEntity.ok(updates);
    }

    /**
     * Get injury updates by team
     */
    @GetMapping("/team/{team}")
    public ResponseEntity<List<InjuryUpdate>> getInjuryUpdatesByTeam(@PathVariable String team) {
        List<InjuryUpdate> updates = injuryUpdateService.getInjuryUpdatesByTeam(team);
        return ResponseEntity.ok(updates);
    }

    /**
     * Get injury updates by player name
     */
    @GetMapping("/player/{playerName}")
    public ResponseEntity<List<InjuryUpdate>> getInjuryUpdatesByPlayer(@PathVariable String playerName) {
        List<InjuryUpdate> updates = injuryUpdateService.getInjuryUpdatesByPlayer(playerName);
        return ResponseEntity.ok(updates);
    }

    /**
     * Get injury updates by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<InjuryUpdate>> getInjuryUpdatesByStatus(@PathVariable String status) {
        List<InjuryUpdate> updates = injuryUpdateService.getInjuryUpdatesByStatus(status);
        return ResponseEntity.ok(updates);
    }

    /**
     * Get recent injury updates (last X hours)
     */
    @GetMapping("/recent")
    public ResponseEntity<List<InjuryUpdate>> getRecentInjuryUpdates(
            @RequestParam(defaultValue = "24") int hours) {
        List<InjuryUpdate> recentUpdates = injuryUpdateService.getRecentInjuryUpdates(hours);
        return ResponseEntity.ok(recentUpdates);
    }

    /**
     * Get injury updates by rotation number
     */
    @GetMapping("/rotation/{rotation}")
    public ResponseEntity<List<InjuryUpdate>> getInjuryUpdatesByRotation(@PathVariable Integer rotation) {
        List<InjuryUpdate> updates = injuryUpdateService.getInjuryUpdatesByRotation(rotation);
        return ResponseEntity.ok(updates);
    }

    /**
     * Get the latest injury update for each player
     */
    @GetMapping("/latest-per-player")
    public ResponseEntity<List<InjuryUpdate>> getLatestUpdateForEachPlayer() {
        List<InjuryUpdate> updates = injuryUpdateService.getLatestUpdateForEachPlayer();
        return ResponseEntity.ok(updates);
    }

    /**
     * Get injury updates by league and team
     */
    @GetMapping("/league/{league}/team/{team}")
    public ResponseEntity<List<InjuryUpdate>> getUpdatesByLeagueAndTeam(
            @PathVariable String league,
            @PathVariable String team) {
        List<InjuryUpdate> updates = injuryUpdateService.getUpdatesByLeagueAndTeam(league, team);
        return ResponseEntity.ok(updates);
    }

    /**
     * Get statistics about injury updates
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUpdates", injuryUpdateService.getTotalUpdateCount());
        stats.put("recentUpdates", injuryUpdateService.getRecentInjuryUpdates(24).size());
        stats.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(stats);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "Injury Update API");
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        
        // Add RabbitMQ connection status
        boolean rabbitConnected = injuryUpdateService.isRabbitMQConnected();
        health.put("rabbitMQ", rabbitConnected ? "CONNECTED" : "DISCONNECTED");
        health.put("connectionMethod", injuryUpdateService.getConnectionMethod());
        
        return ResponseEntity.ok(health);
    }

    /**
     * RabbitMQ connection diagnostics
     */
    @GetMapping("/rabbitmq-status")
    public ResponseEntity<Map<String, Object>> getRabbitMQStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("connected", injuryUpdateService.isRabbitMQConnected());
        status.put("connectionMethod", injuryUpdateService.getConnectionMethod());
        status.put("lastConnectionAttempt", injuryUpdateService.getLastConnectionAttempt());
        status.put("connectionErrors", injuryUpdateService.getConnectionErrors());
        status.put("messagesReceived", injuryUpdateService.getTotalUpdateCount());
        status.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(status);
    }

    /**
     * Search injury updates by multiple criteria
     */
    @GetMapping("/search")
    public ResponseEntity<List<InjuryUpdate>> searchInjuryUpdates(
            @RequestParam(required = false) String player,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String league,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer rotation,
            @RequestParam(defaultValue = "24") int hoursBack) {
        
        // If specific criteria provided, use them
        if (player != null && !player.trim().isEmpty()) {
            return ResponseEntity.ok(injuryUpdateService.getInjuryUpdatesByPlayer(player));
        }
        if (team != null && !team.trim().isEmpty()) {
            return ResponseEntity.ok(injuryUpdateService.getInjuryUpdatesByTeam(team));
        }
        if (league != null && !league.trim().isEmpty()) {
            return ResponseEntity.ok(injuryUpdateService.getInjuryUpdatesByLeague(league));
        }
        
        // Otherwise return recent updates
        return ResponseEntity.ok(injuryUpdateService.getRecentInjuryUpdates(hoursBack));
    }
}

// Add a separate controller for the root path
@Controller
class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "index.html";
    }
} 