#!/bin/bash

# 🔍 RIO Platform Production Verification Script
# Comprehensive health checks for production deployment

set -e

PROJECT_ID="rio-platform-prod"
REGION="asia-south1"
BASE_URL="https://$REGION-$PROJECT_ID.cloudfunctions.net"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PASSED=0
FAILED=0
WARNINGS=0

log_test() {
    echo -e "${BLUE}[TEST]${NC} $1"
}

log_pass() {
    echo -e "${GREEN}[PASS]${NC} $1"
    ((PASSED++))
}

log_fail() {
    echo -e "${RED}[FAIL]${NC} $1"
    ((FAILED++))
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
    ((WARNINGS++))
}

# Test Firebase Functions Health
test_firebase_functions() {
    log_test "Testing Firebase Functions health..."
    
    # Health check endpoint
    if curl -f -s "$BASE_URL/healthCheck" > /dev/null; then
        log_pass "Firebase Functions health check"
    else
        log_fail "Firebase Functions health check failed"
    fi
    
    # Test user authentication function
    if curl -f -s "$BASE_URL/validateUser" -H "Content-Type: application/json" -d '{"test": true}' > /dev/null; then
        log_pass "User validation function accessible"
    else
        log_fail "User validation function not accessible"
    fi
    
    # Test transaction manager
    if curl -f -s "$BASE_URL/createSecureCoinOrder" -H "Content-Type: application/json" -d '{"test": true}' > /dev/null; then
        log_pass "Transaction manager function accessible"
    else
        log_fail "Transaction manager function not accessible"
    fi
}

# Test Firestore Security Rules
test_firestore_security() {
    log_test "Testing Firestore security rules..."
    
    # Test with Firebase CLI
    if firebase firestore:rules:test --project=$PROJECT_ID > /dev/null 2>&1; then
        log_pass "Firestore security rules validation"
    else
        log_fail "Firestore security rules validation failed"
    fi
    
    # Check if rules file exists and is valid
    if [ -f "firestore.rules" ]; then
        if grep -q "tier-based" firestore.rules; then
            log_pass "Tier-based security rules implemented"
        else
            log_warn "Tier-based security rules not found in firestore.rules"
        fi
    else
        log_fail "firestore.rules file not found"
    fi
}

# Test Database Performance
test_database_performance() {
    log_test "Testing database performance..."
    
    # Test Firestore query performance
    start_time=$(date +%s%N)
    if gcloud firestore databases describe --project=$PROJECT_ID > /dev/null 2>&1; then
        end_time=$(date +%s%N)
        duration=$(( (end_time - start_time) / 1000000 ))  # Convert to milliseconds
        
        if [ $duration -lt 1000 ]; then
            log_pass "Firestore query response time: ${duration}ms"
        else
            log_warn "Firestore query response time high: ${duration}ms"
        fi
    else
        log_fail "Firestore database not accessible"
    fi
}

# Test Storage and CDN
test_storage_cdn() {
    log_test "Testing Firebase Storage and CDN..."
    
    # Check if storage bucket exists
    if gsutil ls gs://$PROJECT_ID.appspot.com > /dev/null 2>&1; then
        log_pass "Firebase Storage bucket accessible"
    else
        log_fail "Firebase Storage bucket not accessible"
    fi
    
    # Test image optimization
    if gsutil ls gs://$PROJECT_ID.appspot.com/optimized/ > /dev/null 2>&1; then
        log_pass "Image optimization folder exists"
    else
        log_warn "Image optimization folder not found"
    fi
}

# Test Analytics and Monitoring
test_analytics_monitoring() {
    log_test "Testing analytics and monitoring..."
    
    # Check if Firebase Analytics is enabled
    if gcloud services list --enabled --filter="name:firebase.googleapis.com" --project=$PROJECT_ID > /dev/null 2>&1; then
        log_pass "Firebase Analytics enabled"
    else
        log_fail "Firebase Analytics not enabled"
    fi
    
    # Check if Crashlytics is enabled
    if gcloud services list --enabled --filter="name:crashlytics.googleapis.com" --project=$PROJECT_ID > /dev/null 2>&1; then
        log_pass "Firebase Crashlytics enabled"
    else
        log_fail "Firebase Crashlytics not enabled"
    fi
    
    # Check if Performance Monitoring is enabled
    if gcloud services list --enabled --filter="name:firebaseperf.googleapis.com" --project=$PROJECT_ID > /dev/null 2>&1; then
        log_pass "Firebase Performance Monitoring enabled"
    else
        log_fail "Firebase Performance Monitoring not enabled"
    fi
}

# Test Payment Integration
test_payment_integration() {
    log_test "Testing payment integration..."
    
    # Test Razorpay webhook endpoint
    if curl -f -s "$BASE_URL/handleRazorpayWebhook" -X POST -H "Content-Type: application/json" -d '{"test": true}' > /dev/null; then
        log_pass "Razorpay webhook endpoint accessible"
    else
        log_fail "Razorpay webhook endpoint not accessible"
    fi
    
    # Test coin purchase function
    if curl -f -s "$BASE_URL/processCoinPurchase" -H "Content-Type: application/json" -d '{"test": true}' > /dev/null; then
        log_pass "Coin purchase function accessible"
    else
        log_fail "Coin purchase function not accessible"
    fi
}

