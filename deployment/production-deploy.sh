#!/bin/bash

# 🚀 RIO Platform Production Deployment Script
# Automated deployment for rural farmer platform

set -e  # Exit on any error

echo "🚀 Starting RIO Platform Production Deployment..."

# Configuration
PROJECT_ID="rio-platform-prod"
REGION="asia-south1"
APP_VERSION=$(date +%Y%m%d-%H%M%S)
BACKUP_BUCKET="rio-platform-backups"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Pre-deployment checks
pre_deployment_checks() {
    log_info "Running pre-deployment checks..."
    
    # Check if Firebase CLI is installed
    if ! command -v firebase &> /dev/null; then
        log_error "Firebase CLI not found. Please install it first."
        exit 1
    fi
    
    # Check if gcloud is installed and authenticated
    if ! command -v gcloud &> /dev/null; then
        log_error "Google Cloud CLI not found. Please install it first."
        exit 1
    fi
    
    # Verify project access
    if ! gcloud projects describe $PROJECT_ID &> /dev/null; then
        log_error "Cannot access project $PROJECT_ID. Please check permissions."
        exit 1
    fi
    
    # Check if Android build tools are available
    if [ ! -f "gradlew" ]; then
        log_error "Android project not found. Please run from project root."
        exit 1
    fi
    
    log_success "Pre-deployment checks passed"
}

# Backup current production data
backup_production_data() {
    log_info "Creating production data backup..."
    
    # Export Firestore data
    gcloud firestore export gs://$BACKUP_BUCKET/firestore-backup-$APP_VERSION \
        --project=$PROJECT_ID \
        --async
    
    # Backup Firebase Storage
    gsutil -m cp -r gs://$PROJECT_ID.appspot.com gs://$BACKUP_BUCKET/storage-backup-$APP_VERSION/
    
    log_success "Production data backup initiated"
}

# Deploy Firebase Functions
deploy_firebase_functions() {
    log_info "Deploying Firebase Functions..."
    
    cd firebase/functions
    
    # Install dependencies
    npm ci --production
    
    # Deploy functions
    firebase deploy --only functions --project=$PROJECT_ID
    
    cd ../..
    
    log_success "Firebase Functions deployed"
}

# Deploy Firestore rules and indexes
deploy_firestore_config() {
    log_info "Deploying Firestore rules and indexes..."
    
    # Deploy security rules
    firebase deploy --only firestore:rules --project=$PROJECT_ID
    
    # Deploy indexes
    firebase deploy --only firestore:indexes --project=$PROJECT_ID
    
    log_success "Firestore configuration deployed"
}

# Build and deploy Android app
build_android_app() {
    log_info "Building Android app for production..."
    
    # Clean previous builds
    ./gradlew clean
    
    # Run tests
    log_info "Running unit tests..."
    ./gradlew testReleaseUnitTest
    
    # Build release APK
    log_info "Building release APK..."
    ./gradlew assembleRelease
    
    # Build AAB for Play Store
    log_info "Building Android App Bundle..."
    ./gradlew bundleRelease
    
    log_success "Android app built successfully"
}

# Deploy to Firebase App Distribution
deploy_to_app_distribution() {
    log_info "Deploying to Firebase App Distribution..."
    
    # Upload APK to App Distribution
    firebase appdistribution:distribute app/build/outputs/apk/release/app-release.apk \
        --app $FIREBASE_APP_ID \
        --groups "pilot-testers" \
        --release-notes "Production release $APP_VERSION with all critical fixes"
    
    log_success "App deployed to Firebase App Distribution"
}

# Configure monitoring and alerting
setup_monitoring() {
    log_info "Setting up production monitoring..."
    
    # Enable Firebase Performance Monitoring
    gcloud services enable firebaseperf.googleapis.com --project=$PROJECT_ID
    
    # Enable Firebase Crashlytics
    gcloud services enable crashlytics.googleapis.com --project=$PROJECT_ID
    
    # Set up Cloud Monitoring alerts
    gcloud alpha monitoring policies create --policy-from-file=monitoring/alert-policies.yaml --project=$PROJECT_ID
    
    log_success "Monitoring and alerting configured"
}

# Verify deployment
verify_deployment() {
    log_info "Verifying deployment..."
    
    # Check Firebase Functions health
    curl -f "https://$REGION-$PROJECT_ID.cloudfunctions.net/healthCheck" || {
        log_error "Firebase Functions health check failed"
        exit 1
    }
    
    # Verify Firestore rules
    firebase firestore:rules:test --project=$PROJECT_ID
    
    # Check app distribution
    firebase appdistribution:releases:list --app $FIREBASE_APP_ID --project=$PROJECT_ID
    
    log_success "Deployment verification completed"
}

# Post-deployment tasks
post_deployment_tasks() {
    log_info "Running post-deployment tasks..."
    
    # Send deployment notification
    curl -X POST "https://$REGION-$PROJECT_ID.cloudfunctions.net/sendDeploymentNotification" \
        -H "Content-Type: application/json" \
        -d "{\"version\":\"$APP_VERSION\",\"timestamp\":\"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"}"
    
    # Update deployment tracking
    echo "$APP_VERSION,$(date -u +%Y-%m-%dT%H:%M:%SZ),success" >> deployment/deployment-log.csv
    
    log_success "Post-deployment tasks completed"
}

# Rollback function
rollback_deployment() {
    log_warning "Rolling back deployment..."
    
    # Get previous version
    PREVIOUS_VERSION=$(tail -2 deployment/deployment-log.csv | head -1 | cut -d',' -f1)
    
    if [ -z "$PREVIOUS_VERSION" ]; then
        log_error "No previous version found for rollback"
        exit 1
    fi
    
    # Rollback Firebase Functions
    firebase functions:config:clone --from=$PREVIOUS_VERSION --project=$PROJECT_ID
    firebase deploy --only functions --project=$PROJECT_ID
    
    # Restore Firestore data if needed
    # gcloud firestore import gs://$BACKUP_BUCKET/firestore-backup-$PREVIOUS_VERSION
    
    log_success "Rollback completed to version $PREVIOUS_VERSION"
}

# Main deployment flow
main() {
    case "${1:-deploy}" in
        "deploy")
            pre_deployment_checks
            backup_production_data
            deploy_firebase_functions
            deploy_firestore_config
            build_android_app
            deploy_to_app_distribution
            setup_monitoring
            verify_deployment
            post_deployment_tasks
            
            log_success "🎉 Production deployment completed successfully!"
            log_info "App Version: $APP_VERSION"
            log_info "Deployment Time: $(date)"
            log_info "Next Steps:"
            log_info "1. Monitor dashboards for any issues"
            log_info "2. Notify pilot users about the new release"
            log_info "3. Begin Phase 1 pilot testing"
            ;;
        "rollback")
            rollback_deployment
            ;;
        "verify")
            verify_deployment
            ;;
        *)
            echo "Usage: $0 {deploy|rollback|verify}"
            echo "  deploy   - Full production deployment"
            echo "  rollback - Rollback to previous version"
            echo "  verify   - Verify current deployment"
            exit 1
            ;;
    esac
}

# Trap errors and provide rollback option
trap 'log_error "Deployment failed! Run ./production-deploy.sh rollback to revert changes."; exit 1' ERR

# Execute main function
main "$@"
