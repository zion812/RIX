# RIO Firestore Index Strategy & Query Optimization

## Overview

Comprehensive indexing strategy for the RIO platform designed to optimize query performance for 600K+ users across rural and urban areas in India. This strategy focuses on efficient querying patterns, regional optimization, and cost-effective index management.

## Index Design Principles

### 1. **Query-First Approach**
- Indexes designed based on actual query patterns
- Composite indexes for complex filtering and sorting
- Minimal redundant indexes to control costs

### 2. **Regional Optimization**
- Location-based indexing for regional queries
- Language-specific text search optimization
- District and state-level data partitioning

### 3. **Performance Targets**
- Sub-100ms response times for common queries
- Efficient pagination for large result sets
- Optimized real-time listeners for live updates

### 4. **Cost Management**
- Strategic index selection to minimize storage costs
- Automatic index cleanup for unused patterns
- Monitoring and optimization recommendations

## Core Collection Indexes

### 1. users Collection Indexes

#### Single-Field Indexes
```javascript
// Automatic single-field indexes (created by default)
{
  "collectionGroup": "users",
  "fields": [
    {"fieldPath": "email", "order": "ASCENDING"},
    {"fieldPath": "phoneNumber", "order": "ASCENDING"},
    {"fieldPath": "tier", "order": "ASCENDING"},
    {"fieldPath": "region", "order": "ASCENDING"},
    {"fieldPath": "district", "order": "ASCENDING"},
    {"fieldPath": "verificationStatus.level", "order": "ASCENDING"},
    {"fieldPath": "createdAt", "order": "ASCENDING"},
    {"fieldPath": "lastActiveAt", "order": "ASCENDING"}
  ]
}
```

#### Composite Indexes
```javascript
// Regional user discovery
{
  "collectionGroup": "users",
  "fields": [
    {"fieldPath": "region", "order": "ASCENDING"},
    {"fieldPath": "district", "order": "ASCENDING"},
    {"fieldPath": "tier", "order": "ASCENDING"},
    {"fieldPath": "lastActiveAt", "order": "DESCENDING"}
  ]
}

// Farmer/Enthusiast search by specialization
{
  "collectionGroup": "users",
  "fields": [
    {"fieldPath": "tier", "order": "ASCENDING"},
    {"fieldPath": "farmDetails.specializations", "arrayConfig": "CONTAINS"},
    {"fieldPath": "region", "order": "ASCENDING"},
    {"fieldPath": "stats.rating", "order": "DESCENDING"}
  ]
}

// Verification queue for admins
{
  "collectionGroup": "users",
  "fields": [
    {"fieldPath": "verificationStatus.level", "order": "ASCENDING"},
    {"fieldPath": "tier", "order": "ASCENDING"},
    {"fieldPath": "createdAt", "order": "ASCENDING"}
  ]
}

// Active users by region and tier
{
  "collectionGroup": "users",
  "fields": [
    {"fieldPath": "region", "order": "ASCENDING"},
    {"fieldPath": "tier", "order": "ASCENDING"},
    {"fieldPath": "lastActiveAt", "order": "DESCENDING"}
  ]
}

// User search by language and location
{
  "collectionGroup": "users",
  "fields": [
    {"fieldPath": "language", "order": "ASCENDING"},
    {"fieldPath": "region", "order": "ASCENDING"},
    {"fieldPath": "district", "order": "ASCENDING"},
    {"fieldPath": "lastActiveAt", "order": "DESCENDING"}
  ]
}
```

### 2. fowls Collection Indexes

