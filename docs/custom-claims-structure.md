# Custom Claims Structure for RIO 3-Tier User Hierarchy

## Overview

This document defines the custom claims structure for Firebase Authentication in the RIO rooster community platform, implementing a 3-tier user hierarchy designed for rural farmers and urban enthusiasts in Andhra Pradesh/Telangana.

## User Tier Definitions

### Tier 1: General Users
- **Target Audience**: Casual browsers, potential buyers, newcomers to rooster community
- **Verification Level**: Basic (email/phone verification only)
- **Primary Use Cases**: Browse marketplace, view fowl listings, basic profile management

### Tier 2: Farmers
- **Target Audience**: Active rooster farmers, breeders, sellers
- **Verification Level**: Enhanced (farm documentation, identity verification)
- **Primary Use Cases**: Create/edit fowl listings, manage breeding records, sell in marketplace

### Tier 3: High-Level Enthusiasts
- **Target Audience**: Premium users, expert breeders, community leaders
- **Verification Level**: Premium (comprehensive documentation, references)
- **Primary Use Cases**: Advanced analytics, premium features, verified transfers, priority support

## Custom Claims Schema

### Core Structure

```json
{
  "tier": "general|farmer|enthusiast",
  "permissions": {
    "canCreateListings": boolean,
    "canEditListings": boolean,
    "canDeleteListings": boolean,
    "canAccessMarketplace": boolean,
    "canManageBreedingRecords": boolean,
    "canAccessAnalytics": boolean,
    "canAccessPremiumFeatures": boolean,
    "canVerifyTransfers": boolean,
    "canAccessPrioritySupport": boolean,
    "canModerateContent": boolean
  },
  "verificationStatus": {
    "level": "basic|enhanced|premium",
    "emailVerified": boolean,
    "phoneVerified": boolean,
    "identityVerified": boolean,
    "farmDocumentsVerified": boolean,
    "referencesVerified": boolean,
    "verifiedAt": "ISO8601 timestamp",
    "verifiedBy": "admin_user_id"
  },
  "profile": {
    "region": "andhra_pradesh|telangana|other",
    "district": "string",
    "language": "en|te|hi",
    "farmType": "commercial|hobby|breeding|other",
    "experienceLevel": "beginner|intermediate|expert",
    "specializations": ["breed1", "breed2", "..."]
  },
  "limits": {
    "maxListings": number,
    "maxPhotosPerListing": number,
    "maxBreedingRecords": number,
    "dailyMessageLimit": number
  },
  "metadata": {
    "createdAt": "ISO8601 timestamp",
    "lastUpdated": "ISO8601 timestamp",
    "tierUpgradeHistory": [
      {
        "fromTier": "string",
        "toTier": "string",
        "upgradedAt": "ISO8601 timestamp",
        "reason": "string"
      }
    ]
  }
}
```

## Tier-Specific Configurations

### General Users (Tier 1)

```json
{
  "tier": "general",
  "permissions": {
    "canCreateListings": false,
    "canEditListings": false,
    "canDeleteListings": false,
    "canAccessMarketplace": true,
    "canManageBreedingRecords": false,
    "canAccessAnalytics": false,
    "canAccessPremiumFeatures": false,
    "canVerifyTransfers": false,
    "canAccessPrioritySupport": false,
    "canModerateContent": false
  },
  "verificationStatus": {
    "level": "basic",
    "emailVerified": true,
    "phoneVerified": true,
    "identityVerified": false,
    "farmDocumentsVerified": false,
    "referencesVerified": false
  },
  "limits": {
    "maxListings": 0,
    "maxPhotosPerListing": 0,
    "maxBreedingRecords": 0,
    "dailyMessageLimit": 10
  }
}
```

### Farmers (Tier 2)

```json
{
  "tier": "farmer",
  "permissions": {
    "canCreateListings": true,
    "canEditListings": true,
    "canDeleteListings": true,
    "canAccessMarketplace": true,
    "canManageBreedingRecords": true,
    "canAccessAnalytics": true,
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
    "referencesVerified": false
  },
  "limits": {
    "maxListings": 50,
    "maxPhotosPerListing": 10,
    "maxBreedingRecords": 100,
    "dailyMessageLimit": 50
  }
}
```

