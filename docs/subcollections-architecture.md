# RIO Firestore Subcollections Architecture

## Overview

This document defines the nested data structures and subcollection hierarchies for the RIO platform, optimized for scalability, performance, and efficient querying of complex relationships like fowl lineage, message threading, and transaction histories.

## Subcollection Design Principles

### 1. **Hierarchical Data Organization**
- Parent-child relationships for natural data grouping
- Efficient querying within document boundaries
- Reduced cross-collection joins

### 2. **Scalability Considerations**
- Subcollections can grow beyond parent document limits
- Independent scaling of nested data
- Efficient pagination and real-time updates

### 3. **Security Inheritance**
- Subcollections inherit parent document security
- Granular permissions at subcollection level
- Tier-based access control propagation

## Subcollection Hierarchies

### 1. users/{userId} Subcollections

#### 1.1 users/{userId}/fowls/{fowlId}
**Purpose**: User's personal fowl collection for quick access
**Relationship**: One-to-many (User → Fowls)

```typescript
interface UserFowlDocument {
  // Reference to main fowl document
  fowlId: string;                // Reference to /fowls/{fowlId}

  // User-specific data
  personalName?: string;         // User's pet name for the fowl
  acquisitionDate: Timestamp;
  acquisitionPrice?: number;
  acquisitionSource: 'purchase' | 'breeding' | 'gift' | 'inheritance';

  // Quick access data (cached from main fowl document)
  breed: string;
  gender: 'male' | 'female' | 'unknown';
  birthDate?: Timestamp;
  currentStatus: 'active' | 'sold' | 'deceased' | 'missing';

  // User preferences
  preferences: {
    isBreeder: boolean;
    isForSale: boolean;
    showInPublicProfile: boolean;
    allowBreedingRequests: boolean;
  };

  // Performance tracking
  userStats: {
    totalOffspring?: number;
    totalEarnings?: number;
    breedingRequests: number;
    marketplaceViews: number;
  };

  // Timestamps
  addedAt: Timestamp;
  lastUpdated: Timestamp;

  // Metadata
  metadata: {
    syncStatus: 'synced' | 'pending' | 'conflict';
    lastSyncAt?: Timestamp;
    notes?: string;
    tags: string[];
  };
}
```

#### 1.2 users/{userId}/conversations/{conversationId}
**Purpose**: User's conversation threads
**Relationship**: One-to-many (User → Conversations)

```typescript
interface UserConversationDocument {
  // Conversation identification
  conversationId: string;        // Reference to /conversations/{conversationId}

  // Participants
  participants: Array<{
    userId: string;
    displayName: string;
    photoURL?: string;
    role: 'owner' | 'participant' | 'admin';
    joinedAt: Timestamp;
    leftAt?: Timestamp;
  }>;

  // Conversation metadata
  type: 'direct' | 'group' | 'marketplace' | 'breeding' | 'support';
  title?: string;                // For group conversations
  description?: string;

  // User-specific settings
  userSettings: {
    muted: boolean;
    mutedUntil?: Timestamp;
    pinned: boolean;
    archived: boolean;

    // Notification preferences
    notifications: {
      enabled: boolean;
      sound: boolean;
      vibration: boolean;
    };

    // Privacy settings
    readReceipts: boolean;
    typingIndicators: boolean;
  };

  // Message summary
  lastMessage: {
    messageId: string;
    senderId: string;
    content: string;             // Truncated content
    timestamp: Timestamp;
    type: 'text' | 'image' | 'file' | 'system';
  };

  // Unread tracking
  unread: {
    count: number;
    lastReadMessageId?: string;
    lastReadAt?: Timestamp;
    mentionCount: number;        // Unread mentions
  };

  // Context information
  context?: {
    relatedToListing?: string;   // Listing ID
    relatedToFowl?: string;      // Fowl ID
    relatedToTransfer?: string;  // Transfer ID
    relatedToBreeding?: string;  // Breeding record ID
  };

  // Timestamps
  createdAt: Timestamp;
  lastActivityAt: Timestamp;
  lastMessageAt: Timestamp;

  // Metadata
  metadata: {
    messageCount: number;
    totalParticipants: number;
    isActive: boolean;

    // Moderation
    flagged: boolean;
    moderationStatus: 'clean' | 'warning' | 'restricted';

    // Analytics
    engagementScore: number;     // 0-1 based on activity
    averageResponseTime: number; // In minutes
  };
}
```