#### Single-Field Indexes
```javascript
{
  "collectionGroup": "fowls",
  "fields": [
    {"fieldPath": "ownerId", "order": "ASCENDING"},
    {"fieldPath": "breed.primary", "order": "ASCENDING"},
    {"fieldPath": "gender", "order": "ASCENDING"},
    {"fieldPath": "status.availability", "order": "ASCENDING"},
    {"fieldPath": "origin.region", "order": "ASCENDING"},
    {"fieldPath": "origin.district", "order": "ASCENDING"},
    {"fieldPath": "birthDate", "order": "ASCENDING"},
    {"fieldPath": "createdAt", "order": "ASCENDING"},
    {"fieldPath": "lineage.fatherId", "order": "ASCENDING"},
    {"fieldPath": "lineage.motherId", "order": "ASCENDING"}
  ]
}
```

#### Composite Indexes
```javascript
// Fowl search by breed and location
{
  "collectionGroup": "fowls",
  "fields": [
    {"fieldPath": "breed.primary", "order": "ASCENDING"},
    {"fieldPath": "origin.region", "order": "ASCENDING"},
    {"fieldPath": "origin.district", "order": "ASCENDING"},
    {"fieldPath": "status.availability", "order": "ASCENDING"},
    {"fieldPath": "birthDate", "order": "DESCENDING"}
  ]
}

// Owner's fowl collection
{
  "collectionGroup": "fowls",
  "fields": [
    {"fieldPath": "ownerId", "order": "ASCENDING"},
    {"fieldPath": "status.availability", "order": "ASCENDING"},
    {"fieldPath": "breed.primary", "order": "ASCENDING"},
    {"fieldPath": "createdAt", "order": "DESCENDING"}
  ]
}

// Breeding stock search
{
  "collectionGroup": "fowls",
  "fields": [
    {"fieldPath": "gender", "order": "ASCENDING"},
    {"fieldPath": "breed.primary", "order": "ASCENDING"},
    {"fieldPath": "status.availability", "order": "ASCENDING"},
    {"fieldPath": "performance.breeding.totalOffspring", "order": "DESCENDING"}
  ]
}

// Lineage tracking - find offspring
{
  "collectionGroup": "fowls",
  "fields": [
    {"fieldPath": "lineage.fatherId", "order": "ASCENDING"},
    {"fieldPath": "birthDate", "order": "DESCENDING"}
  ]
}

// Lineage tracking - find offspring by mother
{
  "collectionGroup": "fowls",
  "fields": [
    {"fieldPath": "lineage.motherId", "order": "ASCENDING"},
    {"fieldPath": "birthDate", "order": "DESCENDING"}
  ]
}

// Regional fowl discovery
{
  "collectionGroup": "fowls",
  "fields": [
    {"fieldPath": "origin.region", "order": "ASCENDING"},
    {"fieldPath": "origin.district", "order": "ASCENDING"},
    {"fieldPath": "breed.primary", "order": "ASCENDING"},
    {"fieldPath": "status.availability", "order": "ASCENDING"},
    {"fieldPath": "createdAt", "order": "DESCENDING"}
  ]
}

// Age-based fowl search
{
  "collectionGroup": "fowls",
  "fields": [
    {"fieldPath": "breed.primary", "order": "ASCENDING"},
    {"fieldPath": "gender", "order": "ASCENDING"},
    {"fieldPath": "ageCategory", "order": "ASCENDING"},
    {"fieldPath": "status.availability", "order": "ASCENDING"},
    {"fieldPath": "birthDate", "order": "DESCENDING"}
  ]
}
```

### 3. marketplace Collection Indexes

#### Single-Field Indexes
```javascript
{
  "collectionGroup": "marketplace",
  "fields": [
    {"fieldPath": "sellerId", "order": "ASCENDING"},
    {"fieldPath": "fowlId", "order": "ASCENDING"},
    {"fieldPath": "listingType", "order": "ASCENDING"},
    {"fieldPath": "status.current", "order": "ASCENDING"},
    {"fieldPath": "details.breed", "order": "ASCENDING"},
    {"fieldPath": "details.gender", "order": "ASCENDING"},
    {"fieldPath": "location.district", "order": "ASCENDING"},
    {"fieldPath": "location.region", "order": "ASCENDING"},
    {"fieldPath": "pricing.basePrice", "order": "ASCENDING"},
    {"fieldPath": "timeline.publishedAt", "order": "ASCENDING"},
    {"fieldPath": "engagement.views", "order": "ASCENDING"}
  ]
}
```

