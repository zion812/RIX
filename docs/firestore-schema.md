# RIO Firestore Database Schema

## Overview

Comprehensive Firestore database schema for the RIO rooster traceability and marketplace platform, designed for 600K+ users across rural and urban areas in Andhra Pradesh/Telangana. Optimized for intermittent connectivity and regional requirements.

## Database Architecture

### Collection Hierarchy
```
/users/{userId}
  /fowls/{fowlId}
  /conversations/{conversationId}
    /messages/{messageId}
  /notifications/{notificationId}
  /favorites/{favoriteId}

/fowls/{fowlId}
  /health_records/{recordId}
  /photos/{photoId}
  /lineage/{lineageId}

/transfers/{transferId}
  /documents/{documentId}
  /verification_steps/{stepId}

/marketplace/{listingId}
  /bids/{bidId}
  /watchers/{watcherId}

/breeding_records/{recordId}
  /offspring/{offspringId}

/verification_requests/{requestId}
  /documents/{documentId}

/conversations/{conversationId}
  /messages/{messageId}
  /participants/{participantId}

/notifications/{notificationId}

/admin/{documentId}
```

## Core Collections

### 1. users Collection

**Document ID**: Firebase Auth UID
**Purpose**: Extended user profiles with tier-based permissions and regional data

```typescript
interface UserDocument {
  // Basic Profile
  uid: string;                    // Firebase Auth UID
  email: string;                  // Primary email
  phoneNumber?: string;           // Indian mobile number (+91)
  displayName: string;            // User's display name
  photoURL?: string;              // Profile picture URL

  // Regional Information
  region: 'andhra_pradesh' | 'telangana' | 'other';
  district: string;               // District name
  mandal?: string;                // Sub-district (for rural users)
  village?: string;               // Village name (for farmers)
  pincode: string;                // Postal code
  coordinates?: {                 // GPS coordinates (optional)
    latitude: number;
    longitude: number;
  };

  // User Tier & Permissions (synced with custom claims)
  tier: 'general' | 'farmer' | 'enthusiast';
  permissions: {
    canCreateListings: boolean;
    canEditListings: boolean;
    canDeleteListings: boolean;
    canAccessMarketplace: boolean;
    canManageBreedingRecords: boolean;
    canAccessAnalytics: boolean;
    canAccessPremiumFeatures: boolean;
    canVerifyTransfers: boolean;
    canAccessPrioritySupport: boolean;
    canModerateContent: boolean;
  };

  // Verification Status
  verificationStatus: {
    level: 'basic' | 'enhanced' | 'premium';
    emailVerified: boolean;
    phoneVerified: boolean;
    identityVerified: boolean;
    farmDocumentsVerified: boolean;
    referencesVerified: boolean;
    verifiedAt?: Timestamp;
    verifiedBy?: string;          // Admin user ID
  };

  // Farm Information (for farmers and enthusiasts)
  farmDetails?: {
    farmName: string;
    farmType: 'commercial' | 'hobby' | 'breeding' | 'research';
    establishedYear: number;
    totalArea: number;            // in acres
    fowlCapacity: number;         // maximum birds
    specializations: string[];    // breed specializations
    certifications: string[];    // organic, free-range, etc.
  };

  // Language & Preferences
  language: 'en' | 'te' | 'hi';   // English, Telugu, Hindi
  preferences: {
    notifications: {
      email: boolean;
      sms: boolean;
      push: boolean;
    };
    privacy: {
      showLocation: boolean;
      showPhoneNumber: boolean;
      allowDirectMessages: boolean;
    };
    marketplace: {
      autoRenewListings: boolean;
      priceAlerts: boolean;
      favoriteBreeds: string[];
    };
  };

  // Usage Statistics
  stats: {
    totalFowls: number;
    totalListings: number;
    totalSales: number;
    totalPurchases: number;
    totalMessages: number;
    rating: number;               // 1-5 stars
    reviewCount: number;
  };

  // Limits (based on tier)
  limits: {
    maxListings: number;
    maxPhotosPerListing: number;
    maxBreedingRecords: number;
    dailyMessageLimit: number;
  };

  // Timestamps
  createdAt: Timestamp;
  lastLoginAt: Timestamp;
  lastActiveAt: Timestamp;
  updatedAt: Timestamp;

  // Metadata
  metadata: {
    deviceInfo?: string;
    appVersion?: string;
    registrationSource: string;   // 'web' | 'android' | 'referral'
    referredBy?: string;          // User ID who referred
    tierUpgradeHistory: Array<{
      fromTier: string;
      toTier: string;
      upgradedAt: Timestamp;
      reason: string;
    }>;
  };
}
```

### 2. fowls Collection

**Document ID**: Auto-generated
**Purpose**: Individual rooster/hen records with complete lineage tracking