#### 1.3 users/{userId}/notifications/{notificationId}
**Purpose**: User's personal notification inbox
**Relationship**: One-to-many (User → Notifications)

```typescript
interface UserNotificationDocument {
  // Reference to main notification
  notificationId: string;        // Reference to /notifications/{notificationId}

  // User-specific status
  status: {
    read: boolean;
    readAt?: Timestamp;
    dismissed: boolean;
    dismissedAt?: Timestamp;
    archived: boolean;
    archivedAt?: Timestamp;
  };

  // Quick access data (cached)
  type: string;
  category: string;
  title: string;                 // In user's preferred language
  summary: string;               // Truncated body
  priority: 'low' | 'normal' | 'high' | 'urgent';

  // Action tracking
  actions: Array<{
    actionId: string;
    label: string;
    performed: boolean;
    performedAt?: Timestamp;
    result?: string;
  }>;

  // Delivery tracking
  delivery: {
    channel: 'in_app' | 'push' | 'email' | 'sms';
    deliveredAt: Timestamp;
    clickedAt?: Timestamp;
  };

  // Timestamps
  receivedAt: Timestamp;
  expiresAt?: Timestamp;

  // Metadata
  metadata: {
    source: string;
    campaign?: string;
    personalized: boolean;
    relevanceScore?: number;     // 0-1 based on user behavior
  };
}
```

#### 1.4 users/{userId}/favorites/{favoriteId}
**Purpose**: User's favorite fowls, listings, and breeders
**Relationship**: One-to-many (User → Favorites)

```typescript
interface UserFavoriteDocument {
  // Favorite identification
  favoriteId: string;            // Auto-generated

  // Favorite target
  targetType: 'fowl' | 'listing' | 'breeder' | 'bloodline';
  targetId: string;              // ID of the favorited item

  // Quick access data (cached)
  targetData: {
    title: string;
    description?: string;
    imageUrl?: string;
    price?: number;
    location?: string;

    // Type-specific data
    breed?: string;              // For fowls
    gender?: string;             // For fowls
    rating?: number;             // For breeders
    listingStatus?: string;      // For listings
  };

  // User preferences
  preferences: {
    priceAlerts: boolean;
    availabilityAlerts: boolean;
    updateNotifications: boolean;

    // Alert thresholds
    maxPrice?: number;
    minPrice?: number;
    preferredLocation?: string;
  };

  // Interaction tracking
  interactions: {
    viewCount: number;
    lastViewedAt?: Timestamp;
    inquiryCount: number;
    lastInquiryAt?: Timestamp;
    shareCount: number;
  };

  // Timestamps
  addedAt: Timestamp;
  lastUpdatedAt: Timestamp;

  // Metadata
  metadata: {
    source: 'manual' | 'recommendation' | 'search';
    tags: string[];
    notes?: string;

    // Recommendation data
    recommendationScore?: number;
    recommendationReason?: string;
  };
}
```

### 2. fowls/{fowlId} Subcollections

#### 2.1 fowls/{fowlId}/health_records/{recordId}
**Purpose**: Detailed health history and veterinary records
**Relationship**: One-to-many (Fowl → Health Records)

