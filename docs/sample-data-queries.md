# RIO Sample Data & Query Examples

## Overview

Comprehensive collection of realistic sample documents and optimized query patterns for the RIO platform. Includes performance analysis and best practices for 600K+ users across rural and urban India.

## Sample Documents

### 1. Sample User Documents

#### Farmer User (Tier: farmer)
```json
{
  "uid": "farmer_001_guntur",
  "email": "ramesh.poultry@gmail.com",
  "phoneNumber": "+919876543210",
  "displayName": "Ramesh Kumar",
  "photoURL": "https://storage.googleapis.com/rio-platform/users/farmer_001_guntur/profile.jpg",

  "region": "andhra_pradesh",
  "district": "guntur",
  "mandal": "tenali",
  "village": "cherukupalli",
  "pincode": "522201",
  "coordinates": {
    "latitude": 16.2393,
    "longitude": 80.6461
  },

  "tier": "farmer",
  "permissions": {
    "canCreateListings": true,
    "canEditListings": true,
    "canDeleteListings": true,
    "canAccessMarketplace": true,
    "canManageBreedingRecords": true,
    "canAccessAnalytics": false,
    "canAccessPremiumFeatures": false,
    "canVerifyTransfers": false,
    "canAccessPrioritySupport": false,
    "canModerateContent": false
  },

  "verificationStatus": {
    "level": "enhanced",
    "emailVerified": true,
    "phoneVerified": true,
    "identityVerified": true,
    "farmDocumentsVerified": true,
    "referencesVerified": false,
    "verifiedAt": "2024-01-15T10:30:00Z",
    "verifiedBy": "admin_001"
  },

  "farmDetails": {
    "farmName": "Sri Lakshmi Poultry Farm",
    "farmType": "commercial",
    "establishedYear": 2018,
    "totalArea": 2.5,
    "fowlCapacity": 500,
    "specializations": ["Aseel", "Kadaknath", "Desi"],
    "certifications": ["organic", "free_range"]
  },

  "language": "te",
  "preferences": {
    "notifications": {
      "email": true,
      "sms": true,
      "push": true
    },
    "privacy": {
      "showLocation": true,
      "showPhoneNumber": false,
      "allowDirectMessages": true
    },
    "marketplace": {
      "autoRenewListings": true,
      "priceAlerts": true,
      "favoriteBreeds": ["Aseel", "Kadaknath"]
    }
  },

  "stats": {
    "totalFowls": 45,
    "totalListings": 12,
    "totalSales": 28,
    "totalPurchases": 8,
    "totalMessages": 156,
    "rating": 4.7,
    "reviewCount": 23
  },

  "limits": {
    "maxListings": 50,
    "maxPhotosPerListing": 10,
    "maxBreedingRecords": 100,
    "dailyMessageLimit": 200
  },

  "createdAt": "2024-01-10T08:15:00Z",
  "lastLoginAt": "2024-08-08T14:30:00Z",
  "lastActiveAt": "2024-08-08T15:45:00Z",
  "updatedAt": "2024-08-08T15:45:00Z",

  "metadata": {
    "deviceInfo": "Samsung Galaxy A14 5G",
    "appVersion": "1.2.3",
    "registrationSource": "android",
    "referredBy": null,
    "tierUpgradeHistory": [
      {
        "fromTier": "general",
        "toTier": "farmer",
        "upgradedAt": "2024-01-15T10:30:00Z",
        "reason": "Farm verification completed"
      }
    ]
  }
}
```

