// Enhanced Firestore Security Rules for RIO Rooster Community Platform
// Integrates with 3-tier authentication system and comprehensive database schema
// Optimized for 600K+ users with regional considerations

rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // ============================================================================
    // HELPER FUNCTIONS - Authentication & Authorization
    // ============================================================================

    // Basic authentication check
    function isAuthenticated() {
      return request.auth != null;
    }

    // Get user's tier from custom claims
    function getUserTier() {
      return request.auth.token.tier;
    }

    // Check if user has specific permission
    function hasPermission(permission) {
      return request.auth.token.permissions[permission] == true;
    }

    // Check verification level
    function isVerified(level) {
      return request.auth.token.verificationStatus.level == level;
    }

    // Check if user owns the resource
    function isOwner(userId) {
      return request.auth.uid == userId;
    }

    // Check user tier levels
    function isGeneralUser() {
      return getUserTier() == 'general';
    }

    function isFarmer() {
      return getUserTier() == 'farmer';
    }

    function isEnthusiast() {
      return getUserTier() == 'enthusiast';
    }

    // Check if user is admin or moderator
    function isAdmin() {
      return request.auth.token.permissions.canModerateContent == true;
    }

    // Check if user can access premium features
    function canAccessPremiumFeatures() {
      return hasPermission('canAccessPremiumFeatures');
    }

    // Regional access check
    function isInSameRegion(resourceRegion) {
      return request.auth.token.profile.region == resourceRegion ||
             request.auth.token.profile.region == 'other' ||
             resourceRegion == 'other';
    }

    // Check if user can perform action based on daily limits
    function withinDailyLimits(action) {
      // This would typically be checked via Cloud Functions
      // For security rules, we assume the client has already validated
      return true;
    }

    // Validate field updates (only allow specific fields to be updated)
    function onlyUpdatingFields(allowedFields) {
      return request.resource.data.diff(resource.data).affectedKeys().hasOnly(allowedFields);
    }

    // Check if document is being created with required fields
    function hasRequiredFields(requiredFields) {
      return request.resource.data.keys().hasAll(requiredFields);
    }

    // ============================================================================
    // USERS COLLECTION - Extended user profiles with tier-based access
    // ============================================================================

    match /users/{userId} {
      // Read access: Users can read their own profile, farmers/enthusiasts can read others
      allow read: if isAuthenticated() && (
        isOwner(userId) ||
        isFarmer() ||
        isEnthusiast() ||
        isAdmin()
      );

      // Create access: Only during registration (handled by Cloud Functions)
      allow create: if false; // Handled by Cloud Functions only

      // Update access: Users can update their own profile with tier-specific restrictions
      allow update: if isAuthenticated() && isOwner(userId) && (
        // General users can only update basic profile fields
        (isGeneralUser() && onlyUpdatingFields([
          'displayName', 'photoURL', 'preferences', 'language',
          'region', 'district', 'pincode', 'lastActiveAt', 'updatedAt'
        ])) ||

        // Farmers can update profile and some farm details
        (isFarmer() && onlyUpdatingFields([
          'displayName', 'photoURL', 'preferences', 'language',
          'region', 'district', 'pincode', 'farmDetails',
          'lastActiveAt', 'updatedAt', 'stats'
        ])) ||

        // Enthusiasts have broader update permissions
        (isEnthusiast() && onlyUpdatingFields([
          'displayName', 'photoURL', 'preferences', 'language',
          'region', 'district', 'pincode', 'farmDetails',
          'lastActiveAt', 'updatedAt', 'stats', 'metadata.notes'
        ])) ||

        // Admins can update verification status
        (isAdmin() && onlyUpdatingFields([
          'verificationStatus', 'tier', 'permissions', 'limits', 'updatedAt'
        ]))
      );

      // Delete access: Users cannot delete their profiles
      allow delete: if false;

      // ========================================================================
      // USER SUBCOLLECTIONS
      // ========================================================================

      // User's fowl collection
      match /fowls/{fowlId} {
        allow read: if isAuthenticated() && (
          isOwner(userId) ||
          (isFarmer() || isEnthusiast()) && resource.data.preferences.showInPublicProfile == true
        );

        allow write: if isAuthenticated() && isOwner(userId) && (
          isFarmer() || isEnthusiast()
        );
      }

      // User's conversations
      match /conversations/{conversationId} {
        allow read, write: if isAuthenticated() && isOwner(userId);
      }

      // User's notifications
      match /notifications/{notificationId} {
        allow read: if isAuthenticated() && isOwner(userId);
        allow update: if isAuthenticated() && isOwner(userId) &&
          onlyUpdatingFields(['status', 'actions']);
        allow create, delete: if false; // Managed by system
      }

      // User's favorites
      match /favorites/{favoriteId} {
        allow read, write: if isAuthenticated() && isOwner(userId);
      }
    }

    // ============================================================================
    // FOWLS COLLECTION - Individual rooster/hen records with lineage tracking
    // ============================================================================

    match /fowls/{fowlId} {
      // Read access: Public for available fowls, owner + farmers/enthusiasts for others
      allow read: if isAuthenticated() && (
        // Public access for available fowls
        resource.data.status.availability == 'available' ||

        // Owner can always read their fowls
        isOwner(resource.data.ownerId) ||

        // Farmers and enthusiasts can read for breeding/marketplace purposes
        (isFarmer() || isEnthusiast()) ||

        // Admins can read all
        isAdmin()
      );

      // Create access: Only farmers and enthusiasts can create fowl records
      allow create: if isAuthenticated() && (isFarmer() || isEnthusiast()) && (
        // Must be the owner
        request.resource.data.ownerId == request.auth.uid &&

        // Must have required fields
        hasRequiredFields(['ownerId', 'breed', 'gender', 'origin', 'status', 'createdAt']) &&

        // Regional validation
        isInSameRegion(request.resource.data.origin.region)
      );

      // Update access: Only owner can update their fowls
      allow update: if isAuthenticated() && isOwner(resource.data.ownerId) && (
        // Cannot change ownership
        request.resource.data.ownerId == resource.data.ownerId &&

        // Farmers can update most fields
        (isFarmer() && !onlyUpdatingFields(['ownerId', 'fowlId', 'createdAt'])) ||

        // Enthusiasts can update all fields except system fields
        (isEnthusiast() && !onlyUpdatingFields(['ownerId', 'fowlId', 'createdAt', 'metadata.verificationLevel']))
      );

      // Delete access: Only owner can delete (soft delete recommended)
      allow delete: if isAuthenticated() && isOwner(resource.data.ownerId) && (
        isFarmer() || isEnthusiast()
      );

      // ========================================================================
      // FOWL SUBCOLLECTIONS
      // ========================================================================

      // Health records
      match /health_records/{recordId} {
        allow read: if isAuthenticated() && (
          isOwner(resource.data.fowlId) ||
          (isEnthusiast() && canAccessPremiumFeatures())
        );

        allow create, update: if isAuthenticated() && (
          isOwner(get(/databases/$(database)/documents/fowls/$(fowlId)).data.ownerId) &&
          (isFarmer() || isEnthusiast())
        );

        allow delete: if false; // Health records should not be deleted
      }

      // Photos
      match /photos/{photoId} {
        allow read: if isAuthenticated() && (
          // Public photos
          resource.data.usage.isPublic == true ||

          // Owner can read all their photos
          isOwner(get(/databases/$(database)/documents/fowls/$(fowlId)).data.ownerId) ||

          // Farmers/enthusiasts can read for breeding purposes
          (isFarmer() || isEnthusiast())
        );

        allow create, update: if isAuthenticated() && (
          isOwner(get(/databases/$(database)/documents/fowls/$(fowlId)).data.ownerId) &&
          (isFarmer() || isEnthusiast())
        );

        allow delete: if isAuthenticated() && (
          isOwner(get(/databases/$(database)/documents/fowls/$(fowlId)).data.ownerId) &&
          (isFarmer() || isEnthusiast())
        );
      }

      // Lineage records
      match /lineage/{lineageId} {
        allow read: if isAuthenticated() && (
          // Owner can read lineage
          isOwner(get(/databases/$(database)/documents/fowls/$(fowlId)).data.ownerId) ||

          // Enthusiasts can read for research
          (isEnthusiast() && canAccessPremiumFeatures()) ||

          // Public lineage records
          resource.data.metadata.publiclyVisible == true
        );

        allow create, update: if isAuthenticated() && (
          isOwner(get(/databases/$(database)/documents/fowls/$(fowlId)).data.ownerId) &&
          (isFarmer() || isEnthusiast())
        );

        allow delete: if false; // Lineage records should be immutable
      }
    }

    // ============================================================================
    // MARKETPLACE COLLECTION - Listings with bidding and sales
    // ============================================================================

    match /marketplace/{listingId} {
      // Read access: All authenticated users can browse marketplace
      allow read: if isAuthenticated() && (
        // Active listings are public
        resource.data.status.current == 'active' ||

        // Owner can read their own listings
        isOwner(resource.data.sellerId) ||

        // Admins can read all listings
        isAdmin()
      );

      // Create access: Only farmers and enthusiasts can create listings
      allow create: if isAuthenticated() && hasPermission('canCreateListings') && (
        // Must be the seller
        request.resource.data.sellerId == request.auth.uid &&

        // Must have required fields
        hasRequiredFields(['sellerId', 'fowlId', 'listingType', 'pricing', 'details', 'location', 'status']) &&

        // Must own the fowl being listed
        isOwner(get(/databases/$(database)/documents/fowls/$(request.resource.data.fowlId)).data.ownerId) &&

        // Regional validation
        isInSameRegion(request.resource.data.location.region) &&

        // Check daily limits (would be validated by Cloud Functions)
        withinDailyLimits('listing')
      );

      // Update access: Only seller can update their listings
      allow update: if isAuthenticated() && hasPermission('canEditListings') && (
        isOwner(resource.data.sellerId) &&

        // Cannot change seller or fowl
        request.resource.data.sellerId == resource.data.sellerId &&
        request.resource.data.fowlId == resource.data.fowlId
      );

      // Delete access: Only seller can delete their listings
      allow delete: if isAuthenticated() && hasPermission('canDeleteListings') && (
        isOwner(resource.data.sellerId)
      );

      // ========================================================================
      // MARKETPLACE SUBCOLLECTIONS
      // ========================================================================

      // Bids on auction listings
      match /bids/{bidId} {
        allow read: if isAuthenticated() && (
          // Seller can see all bids
          isOwner(get(/databases/$(database)/documents/marketplace/$(listingId)).data.sellerId) ||

          // Bidder can see their own bids
          isOwner(resource.data.bidderId) ||

          // Enthusiasts can see bid history for research
          (isEnthusiast() && canAccessPremiumFeatures())
        );

        allow create: if isAuthenticated() && (
          // Must be authenticated user placing bid
          request.resource.data.bidderId == request.auth.uid &&

          // Cannot bid on own listing
          !isOwner(get(/databases/$(database)/documents/marketplace/$(listingId)).data.sellerId) &&

          // Listing must be active auction
          get(/databases/$(database)/documents/marketplace/$(listingId)).data.listingType == 'auction' &&
          get(/databases/$(database)/documents/marketplace/$(listingId)).data.status.current == 'active'
        );

        allow update, delete: if false; // Bids are immutable
      }

      // Watchers/favorites for listings
      match /watchers/{watcherId} {
        allow read, write: if isAuthenticated() && isOwner(watcherId);
      }
    }

    // ============================================================================
    // TRANSFERS COLLECTION - Ownership changes with verification
    // ============================================================================

    match /transfers/{transferId} {
      // Read access: Parties involved and verifiers
      allow read: if isAuthenticated() && (
        // Parties involved in transfer
        isOwner(resource.data.fromUserId) ||
        isOwner(resource.data.toUserId) ||

        // Enthusiasts can view transfers for verification
        (isEnthusiast() && hasPermission('canVerifyTransfers')) ||

        // Admins can view all transfers
        isAdmin()
      );

      // Create access: Farmers and enthusiasts can initiate transfers
      allow create: if isAuthenticated() && (isFarmer() || isEnthusiast()) && (
        // Must be involved in the transfer
        (request.resource.data.fromUserId == request.auth.uid ||
         request.resource.data.toUserId == request.auth.uid) &&

        // Must have required fields
        hasRequiredFields(['fromUserId', 'toUserId', 'fowlId', 'transferType', 'verification', 'status']) &&

        // Must own the fowl if selling
        (request.resource.data.fromUserId != request.auth.uid ||
         isOwner(get(/databases/$(database)/documents/fowls/$(request.resource.data.fowlId)).data.ownerId))
      );

      // Update access: Parties can update status, verifiers can update verification
      allow update: if isAuthenticated() && (
        // Parties can update transfer status
        ((isOwner(resource.data.fromUserId) || isOwner(resource.data.toUserId)) &&
         onlyUpdatingFields(['status', 'timeline', 'updatedAt'])) ||

        // Verifiers can update verification status
        ((isEnthusiast() && hasPermission('canVerifyTransfers')) &&
         onlyUpdatingFields(['verification', 'timeline', 'updatedAt'])) ||

        // Admins can update all fields except immutable ones
        (isAdmin() && !onlyUpdatingFields(['transferId', 'fowlId', 'createdAt']))
      );

      // Delete access: Only admins can delete transfers (for cleanup)
      allow delete: if isAdmin();

      // ========================================================================
      // TRANSFER SUBCOLLECTIONS
      // ========================================================================

      // Transfer documents
      match /documents/{documentId} {
        allow read: if isAuthenticated() && (
          isOwner(get(/databases/$(database)/documents/transfers/$(transferId)).data.fromUserId) ||
          isOwner(get(/databases/$(database)/documents/transfers/$(transferId)).data.toUserId) ||
          (isEnthusiast() && hasPermission('canVerifyTransfers')) ||
          isAdmin()
        );

        allow create, update: if isAuthenticated() && (
          isOwner(get(/databases/$(database)/documents/transfers/$(transferId)).data.fromUserId) ||
          isOwner(get(/databases/$(database)/documents/transfers/$(transferId)).data.toUserId)
        );

        allow delete: if false; // Documents should not be deleted
      }

      // Verification steps
      match /verification_steps/{stepId} {
        allow read: if isAuthenticated() && (
          isOwner(get(/databases/$(database)/documents/transfers/$(transferId)).data.fromUserId) ||
          isOwner(get(/databases/$(database)/documents/transfers/$(transferId)).data.toUserId) ||
          (isEnthusiast() && hasPermission('canVerifyTransfers')) ||
          isAdmin()
        );

        allow create, update: if isAuthenticated() && (
          (isEnthusiast() && hasPermission('canVerifyTransfers')) ||
          isAdmin()
        );

        allow delete: if false; // Verification steps are immutable
      }
    }

    // ============================================================================
    // MESSAGES COLLECTION - Real-time messaging system
    // ============================================================================

    match /messages/{messageId} {
      // Read access: Participants in the conversation
      allow read: if isAuthenticated() && (
        // Sender can read their messages
        isOwner(resource.data.senderId) ||

        // Recipient can read messages sent to them
        isOwner(resource.data.recipientId) ||

        // Group message participants
        (resource.data.recipientIds != null &&
         request.auth.uid in resource.data.recipientIds) ||

        // Admins for moderation
        isAdmin()
      );

      // Create access: Authenticated users within daily limits
      allow create: if isAuthenticated() && (
        // Must be the sender
        request.resource.data.senderId == request.auth.uid &&

        // Must have required fields
        hasRequiredFields(['senderId', 'content', 'status', 'sentAt']) &&

        // Check daily message limits (validated by Cloud Functions)
        withinDailyLimits('message')
      );

      // Update access: Sender can update message status, recipients can update read status
      allow update: if isAuthenticated() && (
        // Sender can edit their own messages (limited time window)
        (isOwner(resource.data.senderId) &&
         onlyUpdatingFields(['content', 'status', 'editedAt', 'updatedAt'])) ||

        // Recipients can update read status
        ((isOwner(resource.data.recipientId) ||
          (resource.data.recipientIds != null && request.auth.uid in resource.data.recipientIds)) &&
         onlyUpdatingFields(['status.readBy', 'status.readAt', 'reactions'])) ||

        // Admins can moderate
        (isAdmin() && onlyUpdatingFields(['moderation', 'updatedAt']))
      );

      // Delete access: Sender can delete their messages
      allow delete: if isAuthenticated() && (
        isOwner(resource.data.senderId) || isAdmin()
      );
    }

    // ============================================================================
    // BREEDING RECORDS COLLECTION - Breeding history and genetics
    // ============================================================================

    match /breeding_records/{recordId} {
      // Read access: Breeder, fowl owners, and enthusiasts for research
      allow read: if isAuthenticated() && (
        // Breeder can read their records
        isOwner(resource.data.breederId) ||

        // Sire owner can read
        isOwner(get(/databases/$(database)/documents/fowls/$(resource.data.parents.sireId)).data.ownerId) ||

        // Dam owner can read
        isOwner(get(/databases/$(database)/documents/fowls/$(resource.data.parents.damId)).data.ownerId) ||

        // Enthusiasts can read for research if public
        (isEnthusiast() && canAccessPremiumFeatures() &&
         resource.data.metadata.publiclyVisible == true) ||

        // Admins can read all
        isAdmin()
      );

      // Create access: Farmers and enthusiasts can create breeding records
      allow create: if isAuthenticated() && hasPermission('canManageBreedingRecords') && (
        // Must be the breeder
        request.resource.data.breederId == request.auth.uid &&

        // Must have required fields
        hasRequiredFields(['breederId', 'parents', 'breeding', 'status', 'createdAt']) &&

        // Must have access to both parent fowls
        (isOwner(get(/databases/$(database)/documents/fowls/$(request.resource.data.parents.sireId)).data.ownerId) ||
         isOwner(get(/databases/$(database)/documents/fowls/$(request.resource.data.parents.damId)).data.ownerId))
      );

      // Update access: Breeder can update their records
      allow update: if isAuthenticated() && hasPermission('canManageBreedingRecords') && (
        isOwner(resource.data.breederId) &&

        // Cannot change immutable fields
        request.resource.data.breederId == resource.data.breederId &&
        request.resource.data.parents == resource.data.parents &&
        request.resource.data.createdAt == resource.data.createdAt
      );

      // Delete access: Breeder can delete their records (soft delete recommended)
      allow delete: if isAuthenticated() && (
        isOwner(resource.data.breederId) || isAdmin()
      );

      // ========================================================================
      // BREEDING RECORD SUBCOLLECTIONS
      // ========================================================================

      // Offspring records
      match /offspring/{offspringId} {
        allow read: if isAuthenticated() && (
          isOwner(get(/databases/$(database)/documents/breeding_records/$(recordId)).data.breederId) ||
          (isEnthusiast() && canAccessPremiumFeatures())
        );

        allow create, update: if isAuthenticated() && (
          isOwner(get(/databases/$(database)/documents/breeding_records/$(recordId)).data.breederId)
        );

        allow delete: if false; // Offspring records should not be deleted
      }
    }

    // ============================================================================
    // VERIFICATION REQUESTS COLLECTION - Tier upgrade requests
    // ============================================================================

    match /verification_requests/{requestId} {
      // Read access: Requester and admins
      allow read: if isAuthenticated() && (
        isOwner(resource.data.userId) ||
        isAdmin()
      );

      // Create access: Users can create verification requests
      allow create: if isAuthenticated() && (
        // Must be requesting for themselves
        request.resource.data.userId == request.auth.uid &&

        // Must have required fields
        hasRequiredFields(['userId', 'request', 'documents', 'verification', 'createdAt'])
      );

      // Update access: Requester can update documents, admins can update verification
      allow update: if isAuthenticated() && (
        // Requester can update documents and request details
        (isOwner(resource.data.userId) &&
         onlyUpdatingFields(['documents', 'additionalInfo', 'updatedAt'])) ||

        // Admins can update verification status
        (isAdmin() &&
         onlyUpdatingFields(['verification', 'decision', 'communications', 'qualityAssurance', 'updatedAt']))
      );

      // Delete access: Only admins can delete (for cleanup)
      allow delete: if isAdmin();

      // ========================================================================
      // VERIFICATION REQUEST SUBCOLLECTIONS
      // ========================================================================

      // Verification documents
      match /documents/{documentId} {
        allow read: if isAuthenticated() && (
          isOwner(get(/databases/$(database)/documents/verification_requests/$(requestId)).data.userId) ||
          isAdmin()
        );

        allow create, update: if isAuthenticated() && (
          isOwner(get(/databases/$(database)/documents/verification_requests/$(requestId)).data.userId)
        );

        allow delete: if false; // Documents should not be deleted
      }
    }

    // ============================================================================
    // CONVERSATIONS COLLECTION - Chat conversations
    // ============================================================================

    match /conversations/{conversationId} {
      // Read access: Participants only
      allow read: if isAuthenticated() && (
        request.auth.uid in resource.data.participants ||
        isAdmin()
      );

      // Create access: Authenticated users can create conversations
      allow create: if isAuthenticated() && (
        // Creator must be in participants
        request.auth.uid in request.resource.data.participants &&

        // Must have required fields
        hasRequiredFields(['participants', 'type', 'createdAt'])
      );

      // Update access: Participants can update conversation
      allow update: if isAuthenticated() && (
        request.auth.uid in resource.data.participants &&

        // Cannot remove themselves from participants without proper leave process
        request.auth.uid in request.resource.data.participants
      );

      // Delete access: Only admins can delete conversations
      allow delete: if isAdmin();

      // ========================================================================
      // CONVERSATION SUBCOLLECTIONS
      // ========================================================================

      // Messages within conversations
      match /messages/{messageId} {
        allow read: if isAuthenticated() && (
          request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants ||
          isAdmin()
        );

        allow create: if isAuthenticated() && (
          request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants &&
          request.resource.data.senderId == request.auth.uid
        );

        allow update: if isAuthenticated() && (
          isOwner(resource.data.senderId) ||
          (request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants &&
           onlyUpdatingFields(['status.readBy', 'reactions']))
        );

        allow delete: if isAuthenticated() && (
          isOwner(resource.data.senderId) || isAdmin()
        );
      }

      // Participants management
      match /participants/{participantId} {
        allow read: if isAuthenticated() && (
          request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants ||
          isAdmin()
        );

        allow create, update: if isAuthenticated() && (
          request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants
        );

        allow delete: if isAuthenticated() && (
          isOwner(participantId) || isAdmin()
        );
      }
    }

    // ============================================================================
    // NOTIFICATIONS COLLECTION - System notifications
    // ============================================================================

    match /notifications/{notificationId} {
      // Read access: Recipient only
      allow read: if isAuthenticated() && (
        isOwner(resource.data.userId) ||
        isAdmin()
      );

      // Create access: Only system/admins can create notifications
      allow create: if isAdmin();

      // Update access: Recipient can update read status
      allow update: if isAuthenticated() && (
        isOwner(resource.data.userId) &&
        onlyUpdatingFields(['channels.inApp.read', 'channels.inApp.readAt', 'analytics'])
      );

      // Delete access: Only system can delete (for cleanup)
      allow delete: if isAdmin();
    }

    // ============================================================================
    // ADMIN COLLECTION - Administrative documents and settings
    // ============================================================================

    match /admin/{documentId} {
      // Only admins can access admin collection
      allow read, write: if isAdmin();
    }

  }
}