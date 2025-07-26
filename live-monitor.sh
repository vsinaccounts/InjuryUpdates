#!/bin/bash

echo "🔄 Live Injury Feed Monitor"
echo "==========================="
echo "Monitoring RabbitMQ connection and incoming messages..."
echo "Press Ctrl+C to stop"
echo ""

API_URL="https://injury-update-qjaik.ondigitalocean.app/api/injury-updates"
PREV_TOTAL=0

while true; do
    # Get current stats
    STATS=$(curl -s "$API_URL/stats" 2>/dev/null)
    RABBIT_STATUS=$(curl -s "$API_URL/rabbitmq-status" 2>/dev/null)
    
    # Extract totals
    CURRENT_TOTAL=$(echo "$STATS" | grep -o '"totalUpdates":[0-9]*' | cut -d':' -f2)
    CONNECTED=$(echo "$RABBIT_STATUS" | grep -o '"connected":[^,]*' | cut -d':' -f2)
    
    # Display current status
    echo "$(date '+%H:%M:%S'): Total=$CURRENT_TOTAL | RabbitMQ=$CONNECTED"
    
    # Check for new messages
    if [ "$CURRENT_TOTAL" -gt "$PREV_TOTAL" ]; then
        echo "🚨 NEW INJURY UPDATE DETECTED! Total changed from $PREV_TOTAL to $CURRENT_TOTAL"
        echo "📋 Recent updates:"
        curl -s "$API_URL/recent?hours=1" | python3 -m json.tool 2>/dev/null
        echo "---"
        PREV_TOTAL=$CURRENT_TOTAL
    fi
    
    sleep 10  # Check every 10 seconds
done 