#### Enthusiast User (Tier: enthusiast)
```json
{
  "uid": "enthusiast_001_hyderabad",
  "email": "priya.gamebirds@outlook.com",
  "phoneNumber": "+919123456789",
  "displayName": "Priya Sharma",
  "photoURL": "https://storage.googleapis.com/rio-platform/users/enthusiast_001_hyderabad/profile.jpg",

  "region": "telangana",
  "district": "hyderabad",
  "mandal": "secunderabad",
  "village": null,
  "pincode": "500003",
  "coordinates": {
    "latitude": 17.4399,
    "longitude": 78.4983
  },

  "tier": "enthusiast",
  "permissions": {
    "canCreateListings": true,
    "canEditListings": true,
    "canDeleteListings": true,
    "canAccessMarketplace": true,
    "canManageBreedingRecords": true,
    "canAccessAnalytics": true,
    "canAccessPremiumFeatures": true,
    "canVerifyTransfers": true,
    "canAccessPrioritySupport": true,
    "canModerateContent": false
  },

  "verificationStatus": {
    "level": "premium",
    "emailVerified": true,
    "phoneVerified": true,
    "identityVerified": true,
    "farmDocumentsVerified": true,
    "referencesVerified": true,
    "verifiedAt": "2024-02-20T16:45:00Z",
    "verifiedBy": "admin_002"
  },

  "farmDetails": {
    "farmName": "Heritage Game Birds",
    "farmType": "breeding",
    "establishedYear": 2020,
    "totalArea": 1.2,
    "fowlCapacity": 150,
    "specializations": ["Aseel", "Shamo", "Thai Game"],
    "certifications": ["breeding_excellence", "genetic_preservation"]
  },

  "language": "en",
  "preferences": {
    "notifications": {
      "email": true,
      "sms": false,
      "push": true
    },
    "privacy": {
      "showLocation": true,
      "showPhoneNumber": true,
      "allowDirectMessages": true
    },
    "marketplace": {
      "autoRenewListings": false,
      "priceAlerts": true,
      "favoriteBreeds": ["Aseel", "Shamo", "Thai Game", "Malay"]
    }
  },

  "stats": {
    "totalFowls": 32,
    "totalListings": 8,
    "totalSales": 15,
    "totalPurchases": 12,
    "totalMessages": 89,
    "rating": 4.9,
    "reviewCount": 18
  },

  "limits": {
    "maxListings": 200,
    "maxPhotosPerListing": 25,
    "maxBreedingRecords": 500,
    "dailyMessageLimit": 1000
  },

  "createdAt": "2024-02-15T11:20:00Z",
  "lastLoginAt": "2024-08-08T09:15:00Z",
  "lastActiveAt": "2024-08-08T16:20:00Z",
  "updatedAt": "2024-08-08T16:20:00Z",

  "metadata": {
    "deviceInfo": "iPhone 14 Pro",
    "appVersion": "1.2.3",
    "registrationSource": "web",
    "referredBy": "farmer_001_guntur",
    "tierUpgradeHistory": [
      {
        "fromTier": "general",
        "toTier": "farmer",
        "upgradedAt": "2024-02-18T14:30:00Z",
        "reason": "Farm verification completed"
      },
      {
        "fromTier": "farmer",
        "toTier": "enthusiast",
        "upgradedAt": "2024-02-20T16:45:00Z",
        "reason": "Premium verification with references"
      }
    ]
  }
}
```

### 2. Sample Fowl Documents

#### Premium Aseel Rooster
```json
{
  "fowlId": "aseel_rooster_001",
  "ownerId": "farmer_001_guntur",
  "name": "Raja",

  "breed": {
    "primary": "Aseel",
    "secondary": null,
    "purity": 95,
    "variety": "Reza Aseel"
  },

  "gender": "male",

  "physicalTraits": {
    "color": "Dark Red",
    "weight": 3.2,
    "height": 45,
    "combType": "Pea",
    "legColor": "Yellow",
    "eyeColor": "Red",
    "distinguishingMarks": "White patch on chest"
  },

  "birthDate": "2023-03-15T00:00:00Z",
  "ageCategory": "adult",

  "lineage": {
    "fatherId": "aseel_rooster_father_001",
    "motherId": "aseel_hen_mother_001",
    "generation": 3,
    "bloodline": "Champion Reza Line",
    "inbreedingCoefficient": 0.125
  },

  "origin": {
    "region": "andhra_pradesh",
    "district": "guntur",
    "farmId": "farm_001_guntur",
    "coordinates": {
      "latitude": 16.2393,
      "longitude": 80.6461
    }
  },

  "status": {
    "health": "excellent",
    "availability": "available",
    "location": {
      "currentFarm": "farm_001_guntur",
      "district": "guntur",
      "lastUpdated": "2024-08-08T10:00:00Z"
    }
  },

  "performance": {
    "breeding": {
      "totalOffspring": 23,
      "successfulMatings": 8,
      "lastBreeding": "2024-07-15T00:00:00Z"
    },
    "fighting": {
      "wins": 12,
      "losses": 2,
      "draws": 1,
      "retired": false
    }
  },

  "documentation": {
    "registrationNumber": "ASEEL-AP-2023-001",
    "microchipId": "982000123456789",
    "tattooId": null,
    "certificates": [
      "https://storage.googleapis.com/rio-platform/certificates/aseel_rooster_001_birth.pdf",
      "https://storage.googleapis.com/rio-platform/certificates/aseel_rooster_001_health.pdf"
    ],
    "pedigreeChart": "https://storage.googleapis.com/rio-platform/pedigree/aseel_rooster_001_pedigree.pdf"
  },

  "media": {
    "primaryPhoto": "https://storage.googleapis.com/rio-platform/fowls/aseel_rooster_001/primary.jpg",
    "photoCount": 8,
    "videoCount": 2,
    "lastPhotoAdded": "2024-08-05T14:30:00Z"
  },

  "ownershipHistory": [
    {
      "ownerId": "farmer_001_guntur",
      "transferId": null,
      "acquiredDate": "2023-03-15T00:00:00Z",
      "transferredDate": null,
      "price": null,
      "currency": "INR"
    }
  ],

  "createdAt": "2023-03-15T08:30:00Z",
  "updatedAt": "2024-08-08T10:00:00Z",
  "lastHealthCheck": "2024-07-20T09:00:00Z",

  "searchTerms": ["aseel", "rooster", "male", "reza", "champion", "breeding", "guntur"],
  "tags": ["champion_bloodline", "proven_breeder", "show_quality"],

  "metadata": {
    "dataQuality": "high",
    "verificationLevel": "premium",
    "lastVerifiedBy": "enthusiast_001_hyderabad",
    "importSource": null,
    "notes": "Exceptional breeding rooster with proven track record"
  }
}
```

