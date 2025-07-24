package com.injuryupdates.repository;

import com.injuryupdates.model.InjuryUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InjuryUpdateRepository extends JpaRepository<InjuryUpdate, Long> {
    
    // Find all injury updates ordered by most recent first
    List<InjuryUpdate> findAllByOrderByReceivedAtDesc();
    
    // Find injury updates by league
    List<InjuryUpdate> findByLeagueOrderByReceivedAtDesc(String league);
    
    // Find injury updates by team
    List<InjuryUpdate> findByTeamOrderByReceivedAtDesc(String team);
    
    // Find injury updates by player name
    List<InjuryUpdate> findByPlayerNameOrderByReceivedAtDesc(String playerName);
    
    // Find injury updates by status
    List<InjuryUpdate> findByStatusOrderByReceivedAtDesc(String status);
    
    // Find injury updates received after a certain time
    List<InjuryUpdate> findByReceivedAtAfterOrderByReceivedAtDesc(LocalDateTime dateTime);
    
    // Find injury updates by rotation number
    List<InjuryUpdate> findByRotationOrderByReceivedAtDesc(Integer rotation);
    
    // Custom query to find recent updates for a specific league and team
    @Query("SELECT i FROM InjuryUpdate i WHERE i.league = :league AND i.team = :team ORDER BY i.receivedAt DESC")
    List<InjuryUpdate> findRecentUpdatesByLeagueAndTeam(@Param("league") String league, @Param("team") String team);
    
    // Get the latest update for each player
    @Query("SELECT i FROM InjuryUpdate i WHERE i.receivedAt = (SELECT MAX(i2.receivedAt) FROM InjuryUpdate i2 WHERE i2.playerName = i.playerName) ORDER BY i.receivedAt DESC")
    List<InjuryUpdate> findLatestUpdateForEachPlayer();
} 