```typescript
interface FowlDocument {
  // Basic Information
  fowlId: string;                 // Auto-generated unique ID
  ownerId: string;                // Current owner's user ID
  name?: string;                  // Pet name (optional)

  // Physical Characteristics
  breed: {
    primary: string;              // Aseel, Kadaknath, Chittagong, etc.
    secondary?: string;           // Mixed breed secondary
    purity: number;               // Percentage (0-100)
    variety?: string;             // Color variety, strain
  };

  gender: 'male' | 'female' | 'unknown';

  physicalTraits: {
    color: string;                // Primary color
    weight: number;               // in kg
    height: number;               // in cm
    combType: string;             // Single, Rose, Pea, etc.
    legColor: string;
    eyeColor: string;
    distinguishingMarks?: string; // Scars, unique features
  };

  // Age & Birth Information
  birthDate?: Timestamp;          // Exact or estimated
  ageCategory: 'chick' | 'juvenile' | 'adult' | 'senior';
  estimatedAge?: {                // When exact birth date unknown
    months: number;
    confidence: 'low' | 'medium' | 'high';
  };

  // Lineage & Genetics
  lineage: {
    fatherId?: string;            // Reference to father fowl
    motherId?: string;            // Reference to mother fowl
    generation: number;           // Generation number in tracking
    bloodline?: string;           // Named bloodline/strain
    inbreedingCoefficient?: number; // Genetic diversity metric
  };

  // Geographic Origin
  origin: {
    region: string;               // Birth region
    district: string;             // Birth district
    farmId?: string;              // Birth farm reference
    coordinates?: {
      latitude: number;
      longitude: number;
    };
  };

  // Current Status
  status: {
    health: 'excellent' | 'good' | 'fair' | 'poor' | 'sick';
    availability: 'available' | 'sold' | 'breeding' | 'deceased' | 'missing';
    location: {
      currentFarm?: string;
      district: string;
      lastUpdated: Timestamp;
    };
  };

  // Performance Metrics (for breeding birds)
  performance?: {
    eggProduction?: {
      averagePerMonth: number;
      totalLifetime: number;
      lastRecorded: Timestamp;
    };
    breeding?: {
      totalOffspring: number;
      successfulMatings: number;
      lastBreeding: Timestamp;
    };
    fighting?: {              // For game birds
      wins: number;
      losses: number;
      draws: number;
      retired: boolean;
    };
  };

  // Documentation
  documentation: {
    registrationNumber?: string;  // Official registration
    microchipId?: string;        // RFID/microchip
    tattooId?: string;           // Tattoo identification
    certificates: string[];      // Certificate URLs
    pedigreeChart?: string;      // Pedigree document URL
  };

  // Media
  media: {
    primaryPhoto?: string;        // Main photo URL
    photoCount: number;          // Total photos
    videoCount: number;          // Total videos
    lastPhotoAdded?: Timestamp;
  };

  // Ownership History
  ownershipHistory: Array<{
    ownerId: string;
    transferId?: string;         // Reference to transfer record
    acquiredDate: Timestamp;
    transferredDate?: Timestamp;
    price?: number;              // Purchase price
    currency: 'INR';
  }>;

  // Timestamps
  createdAt: Timestamp;
  updatedAt: Timestamp;
  lastHealthCheck?: Timestamp;

  // Search & Indexing
  searchTerms: string[];         // For text search
  tags: string[];               // User-defined tags

  // Metadata
  metadata: {
    dataQuality: 'high' | 'medium' | 'low'; // Data completeness
    verificationLevel: 'unverified' | 'basic' | 'premium';
    lastVerifiedBy?: string;     // Verifier user ID
    importSource?: string;       // If imported from external system
    notes?: string;              // Additional notes
  };
}
```

### 3. transfers Collection

**Document ID**: Auto-generated
**Purpose**: Immutable ownership changes with document proof and blockchain-like verification