## Common Query Patterns & Performance Analysis

### 1. User Discovery Queries

#### Find Active Farmers in a Region
```javascript
// Query: Find active farmers in Andhra Pradesh, Guntur district
const query = db.collection('users')
  .where('tier', '==', 'farmer')
  .where('region', '==', 'andhra_pradesh')
  .where('district', '==', 'guntur')
  .where('lastActiveAt', '>', thirtyDaysAgo)
  .orderBy('lastActiveAt', 'desc')
  .limit(20);

// Performance Analysis:
// - Uses composite index: tier + region + district + lastActiveAt
// - Expected response time: 50-75ms
// - Suitable for real-time user discovery
// - Efficient pagination with cursor-based approach

const results = await query.get();
console.log(`Found ${results.size} active farmers in Guntur`);
```

#### Search Enthusiasts by Specialization
```javascript
// Query: Find enthusiasts specializing in Aseel breeding
const query = db.collection('users')
  .where('tier', '==', 'enthusiast')
  .where('farmDetails.specializations', 'array-contains', 'Aseel')
  .where('verificationStatus.level', '==', 'premium')
  .orderBy('stats.rating', 'desc')
  .limit(10);

// Performance Analysis:
// - Uses composite index: tier + farmDetails.specializations + verificationStatus.level + stats.rating
// - Expected response time: 60-80ms
// - Array-contains queries are efficient for specialization searches
// - Results sorted by rating for quality assurance

const enthusiasts = await query.get();
enthusiasts.forEach(doc => {
  const user = doc.data();
  console.log(`${user.displayName} - Rating: ${user.stats.rating}`);
});
```

### 2. Marketplace Search Queries

#### Regional Marketplace Browse
```javascript
// Query: Browse active listings in Telangana with price filter
const query = db.collection('marketplace')
  .where('status.current', '==', 'active')
  .where('location.region', '==', 'telangana')
  .where('pricing.basePrice', '>=', 5000)
  .where('pricing.basePrice', '<=', 25000)
  .orderBy('pricing.basePrice', 'asc')
  .orderBy('timeline.publishedAt', 'desc')
  .limit(50);

// Performance Analysis:
// - Uses composite index: status.current + location.region + pricing.basePrice + timeline.publishedAt
// - Expected response time: 75-100ms
// - Efficient for regional marketplace browsing
// - Price range filtering with secondary sort by recency

const listings = await query.get();
console.log(`Found ${listings.size} listings in price range ₹5,000-₹25,000`);
```