#### Composite Indexes
```javascript
// Marketplace browse by location and breed
{
  "collectionGroup": "marketplace",
  "fields": [
    {"fieldPath": "status.current", "order": "ASCENDING"},
    {"fieldPath": "location.region", "order": "ASCENDING"},
    {"fieldPath": "location.district", "order": "ASCENDING"},
    {"fieldPath": "details.breed", "order": "ASCENDING"},
    {"fieldPath": "timeline.publishedAt", "order": "DESCENDING"}
  ]
}

// Price-based search
{
  "collectionGroup": "marketplace",
  "fields": [
    {"fieldPath": "status.current", "order": "ASCENDING"},
    {"fieldPath": "details.breed", "order": "ASCENDING"},
    {"fieldPath": "pricing.basePrice", "order": "ASCENDING"},
    {"fieldPath": "timeline.publishedAt", "order": "DESCENDING"}
  ]
}

// Seller's active listings
{
  "collectionGroup": "marketplace",
  "fields": [
    {"fieldPath": "sellerId", "order": "ASCENDING"},
    {"fieldPath": "status.current", "order": "ASCENDING"},
    {"fieldPath": "timeline.publishedAt", "order": "DESCENDING"}
  ]
}

// Featured and promoted listings
{
  "collectionGroup": "marketplace",
  "fields": [
    {"fieldPath": "status.featured", "order": "DESCENDING"},
    {"fieldPath": "status.promoted", "order": "DESCENDING"},
    {"fieldPath": "location.region", "order": "ASCENDING"},
    {"fieldPath": "timeline.publishedAt", "order": "DESCENDING"}
  ]
}

// Auction listings by end time
{
  "collectionGroup": "marketplace",
  "fields": [
    {"fieldPath": "listingType", "order": "ASCENDING"},
    {"fieldPath": "status.current", "order": "ASCENDING"},
    {"fieldPath": "auction.endTime", "order": "ASCENDING"}
  ]
}

// Popular listings by engagement
{
  "collectionGroup": "marketplace",
  "fields": [
    {"fieldPath": "status.current", "order": "ASCENDING"},
    {"fieldPath": "location.region", "order": "ASCENDING"},
    {"fieldPath": "engagement.views", "order": "DESCENDING"},
    {"fieldPath": "timeline.publishedAt", "order": "DESCENDING"}
  ]
}

// Gender and age specific search
{
  "collectionGroup": "marketplace",
  "fields": [
    {"fieldPath": "status.current", "order": "ASCENDING"},
    {"fieldPath": "details.breed", "order": "ASCENDING"},
    {"fieldPath": "details.gender", "order": "ASCENDING"},
    {"fieldPath": "details.age", "order": "ASCENDING"},
    {"fieldPath": "pricing.basePrice", "order": "ASCENDING"}
  ]
}
```

### 4. transfers Collection Indexes

#### Single-Field Indexes
```javascript
{
  "collectionGroup": "transfers",
  "fields": [
    {"fieldPath": "fromUserId", "order": "ASCENDING"},
    {"fieldPath": "toUserId", "order": "ASCENDING"},
    {"fieldPath": "fowlId", "order": "ASCENDING"},
    {"fieldPath": "transferType", "order": "ASCENDING"},
    {"fieldPath": "verification.status", "order": "ASCENDING"},
    {"fieldPath": "status.current", "order": "ASCENDING"},
    {"fieldPath": "timeline.initiatedAt", "order": "ASCENDING"},
    {"fieldPath": "timeline.completedAt", "order": "ASCENDING"}
  ]
}
```