```typescript
interface TransferDocument {
  // Transfer Identification
  transferId: string;             // Auto-generated unique ID
  transferType: 'sale' | 'gift' | 'inheritance' | 'breeding_loan' | 'return';

  // Parties Involved
  fromUserId: string;             // Previous owner
  toUserId: string;               // New owner
  fowlId: string;                 // Fowl being transferred

  // Transfer Details
  transferDetails: {
    price?: number;               // Sale price (if applicable)
    currency: 'INR';
    paymentMethod?: 'cash' | 'bank_transfer' | 'upi' | 'cheque' | 'barter';
    paymentReference?: string;    // Transaction ID/reference

    // Terms & Conditions
    terms?: string;               // Special terms
    warranty?: string;            // Health/breeding warranty
    returnPolicy?: string;        // Return conditions
    deliveryTerms?: string;       // Delivery arrangements
  };

  // Verification & Documentation
  verification: {
    status: 'pending' | 'in_progress' | 'verified' | 'rejected' | 'disputed';
    level: 'basic' | 'enhanced' | 'premium';  // Based on user tiers
    requiredDocuments: string[];  // List of required document types
    submittedDocuments: string[]; // List of submitted document IDs

    verificationSteps: Array<{
      step: string;               // 'document_upload' | 'identity_check' | 'payment_verification'
      status: 'pending' | 'completed' | 'failed';
      completedAt?: Timestamp;
      verifiedBy?: string;       // Verifier user ID
      notes?: string;
    }>;

    // Multi-signature verification for high-value transfers
    signatures?: Array<{
      userId: string;             // Signatory user ID
      role: 'buyer' | 'seller' | 'witness' | 'verifier';
      signedAt: Timestamp;
      signature: string;          // Digital signature hash
      ipAddress?: string;
    }>;
  };

  // Geographic Information
  location: {
    transferLocation?: {
      district: string;
      coordinates?: {
        latitude: number;
        longitude: number;
      };
    };
    deliveryRequired: boolean;
    deliveryAddress?: string;
    deliveryStatus?: 'pending' | 'in_transit' | 'delivered' | 'failed';
  };

  // Timeline
  timeline: {
    initiatedAt: Timestamp;       // Transfer request created
    agreedAt?: Timestamp;         // Both parties agreed
    documentsSubmittedAt?: Timestamp;
    verificationStartedAt?: Timestamp;
    verificationCompletedAt?: Timestamp;
    completedAt?: Timestamp;      // Transfer finalized
    cancelledAt?: Timestamp;      // If cancelled
  };

  // Status Tracking
  status: {
    current: 'draft' | 'pending_agreement' | 'pending_payment' | 'pending_documents' |
             'under_verification' | 'completed' | 'cancelled' | 'disputed';
    history: Array<{
      status: string;
      changedAt: Timestamp;
      changedBy: string;          // User ID who changed status
      reason?: string;
    }>;
  };

  // Dispute Management
  dispute?: {
    status: 'open' | 'under_review' | 'resolved' | 'escalated';
    raisedBy: string;             // User ID who raised dispute
    raisedAt: Timestamp;
    reason: string;
    description: string;
    evidence: string[];           // Document/photo URLs
    resolution?: string;
    resolvedAt?: Timestamp;
    resolvedBy?: string;          // Admin/mediator user ID
  };

  // Blockchain-like Verification
  blockchainData: {
    previousTransferHash?: string; // Hash of previous transfer
    currentHash: string;          // Hash of this transfer
    merkleRoot?: string;          // For batch verification
    blockNumber?: number;         // Sequential block number
  };

  // Metadata
  metadata: {
    initiatedBy: string;          // User who initiated transfer
    source: 'marketplace' | 'direct' | 'breeding' | 'inheritance';
    relatedListingId?: string;    // If from marketplace
    relatedBreedingId?: string;   // If from breeding
    notes?: string;
    tags: string[];
  };

  // Timestamps
  createdAt: Timestamp;
  updatedAt: Timestamp;
}
```

### 4. marketplace Collection

**Document ID**: Auto-generated
**Purpose**: Listings with bidding, fixed-price sales, and real-time updates