### High-Level Enthusiasts (Tier 3)

```json
{
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
    "referencesVerified": true
  },
  "limits": {
    "maxListings": 200,
    "maxPhotosPerListing": 20,
    "maxBreedingRecords": 500,
    "dailyMessageLimit": 200
  }
}
```

## Implementation Guidelines

### Custom Claims Validation

```typescript
// TypeScript interface for type safety
interface UserClaims {
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
  verificationStatus: {
    level: 'basic' | 'enhanced' | 'premium';
    emailVerified: boolean;
    phoneVerified: boolean;
    identityVerified: boolean;
    farmDocumentsVerified: boolean;
    referencesVerified: boolean;
    verifiedAt?: string;
    verifiedBy?: string;
  };
  profile: {
    region: 'andhra_pradesh' | 'telangana' | 'other';
    district: string;
    language: 'en' | 'te' | 'hi';
    farmType: 'commercial' | 'hobby' | 'breeding' | 'other';
    experienceLevel: 'beginner' | 'intermediate' | 'expert';
    specializations: string[];
  };
  limits: {
    maxListings: number;
    maxPhotosPerListing: number;
    maxBreedingRecords: number;
    dailyMessageLimit: number;
  };
  metadata: {
    createdAt: string;
    lastUpdated: string;
    tierUpgradeHistory: Array<{
      fromTier: string;
      toTier: string;
      upgradedAt: string;
      reason: string;
    }>;
  };
}

// Validation function
function validateUserClaims(claims: any): claims is UserClaims {
  const validTiers = ['general', 'farmer', 'enthusiast'];
  const validVerificationLevels = ['basic', 'enhanced', 'premium'];
  const validRegions = ['andhra_pradesh', 'telangana', 'other'];
  const validLanguages = ['en', 'te', 'hi'];
  const validFarmTypes = ['commercial', 'hobby', 'breeding', 'other'];
  const validExperienceLevels = ['beginner', 'intermediate', 'expert'];

  return (
    claims &&
    validTiers.includes(claims.tier) &&
    claims.permissions &&
    typeof claims.permissions === 'object' &&
    claims.verificationStatus &&
    validVerificationLevels.includes(claims.verificationStatus.level) &&
    claims.profile &&
    validRegions.includes(claims.profile.region) &&
    validLanguages.includes(claims.profile.language) &&
    validFarmTypes.includes(claims.profile.farmType) &&
    validExperienceLevels.includes(claims.profile.experienceLevel) &&
    claims.limits &&
    typeof claims.limits.maxListings === 'number' &&
    claims.metadata &&
    claims.metadata.createdAt &&
    claims.metadata.lastUpdated
  );
}
```

### Helper Functions