```typescript
interface FowlHealthRecordDocument {
  // Record identification
  recordId: string;              // Auto-generated
  fowlId: string;                // Parent fowl ID

  // Health event details
  eventType: 'vaccination' | 'illness' | 'injury' | 'checkup' | 'treatment' | 'surgery' | 'death';
  eventDate: Timestamp;

  // Veterinary information
  veterinary: {
    veterinarianId?: string;     // Vet user ID if registered
    veterinarianName: string;
    clinicName?: string;
    licenseNumber?: string;
    contactInfo: {
      phone?: string;
      email?: string;
      address?: string;
    };
  };

  // Health details
  healthData: {
    // Vital signs
    weight?: number;             // in kg
    temperature?: number;        // in Celsius
    heartRate?: number;          // beats per minute
    respiratoryRate?: number;    // breaths per minute

    // Physical examination
    generalCondition: 'excellent' | 'good' | 'fair' | 'poor' | 'critical';
    appetite: 'normal' | 'increased' | 'decreased' | 'absent';
    activity: 'normal' | 'hyperactive' | 'lethargic' | 'inactive';

    // Specific findings
    symptoms?: string[];
    diagnosis?: string;
    prognosis?: string;

    // Treatment administered
    treatment?: {
      medications: Array<{
        name: string;
        dosage: string;
        frequency: string;
        duration: string;
        route: 'oral' | 'injection' | 'topical' | 'inhalation';
      }>;

      procedures?: string[];
      recommendations?: string[];
      followUpRequired: boolean;
      followUpDate?: Timestamp;
    };
  };

  // Vaccination specific data
  vaccination?: {
    vaccineName: string;
    manufacturer: string;
    batchNumber: string;
    expiryDate: Timestamp;
    site: string;               // Injection site
    nextDueDate?: Timestamp;

    // Adverse reactions
    adverseReactions?: Array<{
      reaction: string;
      severity: 'mild' | 'moderate' | 'severe';
      onsetTime: string;
      duration: string;
      treatment?: string;
    }>;
  };

  // Documentation
  documentation: {
    photos: Array<{
      url: string;
      caption: string;
      takenAt: Timestamp;
    }>;

    documents: Array<{
      url: string;
      type: 'prescription' | 'lab_report' | 'x_ray' | 'certificate' | 'invoice';
      filename: string;
      uploadedAt: Timestamp;
    }>;

    // Lab results
    labResults?: Array<{
      testName: string;
      result: string;
      normalRange?: string;
      unit?: string;
      testedAt: Timestamp;
      labName?: string;
    }>;
  };

  // Cost information
  cost: {
    consultationFee?: number;
    medicationCost?: number;
    procedureCost?: number;
    totalCost: number;
    currency: 'INR';

    // Insurance
    insuranceCovered?: boolean;
    insuranceClaimId?: string;
    amountCovered?: number;
  };

  // Follow-up tracking
  followUp: {
    required: boolean;
    scheduledDate?: Timestamp;
    completed: boolean;
    completedDate?: Timestamp;
    notes?: string;
  };

  // Timestamps
  createdAt: Timestamp;
  updatedAt: Timestamp;

  // Metadata
  metadata: {
    recordedBy: string;          // User ID who recorded
    dataSource: 'manual' | 'vet_system' | 'import';
    verified: boolean;
    verifiedBy?: string;

    // Quality indicators
    completeness: number;        // 0-1 scale
    accuracy: 'high' | 'medium' | 'low';

    tags: string[];
    notes?: string;
  };
}
```

#### 2.2 fowls/{fowlId}/photos/{photoId}
**Purpose**: Photo gallery with metadata and organization
**Relationship**: One-to-many (Fowl → Photos)