```typescript
interface MarketplaceDocument {
  // Listing Identification
  listingId: string;             // Auto-generated unique ID
  sellerId: string;              // Seller's user ID
  fowlId: string;                // Fowl being sold

  // Listing Type & Pricing
  listingType: 'fixed_price' | 'auction' | 'negotiable' | 'breeding_service';

  pricing: {
    basePrice: number;           // Starting/fixed price
    currency: 'INR';
    reservePrice?: number;       // Minimum acceptable price (auctions)
    buyNowPrice?: number;        // Instant purchase price
    currentBid?: number;         // Current highest bid
    bidIncrement?: number;       // Minimum bid increment

    // Breeding service pricing
    breedingFee?: number;        // For breeding services
    studFee?: number;            // Stud service fee
  };

  // Listing Details
  details: {
    title: string;               // Listing title
    description: string;         // Detailed description
    highlights: string[];        // Key selling points

    // Fowl-specific details (cached from fowl document)
    breed: string;
    gender: 'male' | 'female';
    age: string;
    weight: number;
    color: string;

    // Health & Documentation
    healthStatus: string;
    vaccinated: boolean;
    healthCertificate?: string;  // Document URL
    pedigreeAvailable: boolean;
    registrationPapers: boolean;
  };

  // Media
  media: {
    photos: Array<{
      url: string;
      caption?: string;
      isPrimary: boolean;
      uploadedAt: Timestamp;
    }>;
    videos: Array<{
      url: string;
      thumbnail?: string;
      duration?: number;
      caption?: string;
      uploadedAt: Timestamp;
    }>;
  };

  // Geographic & Delivery
  location: {
    district: string;
    region: string;
    exactLocation?: {
      latitude: number;
      longitude: number;
    };

    delivery: {
      available: boolean;
      radius: number;            // Delivery radius in km
      cost: number;              // Delivery cost
      methods: string[];         // 'pickup' | 'local_delivery' | 'courier'
    };
  };

  // Auction Settings (if applicable)
  auction?: {
    startTime: Timestamp;
    endTime: Timestamp;
    autoExtend: boolean;         // Extend if bid in last minutes
    extensionTime: number;       // Minutes to extend
    minimumBidders: number;      // Minimum bidders to proceed

    currentHighestBid?: {
      amount: number;
      bidderId: string;
      bidTime: Timestamp;
      isProxy: boolean;          // Automatic proxy bid
    };

    bidHistory: Array<{
      bidderId: string;
      amount: number;
      bidTime: Timestamp;
      isWinning: boolean;
    }>;
  };

  // Status & Availability
  status: {
    current: 'draft' | 'active' | 'paused' | 'sold' | 'expired' | 'cancelled';
    visibility: 'public' | 'private' | 'tier_restricted';
    featured: boolean;           // Premium placement
    promoted: boolean;           // Paid promotion

    availability: {
      isAvailable: boolean;
      reservedBy?: string;       // User ID if reserved
      reservedUntil?: Timestamp;
      soldTo?: string;           // Buyer user ID
      soldAt?: Timestamp;
    };
  };

  // Engagement Metrics
  engagement: {
    views: number;
    uniqueViews: number;
    favorites: number;
    shares: number;
    inquiries: number;

    // Detailed analytics
    viewHistory: Array<{
      userId?: string;           // Anonymous if not logged in
      viewedAt: Timestamp;
      source: string;            // 'search' | 'category' | 'direct'
      duration?: number;         // Time spent viewing
    }>;
  };

  // Search & Discovery
  searchData: {
    keywords: string[];          // Searchable keywords
    tags: string[];              // User-defined tags
    category: string;            // Primary category
    subcategory?: string;        // Subcategory

    // Filters
    filters: {
      priceRange: string;        // '0-5000' | '5000-10000' etc.
      ageGroup: string;
      breedGroup: string;
      location: string;
    };
  };

  // Timestamps
  timeline: {
    createdAt: Timestamp;
    publishedAt?: Timestamp;
    lastUpdatedAt: Timestamp;
    expiresAt?: Timestamp;       // Auto-expiry date
    soldAt?: Timestamp;

    // Renewal tracking
    renewalCount: number;
    lastRenewedAt?: Timestamp;
    autoRenew: boolean;
  };

  // Seller Preferences
  sellerPreferences: {
    acceptOffers: boolean;
    minimumOffer?: number;
    preferredBuyers?: string[];  // Preferred buyer types
    blacklistedBuyers?: string[]; // Blocked buyers

    communication: {
      allowDirectMessages: boolean;
      allowPhoneCalls: boolean;
      preferredContactMethod: 'message' | 'phone' | 'email';
      responseTime: string;      // Expected response time
    };
  };

  // Metadata
  metadata: {
    source: 'web' | 'mobile' | 'api';
    listingQuality: 'high' | 'medium' | 'low'; // Based on completeness
    moderationStatus: 'pending' | 'approved' | 'rejected' | 'flagged';
    moderatedBy?: string;        // Moderator user ID
    moderatedAt?: Timestamp;

    // Performance tracking
    conversionRate?: number;     // Views to inquiries ratio
    averageResponseTime?: number; // Seller response time in hours
  };
}
```

### 5. messages Collection

**Document ID**: Auto-generated
**Purpose**: Scalable real-time chat system for 600K+ users with offline support