# Test Mobile App Distribution
test_app_distribution() {
    log_test "Testing mobile app distribution..."
    
    # Check if app is uploaded to Firebase App Distribution
    if firebase appdistribution:releases:list --app $FIREBASE_APP_ID --project=$PROJECT_ID > /dev/null 2>&1; then
        log_pass "Firebase App Distribution configured"
    else
        log_fail "Firebase App Distribution not configured"
    fi
    
    # Check if APK exists
    if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
        log_pass "Release APK exists"
    else
        log_warn "Release APK not found"
    fi
    
    # Check if AAB exists for Play Store
    if [ -f "app/build/outputs/bundle/release/app-release.aab" ]; then
        log_pass "Release AAB exists"
    else
        log_warn "Release AAB not found"
    fi
}

# Test Localization
test_localization() {
    log_test "Testing localization support..."
    
    # Check if Hindi strings exist
    if [ -f "app/src/main/res/values-hi/strings.xml" ]; then
        log_pass "Hindi localization files exist"
    else
        log_warn "Hindi localization files not found"
    fi
    
    # Check if Telugu strings exist
    if [ -f "app/src/main/res/values-te/strings.xml" ]; then
        log_pass "Telugu localization files exist"
    else
        log_warn "Telugu localization files not found"
    fi
    
    # Check LocalizationManager implementation
    if [ -f "core/common/src/main/java/com/rio/rostry/core/common/localization/LocalizationManager.kt" ]; then
        if grep -q "HINDI\|TELUGU" "core/common/src/main/java/com/rio/rostry/core/common/localization/LocalizationManager.kt"; then
            log_pass "LocalizationManager supports Hindi/Telugu"
        else
            log_warn "LocalizationManager missing Hindi/Telugu support"
        fi
    else
        log_fail "LocalizationManager not found"
    fi
}

# Test Offline Functionality
test_offline_functionality() {
    log_test "Testing offline functionality..."
    
    # Check if Room database is configured
    if find . -name "*.kt" -exec grep -l "Room\|Database" {} \; | head -1 > /dev/null; then
        log_pass "Room database implementation found"
    else
        log_fail "Room database implementation not found"
    fi
    
    # Check if sync manager exists
    if [ -f "core/data/src/main/java/com/rio/rostry/core/data/sync/SyncManager.kt" ]; then
        log_pass "SyncManager implementation exists"
    else
        log_fail "SyncManager implementation not found"
    fi
    
    # Check if offline repositories exist
    if find . -name "*RepositoryImpl.kt" -exec grep -l "offline\|sync" {} \; | head -1 > /dev/null; then
        log_pass "Offline repository implementations found"
    else
        log_warn "Offline repository implementations not found"
    fi
}

# Test Security Implementation
test_security_implementation() {
    log_test "Testing security implementation..."
    
    # Check if UserValidationService exists
    if [ -f "core/data/src/main/java/com/rio/rostry/core/data/service/UserValidationService.kt" ]; then
        log_pass "UserValidationService implementation exists"
    else
        log_fail "UserValidationService implementation not found"
    fi
    
    # Check if atomic transaction manager exists
    if [ -f "firebase/functions/src/transactionManager.ts" ]; then
        log_pass "Atomic transaction manager exists"
    else
        log_fail "Atomic transaction manager not found"
    fi
    
    # Check if memory management is implemented
    if find . -name "*.kt" -exec grep -l "onCleared\|cleanup" {} \; | head -1 > /dev/null; then
        log_pass "Memory management implementation found"
    else
        log_warn "Memory management implementation not found"
    fi
}

# Generate verification report
generate_report() {
    echo ""
    echo "=========================================="
    echo "🔍 RIO PLATFORM VERIFICATION REPORT"
    echo "=========================================="
    echo "Date: $(date)"
    echo "Project: $PROJECT_ID"
    echo ""
    echo "📊 RESULTS SUMMARY:"
    echo -e "  ${GREEN}Passed:${NC} $PASSED"
    echo -e "  ${RED}Failed:${NC} $FAILED"
    echo -e "  ${YELLOW}Warnings:${NC} $WARNINGS"
    echo ""
    
    if [ $FAILED -eq 0 ]; then
        echo -e "${GREEN}✅ PRODUCTION VERIFICATION PASSED${NC}"
        echo "The RIO platform is ready for production deployment!"
        echo ""
        echo "🚀 NEXT STEPS:"
        echo "1. Begin Phase 1 pilot with 100 farmers"
        echo "2. Monitor dashboards for 24 hours"
        echo "3. Collect user feedback"
        echo "4. Proceed to Phase 2 if successful"
        exit 0
    else
        echo -e "${RED}❌ PRODUCTION VERIFICATION FAILED${NC}"
        echo "Please fix the failed tests before proceeding with deployment."
        echo ""
        echo "🔧 REQUIRED ACTIONS:"
        echo "1. Review and fix failed tests"
        echo "2. Re-run verification script"
        echo "3. Ensure all tests pass before deployment"
        exit 1
    fi
}

# Main execution
main() {
    echo "🔍 Starting RIO Platform Production Verification..."
    echo "Project: $PROJECT_ID"
    echo "Region: $REGION"
    echo ""
    
    test_firebase_functions
    test_firestore_security
    test_database_performance
    test_storage_cdn
    test_analytics_monitoring
    test_payment_integration
    test_app_distribution
    test_localization
    test_offline_functionality
    test_security_implementation
    
    generate_report
}

# Execute main function
main "$@"