#### Breed-Specific Search with Location
```javascript
// Query: Find Aseel roosters in Andhra Pradesh under ₹15,000
const query = db.collection('marketplace')
  .where('status.current', '==', 'active')
  .where('details.breed', '==', 'Aseel')
  .where('details.gender', '==', 'male')
  .where('location.region', '==', 'andhra_pradesh')
  .where('pricing.basePrice', '<=', 15000)
  .orderBy('pricing.basePrice', 'asc')
  .limit(25);

// Performance Analysis:
// - Uses composite index: status.current + details.breed + details.gender + location.region + pricing.basePrice
// - Expected response time: 80-120ms
// - Highly specific search with multiple filters
// - Optimized for mobile app search functionality

const aseelRoosters = await query.get();
aseelRoosters.forEach(doc => {
  const listing = doc.data();
  console.log(`${listing.details.title} - ₹${listing.pricing.basePrice} in ${listing.location.district}`);
});
```

### 3. Fowl Lineage Queries

#### Find Offspring of a Breeding Pair
```javascript
// Query: Find all offspring of a specific rooster
const sireId = 'aseel_rooster_001';
const query = db.collection('fowls')
  .where('lineage.fatherId', '==', sireId)
  .orderBy('birthDate', 'desc')
  .limit(100);

// Performance Analysis:
// - Uses single-field index: lineage.fatherId + birthDate
// - Expected response time: 30-50ms
// - Essential for lineage tracking and breeding records
// - Efficient for family tree construction

const offspring = await query.get();
console.log(`Found ${offspring.size} offspring of ${sireId}`);

// Advanced lineage query: Find siblings
const siblingQuery = db.collection('fowls')
  .where('lineage.fatherId', '==', 'aseel_rooster_father_001')
  .where('lineage.motherId', '==', 'aseel_hen_mother_001')
  .orderBy('birthDate', 'asc');

const siblings = await siblingQuery.get();
console.log(`Found ${siblings.size} siblings in the same clutch`);
```

#### Multi-Generation Lineage Tracking
```javascript
// Query: Find all fowls in a specific bloodline across generations
const bloodline = 'Champion Reza Line';
const query = db.collection('fowls')
  .where('lineage.bloodline', '==', bloodline)
  .where('status.availability', 'in', ['available', 'breeding'])
  .orderBy('lineage.generation', 'asc')
  .orderBy('birthDate', 'desc')
  .limit(200);

// Performance Analysis:
// - Uses composite index: lineage.bloodline + status.availability + lineage.generation + birthDate
// - Expected response time: 100-150ms
// - Complex query for genetic research and breeding planning
// - Suitable for enthusiast-level lineage analysis

const bloodlineMembers = await query.get();
const generationMap = new Map();

bloodlineMembers.forEach(doc => {
  const fowl = doc.data();
  const gen = fowl.lineage.generation;
  if (!generationMap.has(gen)) {
    generationMap.set(gen, []);
  }
  generationMap.get(gen).push(fowl);
});

console.log(`Bloodline spans ${generationMap.size} generations`);
```

### 4. Real-time Messaging Queries

#### Conversation Message History
```javascript
// Query: Load recent messages in a conversation
const conversationId = 'conv_farmer_enthusiast_001';
const query = db.collection('messages')
  .where('conversationId', '==', conversationId)
  .orderBy('sentAt', 'desc')
  .limit(50);

// Performance Analysis:
// - Uses composite index: conversationId + sentAt
// - Expected response time: 25-40ms
// - Optimized for real-time chat functionality
// - Efficient pagination for message history

// Real-time listener for new messages
const unsubscribe = query.onSnapshot(snapshot => {
  snapshot.docChanges().forEach(change => {
    if (change.type === 'added') {
      const message = change.doc.data();
      console.log(`New message from ${message.senderId}: ${message.content.text}`);
    }
  });
});

// Performance: ~20ms per new message update
```

#### Unread Messages Across Conversations
```javascript
// Query: Find all unread messages for a user
const userId = 'farmer_001_guntur';
const query = db.collection('messages')
  .where('recipientId', '==', userId)
  .where('status.read', '==', false)
  .orderBy('sentAt', 'desc')
  .limit(100);

// Performance Analysis:
// - Uses composite index: recipientId + status.read + sentAt
// - Expected response time: 40-60ms
// - Critical for notification systems
// - Efficient for mobile app badge counts

const unreadMessages = await query.get();
console.log(`${unreadMessages.size} unread messages`);

// Group by conversation for UI display
const conversationGroups = new Map();
unreadMessages.forEach(doc => {
  const message = doc.data();
  const convId = message.conversationId;
  if (!conversationGroups.has(convId)) {
    conversationGroups.set(convId, []);
  }
  conversationGroups.get(convId).push(message);
});
```

### 5. Transfer and Transaction Queries