```typescript
interface MessageDocument {
  // Message Identification
  messageId: string;             // Auto-generated unique ID
  conversationId: string;        // Reference to conversation

  // Participants
  senderId: string;              // Sender's user ID
  recipientId?: string;          // Direct message recipient
  recipientIds?: string[];       // Group message recipients

  // Message Content
  content: {
    type: 'text' | 'image' | 'video' | 'audio' | 'file' | 'location' | 'fowl_card' | 'listing_card';

    // Text content
    text?: string;               // Message text

    // Media content
    media?: {
      url: string;
      filename?: string;
      size?: number;             // File size in bytes
      mimeType?: string;
      thumbnail?: string;        // For videos/images
      duration?: number;         // For audio/video
      dimensions?: {             // For images/videos
        width: number;
        height: number;
      };
    };

    // Special content types
    fowlCard?: {
      fowlId: string;
      fowlName?: string;
      breed: string;
      price?: number;
      photoUrl?: string;
    };

    listingCard?: {
      listingId: string;
      title: string;
      price: number;
      photoUrl?: string;
      status: string;
    };

    location?: {
      latitude: number;
      longitude: number;
      address?: string;
    };
  };

  // Message Status
  status: {
    sent: boolean;
    delivered: boolean;
    read: boolean;

    // Timestamps
    sentAt: Timestamp;
    deliveredAt?: Timestamp;
    readAt?: Timestamp;

    // Delivery tracking
    deliveryAttempts: number;
    lastDeliveryAttempt?: Timestamp;

    // Read receipts (for group messages)
    readBy?: Array<{
      userId: string;
      readAt: Timestamp;
    }>;
  };

  // Message Threading
  thread?: {
    isReply: boolean;
    replyToMessageId?: string;   // Original message ID
    threadId?: string;           // Thread identifier
    threadPosition: number;      // Position in thread
  };

  // Message Reactions
  reactions?: {
    [emoji: string]: Array<{
      userId: string;
      reactedAt: Timestamp;
    }>;
  };

  // Moderation & Safety
  moderation: {
    flagged: boolean;
    flaggedBy?: string[];        // User IDs who flagged
    flaggedAt?: Timestamp;
    flagReason?: string;

    moderationStatus: 'pending' | 'approved' | 'rejected' | 'auto_approved';
    moderatedBy?: string;        // Moderator user ID
    moderatedAt?: Timestamp;

    // Content filtering
    containsSpam: boolean;
    containsProfanity: boolean;
    containsPersonalInfo: boolean;

    // Auto-moderation scores
    spamScore?: number;          // 0-1 spam probability
    toxicityScore?: number;      // 0-1 toxicity score
  };

  // Encryption & Privacy
  encryption?: {
    encrypted: boolean;
    encryptionMethod?: string;   // Encryption algorithm used
    keyId?: string;              // Key identifier
  };

  // Offline Support
  offline: {
    queuedForSending: boolean;
    syncStatus: 'synced' | 'pending' | 'failed';
    lastSyncAttempt?: Timestamp;
    syncRetries: number;
  };

  // Message Priority
  priority: 'low' | 'normal' | 'high' | 'urgent';

  // Timestamps
  createdAt: Timestamp;
  updatedAt: Timestamp;
  expiresAt?: Timestamp;         // For disappearing messages

  // Metadata
  metadata: {
    source: 'mobile' | 'web' | 'api';
    deviceInfo?: string;
    appVersion?: string;
    messageSize: number;         // Total message size

    // Analytics
    editCount: number;
    lastEditedAt?: Timestamp;
    forwardCount: number;

    // Context
    relatedToListing?: string;   // Listing ID if discussing a listing
    relatedToFowl?: string;      // Fowl ID if discussing a fowl
    relatedToTransfer?: string;  // Transfer ID if discussing a transfer
  };
}
```

### 6. breeding_records Collection

**Document ID**: Auto-generated
**Purpose**: Breeding history and genetic lineage tracking

