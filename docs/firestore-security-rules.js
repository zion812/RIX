// Firestore Security Rules for RIO Rooster Community Platform
// Implements 3-tier user hierarchy with custom claims-based access control

rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Helper functions for user tier and permission checking
    function isAuthenticated() {
      return request.auth != null;
    }

    function getUserTier() {
      return request.auth.token.tier;
    }

    function hasPermission(permission) {
      return request.auth.token.permissions[permission] == true;
    }

    function isVerified(level) {
      return request.auth.token.verificationStatus.level == level;
    }

    function isOwner(userId) {
      return request.auth.uid == userId;
    }

    function isGeneralUser() {
      return getUserTier() == 'general';
    }

    function isFarmer() {
      return getUserTier() == 'farmer';
    }

    function isEnthusiast() {
      return getUserTier() == 'enthusiast';
    }

    function canCreateListings() {
      return hasPermission('canCreateListings');
    }

    function canEditListings() {
      return hasPermission('canEditListings');
    }

    function canDeleteListings() {
      return hasPermission('canDeleteListings');
    }

    function canAccessAnalytics() {
      return hasPermission('canAccessAnalytics');
    }

    function canAccessPremiumFeatures() {
      return hasPermission('canAccessPremiumFeatures');
    }

    function canManageBreedingRecords() {
      return hasPermission('canManageBreedingRecords');
    }

    function canVerifyTransfers() {
      return hasPermission('canVerifyTransfers');
    }

    function canModerateContent() {
      return hasPermission('canModerateContent');
    }

    // Users collection - profile and tier management
    match /users/{userId} {
      // Users can read their own profile, farmers and enthusiasts can read other profiles
      allow read: if isAuthenticated() && (
        isOwner(userId) ||
        isFarmer() ||
        isEnthusiast()
      );

      // Users can only update their own profile, with tier-specific restrictions
      allow update: if isAuthenticated() && isOwner(userId) && (
        // General users can only update basic profile fields
        (isGeneralUser() && onlyUpdatingFields(['profile', 'metadata.lastUpdated'])) ||
        // Farmers can update profile and some verification status
        (isFarmer() && onlyUpdatingFields(['profile', 'verificationStatus', 'metadata.lastUpdated'])) ||
        // Enthusiasts have broader update permissions
        (isEnthusiast() && onlyUpdatingFields(['profile', 'verificationStatus', 'metadata.lastUpdated']))
      );

      // Only allow creation during user registration (handled by Cloud Functions)
      allow create: if false; // Handled by Cloud Functions only

      // Users cannot delete their own profiles
      allow delete: if false;
    }

    // Helper function to check if only specific fields are being updated
    function onlyUpdatingFields(allowedFields) {
      return request.resource.data.diff(resource.data).affectedKeys().hasOnly(allowedFields);
    }

    // Fowl listings collection - marketplace functionality
    match /fowlListings/{listingId} {
      // All authenticated users can read listings (marketplace access)
      allow read: if isAuthenticated();

      // Only farmers and enthusiasts can create listings
      allow create: if isAuthenticated() && canCreateListings() && (
        // Ensure the listing owner is the authenticated user
        request.resource.data.ownerId == request.auth.uid &&
        // Validate required fields
        request.resource.data.keys().hasAll(['ownerId', 'title', 'description', 'price', 'breed', 'age', 'location', 'createdAt']) &&
        // Check listing limits based on user tier
        checkListingLimits()
      );

      // Only listing owner can update their listings (with edit permission)
      allow update: if isAuthenticated() && canEditListings() &&
        isOwner(resource.data.ownerId) &&
        // Prevent changing ownership
        request.resource.data.ownerId == resource.data.ownerId;

      // Only listing owner can delete their listings (with delete permission)
      allow delete: if isAuthenticated() && canDeleteListings() &&
        isOwner(resource.data.ownerId);
    }

    // Function to check listing limits based on user tier
    function checkListingLimits() {
      let userTier = getUserTier();
      let maxListings = userTier == 'farmer' ? 50 :
                       userTier == 'enthusiast' ? 200 : 0;

      // This would need to be implemented with a counter document or query
      // For now, we'll allow creation and handle limits in Cloud Functions
      return maxListings > 0;
    }

    // Breeding records collection - farmer and enthusiast feature
    match /breedingRecords/{recordId} {
      // Only farmers and enthusiasts can access breeding records
      allow read: if isAuthenticated() && canManageBreedingRecords() && (
        isOwner(resource.data.ownerId) ||
        // Enthusiasts can view other farmers' records for analytics
        isEnthusiast()
      );

      // Only farmers and enthusiasts can create breeding records
      allow create: if isAuthenticated() && canManageBreedingRecords() && (
        request.resource.data.ownerId == request.auth.uid &&
        request.resource.data.keys().hasAll(['ownerId', 'parentMale', 'parentFemale', 'breedingDate', 'expectedHatchDate'])
      );

      // Only record owner can update their breeding records
      allow update: if isAuthenticated() && canManageBreedingRecords() &&
        isOwner(resource.data.ownerId) &&
        request.resource.data.ownerId == resource.data.ownerId;

      // Only record owner can delete their breeding records
      allow delete: if isAuthenticated() && canManageBreedingRecords() &&
        isOwner(resource.data.ownerId);
    }

    // Verification requests collection - tier upgrade management
    match /verificationRequests/{requestId} {
      // Users can read their own verification requests
      allow read: if isAuthenticated() && isOwner(resource.data.userId);

      // Users can create verification requests for tier upgrades
      allow create: if isAuthenticated() && (
        request.resource.data.userId == request.auth.uid &&
        request.resource.data.keys().hasAll(['userId', 'requestedTier', 'status', 'createdAt']) &&
        request.resource.data.status == 'pending' &&
        // Can only request upgrade to farmer or enthusiast
        request.resource.data.requestedTier in ['farmer', 'enthusiast']
      );

      // Users can update their own pending requests (to add documents)
      allow update: if isAuthenticated() && isOwner(resource.data.userId) && (
        resource.data.status == 'pending' &&
        request.resource.data.status == 'pending' &&
        onlyUpdatingFields(['documents', 'updatedAt'])
      );

      // Users cannot delete verification requests
      allow delete: if false;
    }

    // Messages collection - communication between users
    match /messages/{messageId} {
      // Users can read messages where they are sender or recipient
      allow read: if isAuthenticated() && (
        isOwner(resource.data.senderId) ||
        isOwner(resource.data.recipientId)
      );

      // Users can create messages (with daily limits enforced by Cloud Functions)
      allow create: if isAuthenticated() && (
        request.resource.data.senderId == request.auth.uid &&
        request.resource.data.keys().hasAll(['senderId', 'recipientId', 'content', 'createdAt']) &&
        // Prevent self-messaging
        request.resource.data.senderId != request.resource.data.recipientId
      );

      // Messages cannot be updated or deleted (immutable)
      allow update: if false;
      allow delete: if false;
    }

    // Analytics collection - premium feature for enthusiasts
    match /analytics/{analyticsId} {
      // Only enthusiasts can access analytics
      allow read: if isAuthenticated() && canAccessAnalytics();

      // Analytics are generated by Cloud Functions only
      allow create: if false;
      allow update: if false;
      allow delete: if false;
    }

    // Transfer records collection - verified transfers for enthusiasts
    match /transferRecords/{transferId} {
      // Users can read transfers where they are buyer or seller
      allow read: if isAuthenticated() && (
        isOwner(resource.data.sellerId) ||
        isOwner(resource.data.buyerId) ||
        // Enthusiasts can view all transfers for verification
        (isEnthusiast() && canVerifyTransfers())
      );

      // Only farmers and enthusiasts can create transfer records
      allow create: if isAuthenticated() && (isFarmer() || isEnthusiast()) && (
        (request.resource.data.sellerId == request.auth.uid || request.resource.data.buyerId == request.auth.uid) &&
        request.resource.data.keys().hasAll(['sellerId', 'buyerId', 'listingId', 'price', 'status', 'createdAt']) &&
        request.resource.data.status == 'pending'
      );

      // Participants can update transfer status, enthusiasts can verify
      allow update: if isAuthenticated() && (
        // Participants can update status to completed/cancelled
        ((isOwner(resource.data.sellerId) || isOwner(resource.data.buyerId)) &&
         request.resource.data.status in ['completed', 'cancelled']) ||
        // Enthusiasts can verify transfers
        (isEnthusiast() && canVerifyTransfers() &&
         request.resource.data.status == 'verified')
      );

      // Transfer records cannot be deleted
      allow delete: if false;
    }

    // Reports collection - content moderation
    match /reports/{reportId} {
      // Users can read their own reports
      allow read: if isAuthenticated() && isOwner(resource.data.reporterId);

      // Any authenticated user can create reports
      allow create: if isAuthenticated() && (
        request.resource.data.reporterId == request.auth.uid &&
        request.resource.data.keys().hasAll(['reporterId', 'reportedUserId', 'reason', 'description', 'status', 'createdAt']) &&
        request.resource.data.status == 'pending' &&
        // Cannot report yourself
        request.resource.data.reporterId != request.resource.data.reportedUserId
      );

      // Reports cannot be updated or deleted by users
      allow update: if false;
      allow delete: if false;
    }

    // Admin collection - administrative functions (Cloud Functions only)
    match /admin/{document=**} {
      allow read, write: if false; // Only Cloud Functions can access
    }

    // System counters for enforcing limits
    match /counters/{counterId} {
      // Users can read their own counters
      allow read: if isAuthenticated() && counterId.matches('user_' + request.auth.uid + '_.*');

      // Counters are managed by Cloud Functions only
      allow create: if false;
      allow update: if false;
      allow delete: if false;
    }

    // Notifications collection
    match /notifications/{notificationId} {
      // Users can read their own notifications
      allow read: if isAuthenticated() && isOwner(resource.data.userId);

      // Users can update notification read status
      allow update: if isAuthenticated() && isOwner(resource.data.userId) &&
        onlyUpdatingFields(['read', 'readAt']);

      // Notifications are created by Cloud Functions only
      allow create: if false;
      allow delete: if false;
    }

    // Default deny rule for any unmatched paths
    match /{document=**} {
      allow read, write: if false;
    }
  }
}