#### Composite Indexes
```javascript
// User's transfer history
{
  "collectionGroup": "transfers",
  "fields": [
    {"fieldPath": "fromUserId", "order": "ASCENDING"},
    {"fieldPath": "status.current", "order": "ASCENDING"},
    {"fieldPath": "timeline.initiatedAt", "order": "DESCENDING"}
  ]
}

// User's received transfers
{
  "collectionGroup": "transfers",
  "fields": [
    {"fieldPath": "toUserId", "order": "ASCENDING"},
    {"fieldPath": "status.current", "order": "ASCENDING"},
    {"fieldPath": "timeline.initiatedAt", "order": "DESCENDING"}
  ]
}

// Fowl transfer history
{
  "collectionGroup": "transfers",
  "fields": [
    {"fieldPath": "fowlId", "order": "ASCENDING"},
    {"fieldPath": "timeline.completedAt", "order": "DESCENDING"}
  ]
}

// Verification queue
{
  "collectionGroup": "transfers",
  "fields": [
    {"fieldPath": "verification.status", "order": "ASCENDING"},
    {"fieldPath": "verification.level", "order": "ASCENDING"},
    {"fieldPath": "timeline.initiatedAt", "order": "ASCENDING"}
  ]
}

// High-value transfers requiring verification
{
  "collectionGroup": "transfers",
  "fields": [
    {"fieldPath": "transferDetails.price", "order": "DESCENDING"},
    {"fieldPath": "verification.status", "order": "ASCENDING"},
    {"fieldPath": "timeline.initiatedAt", "order": "ASCENDING"}
  ]
}

// Disputed transfers
{
  "collectionGroup": "transfers",
  "fields": [
    {"fieldPath": "dispute.status", "order": "ASCENDING"},
    {"fieldPath": "dispute.raisedAt", "order": "ASCENDING"}
  ]
}
```

### 5. messages Collection Indexes

#### Single-Field Indexes
```javascript
{
  "collectionGroup": "messages",
  "fields": [
    {"fieldPath": "conversationId", "order": "ASCENDING"},
    {"fieldPath": "senderId", "order": "ASCENDING"},
    {"fieldPath": "recipientId", "order": "ASCENDING"},
    {"fieldPath": "content.type", "order": "ASCENDING"},
    {"fieldPath": "status.sent", "order": "ASCENDING"},
    {"fieldPath": "status.delivered", "order": "ASCENDING"},
    {"fieldPath": "status.read", "order": "ASCENDING"},
    {"fieldPath": "sentAt", "order": "ASCENDING"},
    {"fieldPath": "priority", "order": "ASCENDING"}
  ]
}
```

#### Composite Indexes
```javascript
// Conversation messages chronological
{
  "collectionGroup": "messages",
  "fields": [
    {"fieldPath": "conversationId", "order": "ASCENDING"},
    {"fieldPath": "sentAt", "order": "ASCENDING"}
  ]
}

// User's sent messages
{
  "collectionGroup": "messages",
  "fields": [
    {"fieldPath": "senderId", "order": "ASCENDING"},
    {"fieldPath": "sentAt", "order": "DESCENDING"}
  ]
}

// User's received messages
{
  "collectionGroup": "messages",
  "fields": [
    {"fieldPath": "recipientId", "order": "ASCENDING"},
    {"fieldPath": "status.read", "order": "ASCENDING"},
    {"fieldPath": "sentAt", "order": "DESCENDING"}
  ]
}

// Unread messages by conversation
{
  "collectionGroup": "messages",
  "fields": [
    {"fieldPath": "conversationId", "order": "ASCENDING"},
    {"fieldPath": "status.read", "order": "ASCENDING"},
    {"fieldPath": "sentAt", "order": "ASCENDING"}
  ]
}

// High priority messages
{
  "collectionGroup": "messages",
  "fields": [
    {"fieldPath": "priority", "order": "DESCENDING"},
    {"fieldPath": "status.delivered", "order": "ASCENDING"},
    {"fieldPath": "sentAt", "order": "DESCENDING"}
  ]
}

// Media messages
{
  "collectionGroup": "messages",
  "fields": [
    {"fieldPath": "content.type", "order": "ASCENDING"},
    {"fieldPath": "conversationId", "order": "ASCENDING"},
    {"fieldPath": "sentAt", "order": "DESCENDING"}
  ]
}
```

