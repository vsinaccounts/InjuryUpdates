#!/bin/bash

echo "🏥 REAL-TIME INJURY FEED MONITOR"
echo "==============================="
echo "Monitoring both LOCAL and PRODUCTION apps"
echo "Press Ctrl+C to stop"
echo ""

LOCAL_URL="http://localhost:8080/api/injury-updates"
PROD_URL="https://injury-update-qjaik.ondigitalocean.app/api/injury-updates"

# Initialize counters
LOCAL_PREV=0
PROD_PREV=0

while true; do
    # Get current timestamp
    TIMESTAMP=$(date '+%H:%M:%S')
    
    # Check local app
    LOCAL_STATS=$(curl -s "$LOCAL_URL/stats" 2>/dev/null)
    if [ $? -eq 0 ]; then
        LOCAL_TOTAL=$(echo "$LOCAL_STATS" | grep -o '"totalUpdates":[0-9]*' | cut -d':' -f2)
        LOCAL_STATUS="🟢 RUNNING"
    else
        LOCAL_TOTAL=0
        LOCAL_STATUS="🔴 DOWN"
    fi
    
    # Check production app
    PROD_STATS=$(curl -s "$PROD_URL/stats" 2>/dev/null)
    if [ $? -eq 0 ]; then
        PROD_TOTAL=$(echo "$PROD_STATS" | grep -o '"totalUpdates":[0-9]*' | cut -d':' -f2)
        PROD_STATUS="🟢 RUNNING"
    else
        PROD_TOTAL=0
        PROD_STATUS="🔴 DOWN"
    fi
    
    # Display status
    echo "[$TIMESTAMP] LOCAL: $LOCAL_TOTAL injuries $LOCAL_STATUS | PROD: $PROD_TOTAL injuries $PROD_STATUS"
    
    # Check for new injuries on local
    if [ "$LOCAL_TOTAL" -gt "$LOCAL_PREV" ]; then
        echo ""
        echo "🚨 NEW LOCAL INJURY DETECTED! Total: $LOCAL_PREV → $LOCAL_TOTAL"
        echo "📋 Latest local injury:"
        curl -s "$LOCAL_URL/recent?hours=1" | python3 -m json.tool 2>/dev/null | head -20
        echo "---"
        LOCAL_PREV=$LOCAL_TOTAL
    fi
    
    # Check for new injuries on production
    if [ "$PROD_TOTAL" -gt "$PROD_PREV" ]; then
        echo ""
        echo "🚨 NEW PRODUCTION INJURY DETECTED! Total: $PROD_PREV → $PROD_TOTAL"
        echo "📋 Latest production injury:"
        curl -s "$PROD_URL/recent?hours=1" | python3 -m json.tool 2>/dev/null | head -20
        echo "---"
        PROD_PREV=$PROD_TOTAL
    fi
    
    sleep 10  # Check every 10 seconds
done 