#!/bin/bash

echo "🏥 Injury Feed Monitor"
echo "====================="

API_URL="https://injury-update-qjaik.ondigitalocean.app/api/injury-updates"

while true; do
    echo "$(date): Checking for injuries..."
    
    # Get stats
    STATS=$(curl -s "$API_URL/stats")
    echo "📊 Stats: $STATS"
    
    # Extract total updates count
    TOTAL=$(echo "$STATS" | grep -o '"totalUpdates":[0-9]*' | cut -d':' -f2)
    
    if [ "$TOTAL" -gt 0 ]; then
        echo "🚨 INJURY FOUND! Total updates: $TOTAL"
        echo "📋 Recent injuries:"
        curl -s "$API_URL/recent?hours=1" | python3 -m json.tool 2>/dev/null || curl -s "$API_URL/recent?hours=1"
        break
    else
        echo "⏳ No injuries yet (Total: $TOTAL)"
    fi
    
    echo "---"
    sleep 30  # Check every 30 seconds
done 