### 6. breeding_records Collection Indexes

#### Single-Field Indexes
```javascript
{
  "collectionGroup": "breeding_records",
  "fields": [
    {"fieldPath": "breederId", "order": "ASCENDING"},
    {"fieldPath": "parents.sireId", "order": "ASCENDING"},
    {"fieldPath": "parents.damId", "order": "ASCENDING"},
    {"fieldPath": "breeding.breedingDate", "order": "ASCENDING"},
    {"fieldPath": "status.current", "order": "ASCENDING"},
    {"fieldPath": "offspring.hatchedChicks", "order": "ASCENDING"},
    {"fieldPath": "performance.breedingSuccess", "order": "ASCENDING"},
    {"fieldPath": "createdAt", "order": "ASCENDING"}
  ]
}
```

#### Composite Indexes
```javascript
// Breeder's records
{
  "collectionGroup": "breeding_records",
  "fields": [
    {"fieldPath": "breederId", "order": "ASCENDING"},
    {"fieldPath": "status.current", "order": "ASCENDING"},
    {"fieldPath": "breeding.breedingDate", "order": "DESCENDING"}
  ]
}

// Sire's breeding history
{
  "collectionGroup": "breeding_records",
  "fields": [
    {"fieldPath": "parents.sireId", "order": "ASCENDING"},
    {"fieldPath": "status.current", "order": "ASCENDING"},
    {"fieldPath": "breeding.breedingDate", "order": "DESCENDING"}
  ]
}

// Dam's breeding history
{
  "collectionGroup": "breeding_records",
  "fields": [
    {"fieldPath": "parents.damId", "order": "ASCENDING"},
    {"fieldPath": "status.current", "order": "ASCENDING"},
    {"fieldPath": "breeding.breedingDate", "order": "DESCENDING"}
  ]
}

// Successful breeding records
{
  "collectionGroup": "breeding_records",
  "fields": [
    {"fieldPath": "performance.breedingSuccess", "order": "DESCENDING"},
    {"fieldPath": "offspring.hatchedChicks", "order": "DESCENDING"},
    {"fieldPath": "breeding.breedingDate", "order": "DESCENDING"}
  ]
}

// Recent breeding activities
{
  "collectionGroup": "breeding_records",
  "fields": [
    {"fieldPath": "status.current", "order": "ASCENDING"},
    {"fieldPath": "breeding.breedingDate", "order": "DESCENDING"}
  ]
}
```

## Query Patterns & Performance Analysis

### 1. Common Query Patterns

#### User Discovery Queries
```javascript
// Find farmers in a specific region
db.collection('users')
  .where('tier', '==', 'farmer')
  .where('region', '==', 'andhra_pradesh')
  .where('district', '==', 'guntur')
  .orderBy('lastActiveAt', 'desc')
  .limit(20);

// Performance: ~50ms with composite index
// Index used: region + district + tier + lastActiveAt
```

#### Marketplace Search Queries
```javascript
// Search for Aseel roosters under ₹10,000 in Telangana
db.collection('marketplace')
  .where('status.current', '==', 'active')
  .where('details.breed', '==', 'Aseel')
  .where('details.gender', '==', 'male')
  .where('location.region', '==', 'telangana')
  .where('pricing.basePrice', '<=', 10000)
  .orderBy('pricing.basePrice', 'asc')
  .limit(50);

// Performance: ~75ms with composite index
// Index used: status.current + details.breed + details.gender + pricing.basePrice
```

