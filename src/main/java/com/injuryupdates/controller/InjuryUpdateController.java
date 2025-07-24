package com.injuryupdates.controller;

import com.injuryupdates.model.InjuryUpdate;
import com.injuryupdates.service.InjuryUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/injury-updates")
@CrossOrigin(origins = "*") // Allow CORS for frontend integration
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
        List<InjuryUpdate> updates = injuryUpdateService.getInjuryUpdatesByLeague(league);
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
        List<InjuryUpdate> updates = injuryUpdateService.getRecentInjuryUpdates(hours);
        return ResponseEntity.ok(updates);
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
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUpdates", injuryUpdateService.getTotalUpdateCount());
        stats.put("recentUpdates24h", injuryUpdateService.getRecentInjuryUpdates(24).size());
        stats.put("recentUpdates1h", injuryUpdateService.getRecentInjuryUpdates(1).size());
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Injury Update API");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Search injury updates by multiple criteria
     */
    @GetMapping("/search")
    public ResponseEntity<List<InjuryUpdate>> searchInjuryUpdates(
            @RequestParam(required = false) String league,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String playerName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer rotation,
            @RequestParam(defaultValue = "24") int hoursBack) {
        
        List<InjuryUpdate> allUpdates = injuryUpdateService.getRecentInjuryUpdates(hoursBack);
        
        // Filter based on provided parameters
        List<InjuryUpdate> filteredUpdates = allUpdates.stream()
                .filter(update -> league == null || league.equalsIgnoreCase(update.getLeague()))
                .filter(update -> team == null || team.equalsIgnoreCase(update.getTeam()))
                .filter(update -> playerName == null || update.getPlayerName().toLowerCase().contains(playerName.toLowerCase()))
                .filter(update -> status == null || status.equalsIgnoreCase(update.getStatus()))
                .filter(update -> rotation == null || rotation.equals(update.getRotation()))
                .toList();
        
        return ResponseEntity.ok(filteredUpdates);
    }
} 