```typescript
interface FowlPhotoDocument {
  // Photo identification
  photoId: string;               // Auto-generated
  fowlId: string;                // Parent fowl ID

  // Photo details
  photo: {
    url: string;                 // Storage URL
    thumbnailUrl?: string;       // Optimized thumbnail
    filename: string;
    originalFilename: string;

    // Technical metadata
    fileSize: number;            // Bytes
    mimeType: string;
    dimensions: {
      width: number;
      height: number;
    };

    // Camera/device info
    deviceInfo?: string;
    cameraSettings?: {
      iso?: number;
      aperture?: string;
      shutterSpeed?: string;
      focalLength?: string;
    };

    // Location data
    location?: {
      latitude: number;
      longitude: number;
      address?: string;
    };
  };

  // Photo categorization
  category: {
    type: 'profile' | 'full_body' | 'close_up' | 'action' | 'breeding' | 'health' | 'show' | 'family';
    subtype?: string;            // More specific categorization

    // Photo purpose
    purpose: 'documentation' | 'marketing' | 'breeding_record' | 'health_record' | 'show_entry' | 'personal';

    // Quality assessment
    quality: 'excellent' | 'good' | 'fair' | 'poor';
    technicalQuality: {
      sharpness: number;         // 0-1 scale
      lighting: number;          // 0-1 scale
      composition: number;       // 0-1 scale
      clarity: number;           // 0-1 scale
    };
  };

  // Photo content
  content: {
    caption?: string;
    description?: string;

    // Multi-language support
    captions?: {
      en?: string;
      te?: string;
      hi?: string;
    };

    // Tags and keywords
    tags: string[];
    keywords: string[];

    // Visible features
    visibleFeatures: string[];   // 'comb', 'tail', 'legs', 'full_body', etc.

    // Age at photo
    fowlAge?: {
      months: number;
      category: 'chick' | 'juvenile' | 'adult' | 'senior';
    };
  };

  // Usage and permissions
  usage: {
    isPrimary: boolean;          // Primary photo for fowl
    isPublic: boolean;           // Visible to public
    allowDownload: boolean;      // Allow downloads
    allowSharing: boolean;       // Allow social sharing

    // Commercial usage
    commercialUse: boolean;
    licenseType?: 'personal' | 'commercial' | 'editorial';

    // Marketplace usage
    usedInListings: string[];    // Listing IDs using this photo
    usedInBreedingAds: string[]; // Breeding ad IDs
  };

  // Engagement metrics
  engagement: {
    views: number;
    likes: number;
    shares: number;
    downloads: number;

    // Detailed analytics
    viewHistory: Array<{
      userId?: string;
      viewedAt: Timestamp;
      source: string;
      duration?: number;
    }>;

    reactions: Array<{
      userId: string;
      reaction: 'like' | 'love' | 'wow' | 'sad' | 'angry';
      reactedAt: Timestamp;
    }>;
  };

  // Photo processing
  processing: {
    status: 'uploaded' | 'processing' | 'ready' | 'failed';

    // AI analysis
    aiAnalysis?: {
      breedDetection?: {
        detectedBreed: string;
        confidence: number;       // 0-1 scale
        alternativeBreeds?: Array<{
          breed: string;
          confidence: number;
        }>;
      };

      qualityAssessment?: {
        overallScore: number;     // 0-1 scale
        issues: string[];         // Detected issues
        suggestions: string[];    // Improvement suggestions
      };

      featureDetection?: {
        detectedFeatures: Array<{
          feature: string;
          confidence: number;
          boundingBox?: {
            x: number;
            y: number;
            width: number;
            height: number;
          };
        }>;
      };
    };

    // Image enhancements
    enhancements?: {
      autoEnhanced: boolean;
      enhancementType?: string[];
      originalUrl?: string;      // URL to original unenhanced image
    };
  };

  // Timestamps
  takenAt?: Timestamp;           // When photo was taken
  uploadedAt: Timestamp;         // When uploaded to system
  processedAt?: Timestamp;       // When processing completed
  lastModifiedAt: Timestamp;

  // Metadata
  metadata: {
    uploadedBy: string;          // User ID who uploaded
    source: 'camera' | 'gallery' | 'import' | 'professional';

    // Verification
    verified: boolean;
    verifiedBy?: string;
    verificationDate?: Timestamp;

    // Moderation
    moderationStatus: 'pending' | 'approved' | 'rejected' | 'flagged';
    moderatedBy?: string;
    moderationNotes?: string;

    // Backup and sync
    backupStatus: 'pending' | 'backed_up' | 'failed';
    backupUrl?: string;
    syncStatus: 'synced' | 'pending' | 'conflict';

    // Additional metadata
    photographerCredit?: string;
    copyrightInfo?: string;
    notes?: string;
  };
}
```

#### 2.3 fowls/{fowlId}/lineage/{lineageId}
**Purpose**: Family tree relationships and genetic lineage tracking
**Relationship**: One-to-many (Fowl → Lineage Records)

