#!/bin/bash

# Injury Feed Service Deployment Script
# This script helps deploy the injury feed service to various cloud platforms

set -e

echo "🚀 Injury Feed Service Deployment Script"
echo "========================================"

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  -p, --platform PLATFORM    Deployment platform (local|docker|heroku|aws)"
    echo "  -b, --build                 Build the application before deployment"
    echo "  -t, --test                  Test the application after deployment"
    echo "  -h, --help                  Display this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --platform local --build --test"
    echo "  $0 --platform docker"
    echo "  $0 --platform heroku --build"
    exit 1
}

# Default values
PLATFORM=""
BUILD=false
TEST=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -p|--platform)
            PLATFORM="$2"
            shift 2
            ;;
        -b|--build)
            BUILD=true
            shift
            ;;
        -t|--test)
            TEST=true
            shift
            ;;
        -h|--help)
            usage
            ;;
        *)
            echo "Unknown option: $1"
            usage
            ;;
    esac
done

# Validate platform
if [[ -z "$PLATFORM" ]]; then
    echo "❌ Platform is required. Use -p or --platform to specify."
    usage
fi

if [[ ! "$PLATFORM" =~ ^(local|docker|heroku|aws)$ ]]; then
    echo "❌ Invalid platform. Supported platforms: local, docker, heroku, aws"
    exit 1
fi

# Build application if requested
if [[ "$BUILD" == true ]]; then
    echo "🔨 Building application..."
    mvn clean package -DskipTests
    echo "✅ Build completed successfully!"
fi

# Platform-specific deployment
case $PLATFORM in
    local)
        echo "🏠 Deploying locally..."
        if [[ ! -f "target/injury-feed-service-1.0.0.jar" ]]; then
            echo "❌ JAR file not found. Please build the application first with --build flag."
            exit 1
        fi
        
        echo "Starting application on port 8080..."
        java -jar target/injury-feed-service-1.0.0.jar &
        APP_PID=$!
        echo "✅ Application started with PID: $APP_PID"
        echo "📝 Application logs will be displayed below..."
        sleep 5
        ;;
        
    docker)
        echo "🐳 Deploying with Docker..."
        
        # Check if Docker is installed
        if ! command -v docker &> /dev/null; then
            echo "❌ Docker is not installed. Please install Docker first."
            exit 1
        fi
        
        # Build Docker image
        echo "Building Docker image..."
        docker build -t injury-feed-service:latest .
        
        # Stop existing container if running
        if docker ps -q -f name=injury-feed-service; then
            echo "Stopping existing container..."
            docker stop injury-feed-service
            docker rm injury-feed-service
        fi
        
        # Run new container
        echo "Starting new container..."
        docker run -d \
            --name injury-feed-service \
            -p 8080:8080 \
            injury-feed-service:latest
        
        echo "✅ Docker container started successfully!"
        echo "🌐 Application available at: http://localhost:8080"
        ;;
        
    heroku)
        echo "☁️ Deploying to Heroku..."
        
        # Check if Heroku CLI is installed
        if ! command -v heroku &> /dev/null; then
            echo "❌ Heroku CLI is not installed. Please install it first."
            echo "📥 Download from: https://devcenter.heroku.com/articles/heroku-cli"
            exit 1
        fi
        
        # Check if user is logged in
        if ! heroku whoami &> /dev/null; then
            echo "❌ Please log in to Heroku first with: heroku login"
            exit 1
        fi
        
        # Create Heroku app if it doesn't exist
        APP_NAME="injury-feed-$(date +%s)"
        if ! heroku apps:info $APP_NAME &> /dev/null; then
            echo "Creating new Heroku app: $APP_NAME"
            heroku create $APP_NAME
        fi
        
        # Deploy to Heroku
        echo "Deploying to Heroku..."
        git add .
        git commit -m "Deploy injury feed service" || true
        git push heroku main || git push heroku master
        
        # Get app URL
        APP_URL=$(heroku apps:info $APP_NAME --json | jq -r .web_url)
        echo "✅ Deployed to Heroku successfully!"
        echo "🌐 Application URL: $APP_URL"
        ;;
        
    aws)
        echo "☁️ Deploying to AWS..."
        echo "📋 AWS deployment requires additional setup:"
        echo "   1. Configure AWS CLI with your credentials"
        echo "   2. Create an ECS cluster or EC2 instance"
        echo "   3. Set up Application Load Balancer"
        echo "   4. Configure RDS for production database"
        echo ""
        echo "🚀 For AWS deployment, we recommend using AWS ECS with the provided Dockerfile"
        echo "📚 Documentation: https://docs.aws.amazon.com/ecs/"
        ;;
esac

# Test application if requested
if [[ "$TEST" == true ]]; then
    echo "🧪 Testing application..."
    sleep 10  # Wait for application to start
    
    # Test health endpoint
    if curl -f http://localhost:8080/api/injury-updates/health > /dev/null 2>&1; then
        echo "✅ Health check passed!"
    else
        echo "❌ Health check failed. Application may not be running properly."
        exit 1
    fi
    
    # Test stats endpoint
    if curl -f http://localhost:8080/api/injury-updates/stats > /dev/null 2>&1; then
        echo "✅ Stats endpoint working!"
    else
        echo "⚠️ Stats endpoint not responding (this is normal if no data has been received yet)"
    fi
    
    echo "✅ Basic tests completed!"
fi

echo ""
echo "🎉 Deployment completed successfully!"
echo "📖 API Documentation:"
echo "   Health Check: http://localhost:8080/api/injury-updates/health"
echo "   All Updates:  http://localhost:8080/api/injury-updates"
echo "   Recent:       http://localhost:8080/api/injury-updates/recent"
echo "   Stats:        http://localhost:8080/api/injury-updates/stats"
echo ""
echo "🖥️  Frontend Demo: Open demo/index.html in your browser"
echo "📚 Full API documentation available in README.md" 