// InjuryFeed.jsx - React component for displaying injury updates
import React, { useState, useEffect } from 'react';
import './InjuryFeed.css'; // Add your CSS styling

const InjuryFeed = () => {
  const [injuries, setInjuries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Replace with your actual Digital Ocean app URL
  const API_BASE_URL = 'https://injury-update-qjaik.ondigitalocean.app/api/injury-updates';

  const fetchInjuries = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await fetch(`${API_BASE_URL}/recent?hours=24`);
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      const data = await response.json();
      setInjuries(data);
    } catch (err) {
      console.error('Error fetching injuries:', err);
      setError('Unable to load injury updates. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInjuries();
    
    // Auto-refresh every 2 minutes
    const interval = setInterval(fetchInjuries, 120000);
    
    return () => clearInterval(interval);
  }, []);

  const getStatusClass = (status) => {
    const lowerStatus = status.toLowerCase();
    if (lowerStatus.includes('out')) return 'status-out';
    if (lowerStatus.includes('doubtful')) return 'status-doubtful';
    if (lowerStatus.includes('questionable')) return 'status-questionable';
    return 'status-default';
  };

  const getTimeAgo = (timestamp) => {
    const now = new Date();
    const then = new Date(timestamp);
    const diffMs = now - then;
    const diffMins = Math.round(diffMs / 60000);
    
    if (diffMins < 60) return `${diffMins} minutes ago`;
    if (diffMins < 1440) return `${Math.round(diffMins / 60)} hours ago`;
    return `${Math.round(diffMins / 1440)} days ago`;
  };

  if (loading) {
    return (
      <div className="injury-feed">
        <h2>🏥 Latest Injury Updates</h2>
        <div className="loading">Loading injury updates...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="injury-feed">
        <h2>🏥 Latest Injury Updates</h2>
        <div className="error">
          {error}
          <button onClick={fetchInjuries} className="retry-btn">
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="injury-feed">
      <div className="injury-header">
        <h2>🏥 Latest Injury Updates</h2>
        <button onClick={fetchInjuries} className="refresh-btn">
          🔄 Refresh
        </button>
      </div>
      
      {injuries.length === 0 ? (
        <div className="no-injuries">
          No recent injury updates in the last 24 hours.
        </div>
      ) : (
        <div className="injury-list">
          {injuries.map((injury) => (
            <div key={injury.id} className="injury-card">
              <div className="injury-card-header">
                <h3>{injury['Player Name']}</h3>
                <span className={`injury-status ${getStatusClass(injury['Status'])}`}>
                  {injury['Status']}
                </span>
              </div>
              
              <div className="injury-details">
                <div className="detail-row">
                  <span className="label">Team:</span>
                  <span className="value">{injury['Team']} ({injury['League']})</span>
                </div>
                <div className="detail-row">
                  <span className="label">Position:</span>
                  <span className="value">{injury['Position']}</span>
                </div>
                <div className="detail-row">
                  <span className="label">Injury:</span>
                  <span className="value">{injury['Injury Type']}</span>
                </div>
                {injury['Rotation'] && (
                  <div className="detail-row">
                    <span className="label">Rotation:</span>
                    <span className="value">{injury['Rotation']}</span>
                  </div>
                )}
              </div>
              
              <div className="injury-timestamp">
                Updated {getTimeAgo(injury.receivedAt)}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default InjuryFeed;

/* 
// Usage in your React app:

import InjuryFeed from './components/InjuryFeed';

function App() {
  return (
    <div className="App">
      <header>
        <h1>My Sports Website</h1>
      </header>
      
      <main>
        <InjuryFeed />
      </main>
    </div>
  );
}
*/ 