```typescript
interface FowlLineageDocument {
  // Lineage identification
  lineageId: string;             // Auto-generated
  fowlId: string;                // Subject fowl ID

  // Relationship type
  relationshipType: 'parent' | 'offspring' | 'sibling' | 'grandparent' | 'grandchild' | 'cousin' | 'ancestor';

  // Related fowl information
  relatedFowl: {
    fowlId: string;              // Related fowl ID
    name?: string;               // Fowl name
    breed: string;
    gender: 'male' | 'female' | 'unknown';

    // Relationship specifics
    relationship: {
      degree: number;            // Degrees of separation (1=parent/child, 2=grandparent/grandchild)
      side: 'paternal' | 'maternal' | 'both' | 'unknown'; // Which side of family
      verified: boolean;         // Relationship verified
      verificationMethod: 'dna' | 'breeding_record' | 'documentation' | 'witness';
      confidence: 'high' | 'medium' | 'low';
    };
  };

  // Genetic information
  genetics: {
    sharedDNA?: number;          // Percentage of shared DNA (if tested)
    inbreedingCoefficient?: number; // Coefficient of inbreeding

    // Trait inheritance
    inheritedTraits?: Array<{
      trait: string;
      expression: string;
      dominance: 'dominant' | 'recessive' | 'codominant';
      source: 'paternal' | 'maternal' | 'both';
    }>;

    // Genetic health
    geneticRisks?: Array<{
      condition: string;
      riskLevel: 'low' | 'medium' | 'high';
      carrier: boolean;
      affected: boolean;
    }>;
  };

  // Documentation
  documentation: {
    breedingRecordId?: string;   // Reference to breeding record
    transferRecordId?: string;   // Reference to transfer record

    // Supporting documents
    documents: Array<{
      type: 'birth_certificate' | 'pedigree' | 'dna_test' | 'breeding_contract' | 'photo';
      url: string;
      uploadedAt: Timestamp;
      verifiedBy?: string;
    }>;

    // Witness information
    witnesses?: Array<{
      userId: string;
      name: string;
      relationship: string;       // Relationship to the fowls
      contactInfo?: string;
      witnessedAt: Timestamp;
    }>;
  };

  // Timeline
  timeline: {
    relationshipEstablished: Timestamp; // When relationship was first recorded
    lastVerified?: Timestamp;    // Last verification date

    // Key events
    events: Array<{
      eventType: 'birth' | 'breeding' | 'transfer' | 'verification' | 'dna_test';
      eventDate: Timestamp;
      description: string;
      documentId?: string;
    }>;
  };

  // Verification status
  verification: {
    status: 'unverified' | 'pending' | 'verified' | 'disputed' | 'rejected';
    verifiedBy?: string;         // Verifier user ID
    verifiedAt?: Timestamp;
    verificationMethod: string;

    // Dispute information
    dispute?: {
      raisedBy: string;
      raisedAt: Timestamp;
      reason: string;
      evidence: string[];
      status: 'open' | 'investigating' | 'resolved';
    };
  };

  // Timestamps
  createdAt: Timestamp;
  updatedAt: Timestamp;

  // Metadata
  metadata: {
    recordedBy: string;          // User who recorded the relationship
    source: 'breeding_record' | 'manual_entry' | 'dna_test' | 'import' | 'inference';

    // Quality indicators
    dataQuality: 'high' | 'medium' | 'low';
    completeness: number;        // 0-1 scale

    // Research value
    researchValue: 'high' | 'medium' | 'low';
    publiclyVisible: boolean;

    tags: string[];
    notes?: string;
  };
}
```

### 3. conversations/{conversationId} Subcollections

#### 3.1 conversations/{conversationId}/messages/{messageId}
**Purpose**: Individual messages within conversations
**Relationship**: One-to-many (Conversation → Messages)