```typescript
interface BreedingRecordDocument {
  // Record Identification
  recordId: string;              // Auto-generated unique ID
  breederId: string;             // Breeder's user ID

  // Breeding Pair
  parents: {
    sireId: string;              // Father fowl ID
    damId: string;               // Mother fowl ID

    // Parent details (cached for performance)
    sire: {
      name?: string;
      breed: string;
      age: number;
      bloodline?: string;
    };

    dam: {
      name?: string;
      breed: string;
      age: number;
      bloodline?: string;
    };
  };

  // Breeding Details
  breeding: {
    method: 'natural' | 'artificial_insemination' | 'embryo_transfer';
    breedingDate: Timestamp;
    location: {
      farmId?: string;
      district: string;
      coordinates?: {
        latitude: number;
        longitude: number;
      };
    };

    // Breeding conditions
    environment: {
      temperature?: number;      // Celsius
      humidity?: number;         // Percentage
      season: 'spring' | 'summer' | 'monsoon' | 'winter';
      weatherConditions?: string;
    };

    // Breeding supervision
    supervisedBy?: string;       // Veterinarian/expert user ID
    assistedBy?: string[];       // Assistant user IDs
    notes?: string;              // Breeding notes
  };

  // Pregnancy & Incubation
  incubation?: {
    method: 'natural' | 'artificial';
    startDate: Timestamp;
    expectedHatchDate: Timestamp;
    actualHatchDate?: Timestamp;

    // Incubation conditions (for artificial)
    temperature?: number;
    humidity?: number;
    turningFrequency?: number;   // Times per day

    // Monitoring
    candlingDates: Timestamp[];
    fertilityRate?: number;      // Percentage of fertile eggs
    hatchabilityRate?: number;   // Percentage of hatched chicks
  };

  // Offspring Results
  offspring: {
    totalEggs?: number;
    fertilizedEggs?: number;
    hatchedChicks: number;
    survivedChicks: number;      // Survived to maturity

    // Individual offspring records
    chicks: Array<{
      fowlId?: string;           // If fowl record created
      gender?: 'male' | 'female' | 'unknown';
      birthWeight?: number;      // in grams
      birthDate: Timestamp;
      status: 'alive' | 'deceased' | 'sold' | 'unknown';

      // Early characteristics
      color?: string;
      markings?: string;
      healthStatus: 'healthy' | 'weak' | 'sick' | 'deceased';

      // Tracking
      soldTo?: string;           // Buyer user ID
      soldDate?: Timestamp;
      salePrice?: number;
      currentOwnerId?: string;
    }>;
  };

  // Genetic Analysis
  genetics?: {
    expectedTraits: string[];    // Predicted traits
    actualTraits?: string[];     // Observed traits in offspring

    // Genetic calculations
    inbreedingCoefficient: number; // 0-1 scale
    heterosisExpected?: number;  // Hybrid vigor prediction

    // Trait inheritance
    traitInheritance: {
      [trait: string]: {
        expected: string;        // Expected expression
        observed?: string;       // Actual expression
        dominance: 'dominant' | 'recessive' | 'codominant';
      };
    };
  };

  // Health & Veterinary
  health: {
    preBreedingHealthCheck: {
      sireHealthStatus: string;
      damHealthStatus: string;
      veterinarianId?: string;   // Vet user ID
      checkDate: Timestamp;
      certificates: string[];    // Health certificate URLs
    };

    postBreedingCare: {
      damCareNotes?: string;
      supplementsGiven?: string[];
      veterinaryVisits: Array<{
        date: Timestamp;
        veterinarianId: string;
        purpose: string;
        findings: string;
        treatment?: string;
      }>;
    };

    chickCare: {
      vaccinationSchedule?: Array<{
        vaccine: string;
        date: Timestamp;
        batchNumber?: string;
        veterinarianId?: string;
      }>;

      mortalityRecord?: Array<{
        fowlId?: string;
        deathDate: Timestamp;
        cause: string;
        age: number;             // Days old
        weight?: number;
      }>;
    };
  };

  // Performance Metrics
  performance: {
    breedingSuccess: boolean;
    fertilityRate: number;       // Percentage
    hatchabilityRate: number;    // Percentage
    survivalRate: number;        // To maturity

    // Economic metrics
    totalCost: number;           // Breeding costs
    revenue?: number;            // From offspring sales
    profitMargin?: number;       // Calculated profit

    // Quality metrics
    offspringQuality: 'excellent' | 'good' | 'average' | 'poor';
    breedingGoalAchieved: boolean;
    improvementNotes?: string;
  };

  // Documentation
  documentation: {
    photos: Array<{
      url: string;
      caption: string;
      takenAt: Timestamp;
      type: 'breeding' | 'eggs' | 'hatching' | 'chicks' | 'parents';
    }>;

    videos: Array<{
      url: string;
      caption: string;
      duration: number;
      takenAt: Timestamp;
      type: 'breeding' | 'hatching' | 'chick_development';
    }>;

    documents: Array<{
      url: string;
      type: 'health_certificate' | 'breeding_contract' | 'pedigree' | 'other';
      uploadedAt: Timestamp;
    }>;
  };

  // Status & Workflow
  status: {
    current: 'planned' | 'in_progress' | 'incubating' | 'hatched' | 'completed' | 'failed';

    workflow: Array<{
      stage: string;
      status: 'pending' | 'in_progress' | 'completed' | 'skipped';
      startedAt?: Timestamp;
      completedAt?: Timestamp;
      notes?: string;
    }>;
  };

  // Timestamps
  createdAt: Timestamp;
  updatedAt: Timestamp;
  completedAt?: Timestamp;

  // Metadata
  metadata: {
    breedingPurpose: 'commercial' | 'improvement' | 'preservation' | 'research' | 'hobby';
    breedingGoals: string[];     // Specific goals for this breeding

    // Quality & verification
    dataQuality: 'high' | 'medium' | 'low';
    verifiedBy?: string;         // Expert verifier user ID
    verificationLevel: 'unverified' | 'peer_reviewed' | 'expert_verified';

    // Research & sharing
    publiclyVisible: boolean;
    researchConsent: boolean;    // Allow use for research
    tags: string[];
    notes?: string;
  };
}
```

### 7. verification_requests Collection

**Document ID**: Auto-generated
**Purpose**: Document verification for tier upgrades