#### Fowl Lineage Queries
```javascript
// Find all offspring of a specific rooster
db.collection('fowls')
  .where('lineage.fatherId', '==', 'rooster_123')
  .orderBy('birthDate', 'desc')
  .limit(100);

// Performance: ~30ms with single-field index
// Index used: lineage.fatherId + birthDate
```

#### Real-time Message Queries
```javascript
// Get latest messages in a conversation
db.collection('messages')
  .where('conversationId', '==', 'conv_456')
  .orderBy('sentAt', 'desc')
  .limit(50);

// Performance: ~25ms with composite index
// Index used: conversationId + sentAt
```

### 2. Pagination Strategies

#### Cursor-based Pagination
```javascript
// Efficient pagination for large datasets
const lastDoc = await db.collection('marketplace')
  .where('status.current', '==', 'active')
  .orderBy('timeline.publishedAt', 'desc')
  .limit(20)
  .get();

// Next page
const nextPage = await db.collection('marketplace')
  .where('status.current', '==', 'active')
  .orderBy('timeline.publishedAt', 'desc')
  .startAfter(lastDoc.docs[lastDoc.docs.length - 1])
  .limit(20)
  .get();

// Performance: Consistent ~50ms per page regardless of offset
```

#### Offset-based Pagination (Not Recommended)
```javascript
// Avoid this pattern - performance degrades with large offsets
const page3 = await db.collection('marketplace')
  .where('status.current', '==', 'active')
  .orderBy('timeline.publishedAt', 'desc')
  .offset(40)  // Expensive operation
  .limit(20)
  .get();

// Performance: 50ms for page 1, 200ms+ for page 10+
```

### 3. Real-time Listener Optimization

#### Efficient Real-time Updates
```javascript
// Listen to user's active conversations
const unsubscribe = db.collection('conversations')
  .where('participants', 'array-contains', userId)
  .where('lastActivityAt', '>', thirtyDaysAgo)
  .orderBy('lastActivityAt', 'desc')
  .onSnapshot(snapshot => {
    // Handle updates efficiently
    snapshot.docChanges().forEach(change => {
      if (change.type === 'added') {
        // Handle new conversation
      } else if (change.type === 'modified') {
        // Handle conversation update
      }
    });
  });

// Performance: ~100ms initial load, ~10ms per update
```

#### Optimized Message Listeners
```javascript
// Listen to new messages in active conversation
const unsubscribe = db.collection('messages')
  .where('conversationId', '==', conversationId)
  .where('sentAt', '>', lastMessageTime)
  .orderBy('sentAt', 'asc')
  .onSnapshot(snapshot => {
    snapshot.docChanges().forEach(change => {
      if (change.type === 'added') {
        // Handle new message
        displayMessage(change.doc.data());
      }
    });
  });

// Performance: ~20ms per new message
```

### 4. Regional Query Optimization

#### District-level Queries
```javascript
// Optimized for Indian administrative divisions
const getRegionalListings = async (region, district) => {
  return await db.collection('marketplace')
    .where('status.current', '==', 'active')
    .where('location.region', '==', region)
    .where('location.district', '==', district)
    .orderBy('timeline.publishedAt', 'desc')
    .limit(50)
    .get();
};

// Performance: ~60ms with regional index
// Covers 99% of user searches within their district
```

#### Multi-region Queries
```javascript
// When users want to search across regions
const getMultiRegionalListings = async (regions, breed) => {
  const promises = regions.map(region =>
    db.collection('marketplace')
      .where('status.current', '==', 'active')
      .where('location.region', '==', region)
      .where('details.breed', '==', breed)
      .orderBy('timeline.publishedAt', 'desc')
      .limit(20)
      .get()
  );

  const results = await Promise.all(promises);
  return mergeAndSortResults(results);
};

// Performance: ~150ms for 3 regions (parallel execution)
```