```typescript
interface ConversationMessageDocument {
  // Message identification
  messageId: string;             // Auto-generated
  conversationId: string;        // Parent conversation ID

  // Message content (detailed version of main messages collection)
  content: {
    type: 'text' | 'image' | 'video' | 'audio' | 'file' | 'location' | 'fowl_card' | 'listing_card' | 'system';
    text?: string;

    // Rich content
    richContent?: {
      formatting: Array<{
        type: 'bold' | 'italic' | 'underline' | 'link' | 'mention';
        start: number;
        end: number;
        data?: any;              // Additional data for links, mentions
      }>;

      mentions?: Array<{
        userId: string;
        displayName: string;
        start: number;
        end: number;
      }>;

      links?: Array<{
        url: string;
        title?: string;
        description?: string;
        imageUrl?: string;
        start: number;
        end: number;
      }>;
    };

    // Media content
    media?: {
      url: string;
      thumbnailUrl?: string;
      filename?: string;
      size?: number;
      mimeType?: string;
      duration?: number;
      dimensions?: {
        width: number;
        height: number;
      };
    };

    // Special content types
    specialContent?: {
      fowlCard?: {
        fowlId: string;
        fowlData: any;           // Cached fowl data
      };

      listingCard?: {
        listingId: string;
        listingData: any;        // Cached listing data
      };

      location?: {
        latitude: number;
        longitude: number;
        address?: string;
        placeName?: string;
      };

      systemMessage?: {
        type: 'user_joined' | 'user_left' | 'title_changed' | 'photo_changed';
        data: any;
      };
    };
  };

  // Message metadata
  sender: {
    userId: string;
    displayName: string;
    photoURL?: string;
    userTier: 'general' | 'farmer' | 'enthusiast';
  };

  // Message status and delivery
  status: {
    sent: boolean;
    delivered: boolean;

    // Read receipts per participant
    readBy: Array<{
      userId: string;
      readAt: Timestamp;
    }>;

    // Delivery tracking
    deliveryAttempts: number;
    lastDeliveryAttempt?: Timestamp;
    deliveryErrors?: string[];
  };

  // Message threading
  thread?: {
    isReply: boolean;
    replyToMessageId?: string;
    threadId?: string;
    threadPosition: number;

    // Thread summary
    threadSummary?: {
      totalReplies: number;
      lastReplyAt: Timestamp;
      participants: string[];    // User IDs who participated in thread
    };
  };

  // Message reactions and interactions
  reactions: {
    [emoji: string]: Array<{
      userId: string;
      reactedAt: Timestamp;
    }>;
  };

  interactions: {
    edited: boolean;
    editCount: number;
    lastEditedAt?: Timestamp;
    editHistory?: Array<{
      content: string;
      editedAt: Timestamp;
      reason?: string;
    }>;

    forwarded: boolean;
    forwardCount: number;
    originalMessageId?: string;

    pinned: boolean;
    pinnedBy?: string;
    pinnedAt?: Timestamp;

    starred: boolean;
    starredBy: string[];         // User IDs who starred
  };

  // Moderation and safety
  moderation: {
    flagged: boolean;
    flaggedBy?: string[];
    flaggedAt?: Timestamp;
    flagReason?: string;

    moderationStatus: 'pending' | 'approved' | 'rejected' | 'auto_approved';
    moderatedBy?: string;
    moderatedAt?: Timestamp;
    moderationAction?: 'none' | 'warning' | 'edit' | 'delete' | 'ban_user';

    // Content analysis
    contentAnalysis?: {
      spamScore: number;         // 0-1
      toxicityScore: number;     // 0-1
      languageDetected: string;
      sentimentScore: number;    // -1 to 1

      // Content flags
      containsPersonalInfo: boolean;
      containsProfanity: boolean;
      containsSpam: boolean;
      containsHateSpeech: boolean;
    };
  };

  // Encryption and privacy
  encryption?: {
    encrypted: boolean;
    encryptionMethod?: string;
    keyId?: string;

    // End-to-end encryption
    e2eEncrypted: boolean;
    senderKeyId?: string;
    recipientKeyIds?: string[];
  };

  // Message priority and urgency
  priority: 'low' | 'normal' | 'high' | 'urgent';

  // Timestamps
  sentAt: Timestamp;
  editedAt?: Timestamp;
  deletedAt?: Timestamp;
  expiresAt?: Timestamp;         // For disappearing messages

  // Metadata
  metadata: {
    source: 'mobile' | 'web' | 'api' | 'bot';
    deviceInfo?: string;
    appVersion?: string;

    // Message context
    context?: {
      relatedToListing?: string;
      relatedToFowl?: string;
      relatedToTransfer?: string;
      relatedToBreeding?: string;
    };

    // Analytics
    messageSize: number;
    processingTime?: number;
    deliveryLatency?: number;

    // Offline support
    offlineQueued: boolean;
    syncStatus: 'synced' | 'pending' | 'failed';
    syncRetries: number;

    tags: string[];
    notes?: string;
  };
}
```

