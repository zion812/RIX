# RIO Platform API Documentation

## Overview
Complete API documentation for the RIO platform's Firebase Functions backend and Android client integration. All endpoints are optimized for rural connectivity with offline-first design.

## Base Configuration

### **Firebase Functions Endpoint**
```
Production: https://asia-south1-rio-platform-prod.cloudfunctions.net
Staging: https://asia-south1-rio-platform-staging.cloudfunctions.net
Development: http://localhost:5001/rio-platform-dev/asia-south1
```

### **Authentication**
All API calls require Firebase Authentication token in the Authorization header:
```
Authorization: Bearer <firebase_id_token>
```

### **Rate Limiting**
- **General Users**: 100 requests/minute
- **Farmer Tier**: 500 requests/minute
- **Enthusiast Tier**: 1000 requests/minute

## User Management APIs

### **POST /api/users/register**
Register a new user with tier assignment.

**Request Body:**
```json
{
  "email": "farmer@example.com",
  "phoneNumber": "+919876543210",
  "displayName": "Ravi Kumar",
  "location": {
    "district": "Krishna",
    "state": "Andhra Pradesh",
    "pincode": "521001"
  },
  "preferredLanguage": "te",
  "userTier": "general",
  "farmingExperience": "5_years",
  "primaryInterest": "breeding"
}
```

**Response:**
```json
{
  "success": true,
  "userId": "user_12345",
  "userTier": "general",
  "customClaims": {
    "tier": "general",
    "verified": false,
    "registrationDate": "2024-01-15T10:30:00Z"
  },
  "coinBalance": 0,
  "message": "User registered successfully"
}
```

### **PUT /api/users/upgrade-tier**
Upgrade user tier with payment verification.

**Request Body:**
```json
{
  "targetTier": "farmer",
  "paymentId": "pay_12345",
  "paymentMethod": "upi",
  "amount": 500
}
```

**Response:**
```json
{
  "success": true,
  "newTier": "farmer",
  "features": [
    "unlimited_fowl_registration",
    "marketplace_listing",
    "family_tree_visualization",
    "priority_support"
  ],
  "expiryDate": "2025-01-15T10:30:00Z"
}
```

### **GET /api/users/profile**
Get current user profile and tier information.

**Response:**
```json
{
  "userId": "user_12345",
  "email": "farmer@example.com",
  "displayName": "Ravi Kumar",
  "userTier": "farmer",
  "verified": true,
  "location": {
    "district": "Krishna",
    "state": "Andhra Pradesh"
  },
  "coinBalance": 150,
  "tierExpiry": "2025-01-15T10:30:00Z",
  "statistics": {
    "fowlsRegistered": 25,
    "marketplaceListings": 8,
    "successfulSales": 3,
    "totalEarnings": 15000
  }
}
```

## Fowl Management APIs

### **POST /api/fowl/register**
Register a new fowl with detailed information.

**Request Body:**
```json
{
  "name": "Champion Rooster",
  "breed": "Aseel",
  "type": "rooster",
  "dateOfBirth": "2023-06-15",
  "color": "red_brown",
  "weight": 3.5,
  "height": 45,
  "parentage": {
    "fatherId": "fowl_123",
    "motherId": "fowl_124"
  },
  "healthRecords": [
    {
      "date": "2023-07-01",
      "type": "vaccination",
      "description": "Newcastle disease vaccine",
      "veterinarian": "Dr. Suresh"
    }
  ],
  "photos": [
    "https://storage.googleapis.com/rio-platform/fowl/photo1.jpg"
  ],
  "location": {
    "district": "Krishna",
    "village": "Machilipatnam"
  }
}
```

**Response:**
```json
{
  "success": true,
  "fowlId": "fowl_789",
  "registrationNumber": "RIO-KR-2024-001",
  "qrCode": "https://storage.googleapis.com/rio-platform/qr/fowl_789.png",
  "familyTreeId": "tree_456",
  "message": "Fowl registered successfully"
}
```

### **GET /api/fowl/{fowlId}**
Get detailed information about a specific fowl.

**Response:**
```json
{
  "fowlId": "fowl_789",
  "registrationNumber": "RIO-KR-2024-001",
  "name": "Champion Rooster",
  "breed": "Aseel",
  "type": "rooster",
  "owner": {
    "userId": "user_12345",
    "displayName": "Ravi Kumar",
    "verified": true
  },
  "dateOfBirth": "2023-06-15",
  "age": "6 months",
  "physicalAttributes": {
    "color": "red_brown",
    "weight": 3.5,
    "height": 45
  },
  "parentage": {
    "father": {
      "fowlId": "fowl_123",
      "name": "Strong Father",
      "breed": "Aseel"
    },
    "mother": {
      "fowlId": "fowl_124",
      "name": "Good Mother",
      "breed": "Aseel"
    }
  },
  "offspring": [
    {
      "fowlId": "fowl_790",
      "name": "Young Chick",
      "dateOfBirth": "2024-01-10"
    }
  ],
  "healthRecords": [],
  "photos": [],
  "marketplaceStatus": "available",
  "verificationStatus": "verified"
}
```