```typescript
interface VerificationRequestDocument {
  // Request Identification
  requestId: string;             // Auto-generated unique ID
  userId: string;                // User requesting verification

  // Request Details
  request: {
    currentTier: 'general' | 'farmer' | 'enthusiast';
    requestedTier: 'farmer' | 'enthusiast';
    requestType: 'tier_upgrade' | 'document_verification' | 'identity_verification';

    // Justification
    reason: string;              // Why requesting upgrade
    experience: string;          // Relevant experience
    goals: string;               // Future goals/plans
  };

  // Required Documents
  documents: {
    required: Array<{
      type: string;              // 'identity' | 'farm_ownership' | 'experience_certificate'
      description: string;       // What document is needed
      mandatory: boolean;        // Is this document required
      status: 'pending' | 'submitted' | 'verified' | 'rejected';
    }>;

    submitted: Array<{
      documentId: string;        // Reference to document
      type: string;
      filename: string;
      url: string;
      uploadedAt: Timestamp;

      // Verification details
      verificationStatus: 'pending' | 'verified' | 'rejected' | 'needs_clarification';
      verifiedBy?: string;       // Verifier user ID
      verifiedAt?: Timestamp;
      rejectionReason?: string;

      // Document analysis
      ocrText?: string;          // Extracted text
      documentQuality: 'high' | 'medium' | 'low';
      authenticity: 'verified' | 'suspicious' | 'fake' | 'unknown';
    }>;
  };

  // Verification Process
  verification: {
    status: 'submitted' | 'under_review' | 'additional_info_required' |
            'approved' | 'rejected' | 'expired';

    // Review stages
    stages: Array<{
      stage: 'document_review' | 'identity_verification' | 'reference_check' | 'final_approval';
      status: 'pending' | 'in_progress' | 'completed' | 'failed';
      assignedTo?: string;       // Reviewer user ID
      startedAt?: Timestamp;
      completedAt?: Timestamp;
      notes?: string;
    }>;

    // Reviewer assignments
    reviewers: Array<{
      userId: string;
      role: 'primary_reviewer' | 'secondary_reviewer' | 'specialist';
      assignedAt: Timestamp;
      expertise: string[];       // Areas of expertise
    }>;

    // Review timeline
    timeline: {
      submittedAt: Timestamp;
      firstReviewAt?: Timestamp;
      lastActivityAt: Timestamp;
      expectedCompletionAt: Timestamp;
      actualCompletionAt?: Timestamp;
    };
  };

  // Reference Checks (for enthusiast tier)
  references?: Array<{
    referenceId: string;
    referenceType: 'professional' | 'peer' | 'customer' | 'institution';

    // Reference details
    referenceName: string;
    referenceContact: string;
    relationship: string;
    yearsKnown: number;

    // Reference verification
    contactAttempts: number;
    lastContactAt?: Timestamp;
    verified: boolean;
    verificationMethod: 'phone' | 'email' | 'in_person' | 'document';

    // Reference feedback
    feedback?: {
      reliability: number;       // 1-5 scale
      expertise: number;         // 1-5 scale
      recommendation: 'strongly_recommend' | 'recommend' | 'neutral' | 'not_recommend';
      comments: string;
      providedAt: Timestamp;
    };
  }>;

  // Additional Information Requests
  additionalInfo?: Array<{
    requestId: string;
    requestedBy: string;         // Reviewer user ID
    requestedAt: Timestamp;

    // Request details
    category: 'document_clarification' | 'additional_document' | 'interview' | 'site_visit';
    description: string;
    priority: 'low' | 'medium' | 'high';
    dueDate?: Timestamp;

    // Response
    response?: {
      providedBy: string;        // User ID
      providedAt: Timestamp;
      content: string;
      attachments?: string[];    // Document URLs
    };

    status: 'pending' | 'provided' | 'overdue' | 'waived';
  }>;

  // Decision & Outcome
  decision?: {
    outcome: 'approved' | 'rejected' | 'conditional_approval';
    decidedBy: string;           // Decision maker user ID
    decidedAt: Timestamp;

    // Decision details
    reasoning: string;
    conditions?: string[];       // If conditional approval
    validUntil?: Timestamp;      // Approval expiry

    // Appeals process
    appealable: boolean;
    appealDeadline?: Timestamp;

    // Follow-up actions
    followUpRequired: boolean;
    followUpDate?: Timestamp;
    followUpType?: string;
  };

  // Communication Log
  communications: Array<{
    communicationId: string;
    type: 'email' | 'sms' | 'in_app' | 'phone_call' | 'meeting';
    direction: 'inbound' | 'outbound';

    // Participants
    from: string;                // User ID
    to: string[];                // User IDs

    // Content
    subject?: string;
    content: string;
    attachments?: string[];

    // Metadata
    sentAt: Timestamp;
    deliveredAt?: Timestamp;
    readAt?: Timestamp;
    responseRequired: boolean;
    priority: 'low' | 'medium' | 'high';
  }>;

  // Quality Assurance
  qualityAssurance?: {
    reviewedBy: string;          // QA reviewer user ID
    reviewedAt: Timestamp;

    // QA metrics
    processCompliance: boolean;
    documentationComplete: boolean;
    timelinessScore: number;     // 1-5 scale
    qualityScore: number;        // 1-5 scale

    // QA notes
    findings: string[];
    recommendations: string[];
    approved: boolean;
  };

  // Timestamps
  createdAt: Timestamp;
  updatedAt: Timestamp;
  expiresAt?: Timestamp;         // Request expiry

  // Metadata
  metadata: {
    source: 'user_request' | 'admin_initiated' | 'system_triggered';
    priority: 'low' | 'medium' | 'high' | 'urgent';

    // Processing metrics
    estimatedProcessingTime: number; // Hours
    actualProcessingTime?: number;   // Hours
    complexityScore: number;     // 1-5 scale

    // Regional considerations
    region: string;
    language: 'en' | 'te' | 'hi';
    localRequirements?: string[];

    // Tags and categorization
    tags: string[];
    category: string;
    subcategory?: string;

    // Notes
    internalNotes?: string;
    publicNotes?: string;
  };
}
```

### 8. notifications Collection

**Document ID**: Auto-generated
**Purpose**: Multi-channel notification system