#### 3.2 conversations/{conversationId}/participants/{participantId}
**Purpose**: Participant management and permissions
**Relationship**: One-to-many (Conversation → Participants)

```typescript
interface ConversationParticipantDocument {
  // Participant identification
  participantId: string;         // User ID
  conversationId: string;        // Parent conversation ID

  // Participant details
  user: {
    userId: string;
    displayName: string;
    photoURL?: string;
    userTier: 'general' | 'farmer' | 'enthusiast';

    // Contact information
    email?: string;
    phoneNumber?: string;
    location?: {
      district: string;
      region: string;
    };
  };

  // Participation status
  status: {
    current: 'active' | 'left' | 'removed' | 'banned' | 'muted';
    joinedAt: Timestamp;
    leftAt?: Timestamp;
    lastActiveAt: Timestamp;

    // Activity metrics
    messageCount: number;
    lastMessageAt?: Timestamp;
    averageResponseTime?: number; // In minutes
  };

  // Role and permissions
  role: {
    type: 'owner' | 'admin' | 'moderator' | 'member' | 'guest';
    assignedBy: string;          // User ID who assigned role
    assignedAt: Timestamp;

    // Specific permissions
    permissions: {
      canSendMessages: boolean;
      canSendMedia: boolean;
      canAddParticipants: boolean;
      canRemoveParticipants: boolean;
      canChangeTitle: boolean;
      canChangePhoto: boolean;
      canPinMessages: boolean;
      canDeleteMessages: boolean;
      canMuteParticipants: boolean;
    };
  };

  // Participant preferences
  preferences: {
    notifications: {
      enabled: boolean;
      sound: boolean;
      vibration: boolean;
      mentions: boolean;
      keywords: string[];        // Keywords to notify on
    };

    privacy: {
      readReceipts: boolean;
      typingIndicators: boolean;
      lastSeenStatus: boolean;
      profileVisibility: 'public' | 'participants' | 'private';
    };

    // Display preferences
    display: {
      messagePreview: boolean;
      mediaAutoDownload: boolean;
      fontSize: 'small' | 'medium' | 'large';
      theme: 'light' | 'dark' | 'auto';
    };
  };

  // Read tracking
  readTracking: {
    lastReadMessageId?: string;
    lastReadAt?: Timestamp;
    unreadCount: number;
    mentionCount: number;

    // Read history (for analytics)
    readHistory: Array<{
      messageId: string;
      readAt: Timestamp;
      timeToRead?: number;       // Seconds from message sent to read
    }>;
  };

  // Interaction history
  interactions: {
    // Message interactions
    messagesSent: number;
    messagesReceived: number;
    reactionsGiven: number;
    reactionsReceived: number;

    // Media sharing
    photosShared: number;
    videosShared: number;
    filesShared: number;

    // Engagement metrics
    averageSessionDuration: number; // Minutes
    totalTimeSpent: number;      // Minutes
    lastEngagementAt: Timestamp;
  };

  // Moderation history
  moderation?: {
    warnings: Array<{
      warningId: string;
      reason: string;
      issuedBy: string;
      issuedAt: Timestamp;
      acknowledged: boolean;
    }>;

    mutes: Array<{
      muteId: string;
      reason: string;
      mutedBy: string;
      mutedAt: Timestamp;
      unmutedAt?: Timestamp;
      duration?: number;         // Minutes
    }>;

    violations: Array<{
      violationId: string;
      type: string;
      description: string;
      severity: 'low' | 'medium' | 'high';
      actionTaken: string;
      reportedAt: Timestamp;
    }>;
  };

  // Timestamps
  createdAt: Timestamp;
  updatedAt: Timestamp;

  // Metadata
  metadata: {
    invitedBy?: string;          // User ID who invited
    inviteMethod?: 'direct' | 'link' | 'qr_code' | 'phone_contact';

    // Source tracking
    joinSource: 'invitation' | 'link' | 'search' | 'recommendation';
    referralCode?: string;

    // Analytics
    engagementScore: number;     // 0-1 based on activity
    influenceScore: number;      // 0-1 based on message impact

    // Device and platform
    primaryDevice: 'mobile' | 'web' | 'desktop';
    lastUsedDevice: string;

    tags: string[];
    notes?: string;
  };
}
```