```typescript
// Get default claims for a tier
function getDefaultClaimsForTier(tier: 'general' | 'farmer' | 'enthusiast'): UserClaims {
  const baseProfile = {
    region: 'other' as const,
    district: '',
    language: 'en' as const,
    farmType: 'hobby' as const,
    experienceLevel: 'beginner' as const,
    specializations: []
  };

  const baseMetadata = {
    createdAt: new Date().toISOString(),
    lastUpdated: new Date().toISOString(),
    tierUpgradeHistory: []
  };

  switch (tier) {
    case 'general':
      return {
        tier: 'general',
        permissions: {
          canCreateListings: false,
          canEditListings: false,
          canDeleteListings: false,
          canAccessMarketplace: true,
          canManageBreedingRecords: false,
          canAccessAnalytics: false,
          canAccessPremiumFeatures: false,
          canVerifyTransfers: false,
          canAccessPrioritySupport: false,
          canModerateContent: false
        },
        verificationStatus: {
          level: 'basic',
          emailVerified: false,
          phoneVerified: false,
          identityVerified: false,
          farmDocumentsVerified: false,
          referencesVerified: false
        },
        profile: baseProfile,
        limits: {
          maxListings: 0,
          maxPhotosPerListing: 0,
          maxBreedingRecords: 0,
          dailyMessageLimit: 10
        },
        metadata: baseMetadata
      };

    case 'farmer':
      return {
        tier: 'farmer',
        permissions: {
          canCreateListings: true,
          canEditListings: true,
          canDeleteListings: true,
          canAccessMarketplace: true,
          canManageBreedingRecords: true,
          canAccessAnalytics: true,
          canAccessPremiumFeatures: false,
          canVerifyTransfers: false,
          canAccessPrioritySupport: false,
          canModerateContent: false
        },
        verificationStatus: {
          level: 'enhanced',
          emailVerified: true,
          phoneVerified: true,
          identityVerified: false,
          farmDocumentsVerified: false,
          referencesVerified: false
        },
        profile: { ...baseProfile, farmType: 'commercial' },
        limits: {
          maxListings: 50,
          maxPhotosPerListing: 10,
          maxBreedingRecords: 100,
          dailyMessageLimit: 50
        },
        metadata: baseMetadata
      };

    case 'enthusiast':
      return {
        tier: 'enthusiast',
        permissions: {
          canCreateListings: true,
          canEditListings: true,
          canDeleteListings: true,
          canAccessMarketplace: true,
          canManageBreedingRecords: true,
          canAccessAnalytics: true,
          canAccessPremiumFeatures: true,
          canVerifyTransfers: true,
          canAccessPrioritySupport: true,
          canModerateContent: false
        },
        verificationStatus: {
          level: 'premium',
          emailVerified: true,
          phoneVerified: true,
          identityVerified: false,
          farmDocumentsVerified: false,
          referencesVerified: false
        },
        profile: { ...baseProfile, farmType: 'breeding', experienceLevel: 'expert' },
        limits: {
          maxListings: 200,
          maxPhotosPerListing: 20,
          maxBreedingRecords: 500,
          dailyMessageLimit: 200
        },
        metadata: baseMetadata
      };
  }
}

// Check if user has specific permission
function hasPermission(claims: UserClaims, permission: keyof UserClaims['permissions']): boolean {
  return claims.permissions[permission] === true;
}

// Check if user can upgrade to a tier
function canUpgradeToTier(currentClaims: UserClaims, targetTier: 'farmer' | 'enthusiast'): boolean {
  const { verificationStatus } = currentClaims;

  if (targetTier === 'farmer') {
    return verificationStatus.emailVerified && verificationStatus.phoneVerified;
  }

  if (targetTier === 'enthusiast') {
    return (
      verificationStatus.emailVerified &&
      verificationStatus.phoneVerified &&
      verificationStatus.identityVerified &&
      verificationStatus.farmDocumentsVerified
    );
  }

  return false;
}
```

## Regional Considerations

### Language Support
- **Telugu (te)**: Primary language for Andhra Pradesh and Telangana users
- **Hindi (hi)**: Secondary language for broader Indian audience
- **English (en)**: Default language for technical terms and international users

### Regional Specializations
Common rooster breeds in the region:
- Aseel
- Kadaknath
- Chittagong
- Brahma
- Country Chicken
- Giriraja

### District Mapping
**Andhra Pradesh Districts:**
- Anantapur, Chittoor, East Godavari, Guntur, Krishna, Kurnool, Nellore, Prakasam, Srikakulam, Visakhapatnam, Vizianagaram, West Godavari, YSR Kadapa

**Telangana Districts:**
- Adilabad, Bhadradri Kothagudem, Hyderabad, Jagtial, Jangaon, Jayashankar Bhupalpally, Jogulamba Gadwal, Kamareddy, Karimnagar, Khammam, Komaram Bheem Asifabad, Mahabubabad, Mahabubnagar, Mancherial, Medak, Medchal-Malkajgiri, Mulugu, Nagarkurnool, Nalgonda, Narayanpet, Nirmal, Nizamabad, Peddapalli, Rajanna Sircilla, Rangareddy, Sangareddy, Siddipet, Suryapet, Vikarabad, Wanaparthy, Warangal Rural, Warangal Urban, Yadadri Bhuvanagiri

## Security Considerations

1. **Claims Size Limit**: Firebase custom claims are limited to 1000 characters. Consider storing extended profile data in Firestore.

2. **Claim Updates**: Custom claims are cached and may take up to 1 hour to propagate. Use force refresh for immediate updates.

3. **Validation**: Always validate claims on both client and server side.

4. **Audit Trail**: Maintain detailed logs of tier upgrades and permission changes.

5. **Regional Compliance**: Ensure data handling complies with Indian data protection regulations.