```typescript
interface NotificationDocument {
  // Notification Identification
  notificationId: string;        // Auto-generated unique ID
  userId: string;                // Recipient user ID

  // Notification Content
  content: {
    type: 'system' | 'marketplace' | 'breeding' | 'transfer' | 'message' | 'verification' | 'promotion';
    category: 'info' | 'warning' | 'error' | 'success' | 'reminder' | 'urgent';

    // Multi-language content
    title: {
      en: string;
      te?: string;               // Telugu
      hi?: string;               // Hindi
    };

    body: {
      en: string;
      te?: string;
      hi?: string;
    };

    // Rich content
    actionText?: {
      en: string;
      te?: string;
      hi?: string;
    };

    actionUrl?: string;          // Deep link or URL
    imageUrl?: string;           // Notification image
    iconUrl?: string;            // Notification icon
  };

  // Delivery Channels
  channels: {
    inApp: {
      enabled: boolean;
      delivered: boolean;
      deliveredAt?: Timestamp;
      read: boolean;
      readAt?: Timestamp;
    };

    push: {
      enabled: boolean;
      delivered: boolean;
      deliveredAt?: Timestamp;
      clicked: boolean;
      clickedAt?: Timestamp;

      // Push notification details
      fcmToken?: string;
      messageId?: string;        // FCM message ID
      deliveryAttempts: number;
      lastAttemptAt?: Timestamp;
    };

    email: {
      enabled: boolean;
      sent: boolean;
      sentAt?: Timestamp;
      delivered: boolean;
      deliveredAt?: Timestamp;
      opened: boolean;
      openedAt?: Timestamp;
      clicked: boolean;
      clickedAt?: Timestamp;

      // Email details
      emailAddress: string;
      subject: string;
      templateId?: string;
      messageId?: string;        // Email service message ID
    };

    sms: {
      enabled: boolean;
      sent: boolean;
      sentAt?: Timestamp;
      delivered: boolean;
      deliveredAt?: Timestamp;

      // SMS details
      phoneNumber: string;
      messageId?: string;        // SMS service message ID
      cost?: number;             // SMS cost
      provider?: string;         // SMS provider
    };
  };

  // Context & Related Data
  context?: {
    relatedEntityType: 'fowl' | 'listing' | 'transfer' | 'breeding_record' | 'user' | 'verification_request';
    relatedEntityId: string;

    // Additional context data
    contextData?: {
      [key: string]: any;        // Flexible context data
    };

    // Action data
    actionData?: {
      action: string;            // Action to perform
      parameters?: {
        [key: string]: any;
      };
    };
  };

  // Scheduling & Timing
  scheduling: {
    scheduledFor?: Timestamp;    // When to send
    timezone?: string;           // User's timezone

    // Delivery preferences
    deliveryWindow?: {
      startHour: number;         // 0-23
      endHour: number;           // 0-23
      days: string[];            // Days of week
    };

    // Retry logic
    retryPolicy: {
      maxRetries: number;
      retryInterval: number;     // Minutes
      backoffMultiplier: number;
    };

    currentRetries: number;
    nextRetryAt?: Timestamp;
  };

  // User Preferences
  userPreferences: {
    notificationsEnabled: boolean;
    channelPreferences: {
      inApp: boolean;
      push: boolean;
      email: boolean;
      sms: boolean;
    };

    // Category preferences
    categoryPreferences: {
      [category: string]: boolean;
    };

    // Frequency limits
    dailyLimit?: number;
    weeklyLimit?: number;
    lastNotificationAt?: Timestamp;
  };

  // Analytics & Tracking
  analytics: {
    impressions: number;         // Times shown
    clicks: number;              // Times clicked
    conversions: number;         // Times action completed

    // Engagement metrics
    engagementScore: number;     // 0-1 scale
    relevanceScore: number;      // 0-1 scale

    // A/B testing
    experimentId?: string;
    variant?: string;

    // Performance metrics
    deliveryLatency?: number;    // Milliseconds
    processingTime?: number;     // Milliseconds
  };

  // Status & Lifecycle
  status: {
    current: 'draft' | 'scheduled' | 'sending' | 'sent' | 'delivered' | 'failed' | 'expired' | 'cancelled';

    // Status history
    history: Array<{
      status: string;
      changedAt: Timestamp;
      reason?: string;
    }>;

    // Error handling
    errors?: Array<{
      channel: string;
      error: string;
      occurredAt: Timestamp;
      retryable: boolean;
    }>;
  };

  // Timestamps
  createdAt: Timestamp;
  updatedAt: Timestamp;
  expiresAt?: Timestamp;         // Notification expiry

  // Metadata
  metadata: {
    source: 'system' | 'admin' | 'automated' | 'user_triggered';
    campaign?: string;           // Marketing campaign ID

    // Personalization
    personalized: boolean;
    personalizationData?: {
      [key: string]: any;
    };

    // Compliance
    gdprCompliant: boolean;
    dataRetentionDays: number;

    // Regional
    region: string;
    language: 'en' | 'te' | 'hi';

    // Tags
    tags: string[];

    // Internal tracking
    internalNotes?: string;
    createdBy?: string;          // Admin user ID if manually created
  };
}
```