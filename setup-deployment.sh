#!/bin/bash

# Setup script for Digital Ocean deployment
set -e

echo "🚀 Setting up Injury Feed Service for Digital Ocean Deployment"
echo "============================================================="

# Check if git is initialized
if [ ! -d ".git" ]; then
    echo "📁 Initializing git repository..."
    git init
    git branch -M main
fi

# Check if GitHub username is provided
if [ -z "$1" ]; then
    echo "❌ Please provide your GitHub username as an argument"
    echo "Usage: $0 YOUR_GITHUB_USERNAME [REPO_NAME]"
    echo "Example: $0 johnsmith InjuryUpdates"
    exit 1
fi

GITHUB_USERNAME=$1
REPO_NAME=${2:-"InjuryUpdates"}

echo "GitHub Username: $GITHUB_USERNAME"
echo "Repository Name: $REPO_NAME"

# Update app.yaml with correct GitHub repo
echo "📝 Updating Digital Ocean app configuration..."
sed -i.bak "s/YOUR_GITHUB_USERNAME/$GITHUB_USERNAME/g" .do/app.yaml
rm .do/app.yaml.bak

# Create .gitignore if it doesn't exist
if [ ! -f ".gitignore" ]; then
    echo "📄 Creating .gitignore..."
    cat > .gitignore << EOF
target/
.mvn/
*.log
.DS_Store
.vscode/
.idea/
*.iml
.env
.env.local
EOF
fi

# Add all files to git
echo "📦 Adding files to git..."
git add .

# Commit changes
if git diff --staged --quiet; then
    echo "ℹ️  No changes to commit"
else
    git commit -m "Initial setup for Digital Ocean deployment"
fi

echo ""
echo "✅ Setup complete! Next steps:"
echo ""
echo "1. 🌐 Create GitHub repository:"
echo "   - Go to https://github.com/new"
echo "   - Repository name: $REPO_NAME"
echo "   - Make it public (required for free Digital Ocean deployments)"
echo "   - Don't initialize with README (we already have files)"
echo ""
echo "2. 📤 Push to GitHub:"
echo "   git remote add origin https://github.com/$GITHUB_USERNAME/$REPO_NAME.git"
echo "   git push -u origin main"
echo ""
echo "3. 🌊 Deploy to Digital Ocean:"
echo "   - Go to https://cloud.digitalocean.com/apps"
echo "   - Click 'Create App'"
echo "   - Connect GitHub and select your $REPO_NAME repository"
echo "   - Digital Ocean will auto-detect the configuration"
echo "   - Click 'Create Resources'"
echo ""
echo "4. 🔗 Update frontend examples:"
echo "   - Replace 'your-app-url.ondigitalocean.app' with your actual app URL"
echo "   - Copy code from frontend-examples/ to your website"
echo ""
echo "💰 Cost: ~$5/month for basic Digital Ocean App Platform"
echo "📚 Full guide: See DEPLOYMENT.md for detailed instructions"
echo ""
echo "🎉 Your injury feed will be live at: https://injury-feed-service-xxxxx.ondigitalocean.app" 