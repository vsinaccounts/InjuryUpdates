package com.injuryupdates.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Entity
@Table(name = "injury_updates")
public class InjuryUpdate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonProperty("Status")
    private String status;
    
    @JsonProperty("Injury Type")
    @Column(name = "injury_type")
    private String injuryType;
    
    @JsonProperty("Position")
    private String position;
    
    @JsonProperty("Team")
    private String team;
    
    @JsonProperty("Rotation")
    private Integer rotation;
    
    @JsonProperty("Player Name")
    @Column(name = "player_name")
    private String playerName;
    
    @JsonProperty("League")
    private String league;
    
    @Column(name = "received_at")
    private LocalDateTime receivedAt;
    
    // Default constructor
    public InjuryUpdate() {
        this.receivedAt = LocalDateTime.now();
    }
    
    // Constructor with all fields
    public InjuryUpdate(String status, String injuryType, String position, String team, 
                       Integer rotation, String playerName, String league) {
        this.status = status;
        this.injuryType = injuryType;
        this.position = position;
        this.team = team;
        this.rotation = rotation;
        this.playerName = playerName;
        this.league = league;
        this.receivedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getInjuryType() {
        return injuryType;
    }
    
    public void setInjuryType(String injuryType) {
        this.injuryType = injuryType;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public String getTeam() {
        return team;
    }
    
    public void setTeam(String team) {
        this.team = team;
    }
    
    public Integer getRotation() {
        return rotation;
    }
    
    public void setRotation(Integer rotation) {
        this.rotation = rotation;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public String getLeague() {
        return league;
    }
    
    public void setLeague(String league) {
        this.league = league;
    }
    
    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
    
    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
    
    @Override
    public String toString() {
        return "InjuryUpdate{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", injuryType='" + injuryType + '\'' +
                ", position='" + position + '\'' +
                ", team='" + team + '\'' +
                ", rotation=" + rotation +
                ", playerName='" + playerName + '\'' +
                ", league='" + league + '\'' +
                ", receivedAt=" + receivedAt +
                '}';
    }
} 