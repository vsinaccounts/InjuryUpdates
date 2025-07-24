# 🚀 Digital Ocean Deployment Guide

This guide will help you deploy your Injury Feed Service to Digital Ocean so it runs 24/7 and automatically ingests injury updates.

## 📋 Prerequisites

1. **Digital Ocean Account** - [Sign up here](https://www.digitalocean.com/)
2. **GitHub Account** - For automatic deployments
3. **Domain name** (optional) - For custom URL

## 🔧 Step 1: Prepare Your Repository

### 1.1 Create GitHub Repository
```bash
# Initialize git if not already done
git init
git add .
git commit -m "Initial commit: Injury Feed Service"

# Create repository on GitHub and push
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/InjuryUpdates.git
git push -u origin main
```

### 1.2 Update App Configuration
Edit `.do/app.yaml` and replace `YOUR_GITHUB_USERNAME` with your actual GitHub username.

## 🌊 Step 2: Deploy to Digital Ocean

### 2.1 Using Digital Ocean App Platform (Recommended)

1. **Login to Digital Ocean**
   - Go to [Digital Ocean Console](https://cloud.digitalocean.com/)
   - Navigate to "Apps" in the left sidebar

2. **Create New App**
   - Click "Create App"
   - Choose "GitHub" as source
   - Connect your GitHub account
   - Select your `InjuryUpdates` repository
   - Choose `main` branch

3. **Configure App**
   - Digital Ocean will auto-detect the `app.yaml` file
   - Review the configuration:
     - **Service Name**: `injury-feed-service`
     - **Plan**: Basic ($5/month)
     - **Build Command**: Auto-detected from Dockerfile
     - **HTTP Port**: 8080

4. **Environment Variables**
   The following are automatically set from `app.yaml`:
   - `SPRING_PROFILES_ACTIVE=production`
   - `SERVER_PORT=8080`
   - `JAVA_OPTS=-Xmx512m -Xms256m`

5. **Deploy**
   - Click "Create Resources"
   - Wait 5-10 minutes for deployment
   - Your app will be available at: `https://injury-feed-service-xxxxx.ondigitalocean.app`

### 2.2 Using DigitalOcean CLI (Alternative)
```bash
# Install doctl
brew install doctl  # macOS
# or download from https://github.com/digitalocean/doctl

# Authenticate
doctl auth init

# Deploy app
doctl apps create .do/app.yaml
```

## 🔍 Step 3: Verify Deployment

### 3.1 Check App Status
```bash
# Get your app URL from Digital Ocean console or:
curl https://your-app-url.ondigitalocean.app/api/injury-updates/health
```

### 3.2 Test API Endpoints
```bash
# Health check
curl https://your-app-url.ondigitalocean.app/api/injury-updates/health

# Statistics
curl https://your-app-url.ondigitalocean.app/api/injury-updates/stats

# Recent updates
curl https://your-app-url.ondigitalocean.app/api/injury-updates/recent
```

## 🌐 Step 4: Custom Domain (Optional)

1. **Add Domain in Digital Ocean**
   - Go to "Networking" → "Domains"
   - Add your domain name
   - Point it to your app

2. **Update DNS**
   - Add CNAME record: `api.yourdomain.com` → `your-app-url.ondigitalocean.app`

## 🔄 Step 5: Enable Auto-Deployment

Your app is now configured for automatic deployment:
- **Every push to `main` branch** triggers a new deployment
- **Zero-downtime deployments** with health checks
- **Automatic rollback** if deployment fails

## 💻 Step 6: Frontend Integration

### 6.1 Update Your Website's JavaScript

Replace the demo URL with your production URL:

```javascript
// Your production API URL
const API_BASE_URL = 'https://your-app-url.ondigitalocean.app/api/injury-updates';

// Fetch recent injury updates
async function getInjuryUpdates() {
    try {
        const response = await fetch(`${API_BASE_URL}/recent?hours=24`);
        const updates = await response.json();
        return updates;
    } catch (error) {
        console.error('Error fetching injury updates:', error);
        return [];
    }
}

// Example: Display injuries on your website
async function displayInjuries() {
    const injuries = await getInjuryUpdates();
    const container = document.getElementById('injury-updates');
    
    if (injuries.length === 0) {
        container.innerHTML = '<p>No recent injury updates</p>';
        return;
    }
    
    container.innerHTML = injuries.map(injury => `
        <div class="injury-card">
            <h3>${injury.playerName}</h3>
            <p><strong>Team:</strong> ${injury.team}</p>
            <p><strong>Status:</strong> ${injury.status}</p>
            <p><strong>Injury:</strong> ${injury.injuryType}</p>
            <p><strong>League:</strong> ${injury.league}</p>
            <small>Updated: ${new Date(injury.receivedAt).toLocaleString()}</small>
        </div>
    `).join('');
}

// Auto-refresh every 30 seconds
setInterval(displayInjuries, 30000);
displayInjuries(); // Initial load
```

### 6.2 CORS Configuration
Your API is already configured with CORS enabled for all origins. For production, you might want to restrict this to your domain.

## 📊 Step 7: Monitoring & Scaling

### 7.1 Built-in Monitoring
- **App Platform Dashboard**: View metrics, logs, and performance
- **Health Checks**: Automatic monitoring of `/api/injury-updates/health`
- **Alerts**: CPU/Memory usage alerts at 80%

### 7.2 View Logs
```bash
# Using doctl
doctl apps logs YOUR_APP_ID --type=build
doctl apps logs YOUR_APP_ID --type=deploy
doctl apps logs YOUR_APP_ID --type=run

# Or view in Digital Ocean console
```

### 7.3 Scaling
- **Vertical Scaling**: Upgrade to larger instance sizes
- **Horizontal Scaling**: Increase instance count (paid plans)

## 💰 Costs

**Digital Ocean App Platform Pricing:**
- **Basic Plan**: $5/month - Perfect for this application
- **Professional**: $12/month - For higher traffic
- **Includes**: 512MB RAM, shared CPU, 1 instance

## 🔧 Troubleshooting

### Common Issues:

1. **RabbitMQ Connection Errors**
   - Check credentials in `InjuryUpdateService.java`
   - Verify SSL certificate issues (logs will show details)

2. **Deployment Failures**
   - Check build logs in Digital Ocean console
   - Verify `Dockerfile.production` syntax

3. **Memory Issues**
   - Increase instance size in `app.yaml`
   - Adjust `JAVA_OPTS` memory settings

### Debug Commands:
```bash
# Check app status
doctl apps list

# View deployment logs
doctl apps logs YOUR_APP_ID --type=deploy --follow

# Scale app
doctl apps update YOUR_APP_ID --spec=.do/app.yaml
```

## 🎉 Success!

Your injury feed service is now:
- ✅ **Running 24/7** on Digital Ocean
- ✅ **Auto-deploying** on every code push
- ✅ **Ingesting injury data** from SpankOdds RabbitMQ
- ✅ **Serving REST API** for your website
- ✅ **Monitoring & alerting** enabled

**Next Steps:**
1. Build your frontend integration using the API
2. Add more sophisticated injury filtering/search
3. Set up custom domain for professional URL
4. Consider adding email/webhook notifications for critical injuries

Your injury feed is now production-ready! 🏥⚡ 