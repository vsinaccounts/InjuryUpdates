#!/bin/bash

echo "🔍 Injury Feed Diagnostics"
echo "=========================="
echo "URL: https://injury-update-qjaik.ondigitalocean.app"
echo ""

API_URL="https://injury-update-qjaik.ondigitalocean.app/api/injury-updates"

echo "1. 📊 Current Statistics:"
curl -s "$API_URL/stats" | python3 -m json.tool 2>/dev/null || curl -s "$API_URL/stats"
echo ""

echo "2. 🏥 Health Status:"
curl -s "$API_URL/health" | python3 -m json.tool 2>/dev/null || curl -s "$API_URL/health"
echo ""

echo "3. 📈 Recent Updates (last 24 hours):"
RECENT=$(curl -s "$API_URL/recent?hours=24")
echo "$RECENT" | python3 -m json.tool 2>/dev/null || echo "$RECENT"
echo ""

echo "4. 🎯 Aaron Judge Search (should show the missing injury):"
curl -s "$API_URL/search?playerName=Aaron+Judge&hoursBack=24" | python3 -m json.tool 2>/dev/null || curl -s "$API_URL/search?playerName=Aaron+Judge&hoursBack=24"
echo ""

echo "5. 📅 All Time Data:"
curl -s "$API_URL?limit=10" | python3 -m json.tool 2>/dev/null || curl -s "$API_URL?limit=10"
echo ""

echo "=========================="
echo "🚨 EXPECTED: Aaron Judge 'NOT IN STARTING LINEUP' from 7:07 AM"
echo "💡 If Aaron Judge is missing, RabbitMQ connection is broken!"
echo "==========================" 