#### User Transfer History
```javascript
// Query: Get complete transfer history for a user
const userId = 'farmer_001_guntur';

// Transfers sent by user
const sentTransfers = db.collection('transfers')
  .where('fromUserId', '==', userId)
  .orderBy('timeline.initiatedAt', 'desc')
  .limit(50);

// Transfers received by user
const receivedTransfers = db.collection('transfers')
  .where('toUserId', '==', userId)
  .orderBy('timeline.initiatedAt', 'desc')
  .limit(50);

// Performance Analysis:
// - Uses composite indexes: fromUserId + timeline.initiatedAt, toUserId + timeline.initiatedAt
// - Expected response time: 60-80ms for both queries
// - Essential for transaction history and audit trails
// - Parallel execution for complete user history

const [sentResults, receivedResults] = await Promise.all([
  sentTransfers.get(),
  receivedTransfers.get()
]);

console.log(`Sent: ${sentResults.size}, Received: ${receivedResults.size} transfers`);
```

#### Pending Verification Queue
```javascript
// Query: Get transfers pending verification (for enthusiasts/admins)
const query = db.collection('transfers')
  .where('verification.status', '==', 'pending')
  .where('transferDetails.price', '>=', 10000) // High-value transfers
  .orderBy('transferDetails.price', 'desc')
  .orderBy('timeline.initiatedAt', 'asc')
  .limit(25);

// Performance Analysis:
// - Uses composite index: verification.status + transferDetails.price + timeline.initiatedAt
// - Expected response time: 70-90ms
// - Critical for verification workflow management
// - Prioritizes high-value transfers

const pendingVerifications = await query.get();
console.log(`${pendingVerifications.size} transfers pending verification`);
```

### 6. Performance Optimization Strategies

#### Efficient Pagination
```javascript
// Cursor-based pagination for large datasets
class EfficientPagination {
  constructor(baseQuery, pageSize = 20) {
    this.baseQuery = baseQuery;
    this.pageSize = pageSize;
    this.lastDoc = null;
  }

  async getFirstPage() {
    const query = this.baseQuery.limit(this.pageSize);
    const snapshot = await query.get();

    if (!snapshot.empty) {
      this.lastDoc = snapshot.docs[snapshot.docs.length - 1];
    }

    return {
      docs: snapshot.docs,
      hasMore: snapshot.size === this.pageSize,
      performance: {
        size: snapshot.size,
        fromCache: snapshot.metadata.fromCache
      }
    };
  }

  async getNextPage() {
    if (!this.lastDoc) return { docs: [], hasMore: false };

    const query = this.baseQuery
      .startAfter(this.lastDoc)
      .limit(this.pageSize);

    const snapshot = await query.get();

    if (!snapshot.empty) {
      this.lastDoc = snapshot.docs[snapshot.docs.length - 1];
    }

    return {
      docs: snapshot.docs,
      hasMore: snapshot.size === this.pageSize,
      performance: {
        size: snapshot.size,
        fromCache: snapshot.metadata.fromCache
      }
    };
  }
}

// Usage example
const marketplacePagination = new EfficientPagination(
  db.collection('marketplace')
    .where('status.current', '==', 'active')
    .where('location.region', '==', 'andhra_pradesh')
    .orderBy('timeline.publishedAt', 'desc'),
  25
);

const firstPage = await marketplacePagination.getFirstPage();
console.log(`First page: ${firstPage.docs.length} listings, from cache: ${firstPage.performance.fromCache}`);
```

#### Batch Operations for Efficiency
```javascript
// Efficient batch reading for related documents
async function getBatchFowlDetails(fowlIds) {
  const batchSize = 10; // Firestore limit
  const batches = [];

  for (let i = 0; i < fowlIds.length; i += batchSize) {
    const batch = fowlIds.slice(i, i + batchSize);
    const query = db.collection('fowls')
      .where(FieldPath.documentId(), 'in', batch);

    batches.push(query.get());
  }

  const results = await Promise.all(batches);
  const fowls = [];

  results.forEach(snapshot => {
    snapshot.forEach(doc => {
      fowls.push({ id: doc.id, ...doc.data() });
    });
  });

  return fowls;
}

// Performance: ~100ms for 50 fowls vs 2500ms for individual queries
const fowlIds = ['fowl_001', 'fowl_002', /* ... up to 50 IDs */];
const fowlDetails = await getBatchFowlDetails(fowlIds);
console.log(`Retrieved ${fowlDetails.length} fowl details efficiently`);
```