# ROSTRY Platform - Rules Test Plan

This document outlines the test plan for validating Firestore and Storage security rules to ensure they meet the MVP requirements.

## Firestore Rules Test Plan

### 1. Transfer Log Immutability Tests

#### Test Case 1.1: Create Transfer Log
- **Precondition**: Authenticated user
- **Action**: Create a new transfer log with status "PENDING"
- **Expected Result**: Operation succeeds

#### Test Case 1.2: Update Pending Transfer Log
- **Precondition**: Authenticated user, transfer log with status "PENDING"
- **Action**: Update transfer log details
- **Expected Result**: Operation succeeds

#### Test Case 1.3: Update Verified Transfer Log
- **Precondition**: Authenticated user, transfer log with status "VERIFIED"
- **Action**: Attempt to update transfer log details
- **Expected Result**: Operation fails with permission denied

#### Test Case 1.4: Update Rejected Transfer Log
- **Precondition**: Authenticated user, transfer log with status "REJECTED"
- **Action**: Attempt to update transfer log details
- **Expected Result**: Operation fails with permission denied

### 2. Ownership-based Write Tests

#### Test Case 2.1: Fowl Creation
- **Precondition**: Authenticated farmer or enthusiast
- **Action**: Create a new fowl with proper schema
- **Expected Result**: Operation succeeds

#### Test Case 2.2: Fowl Update by Owner
- **Precondition**: Authenticated user who owns the fowl
- **Action**: Update fowl details
- **Expected Result**: Operation succeeds

#### Test Case 2.3: Fowl Update by Non-Owner
- **Precondition**: Authenticated user who does not own the fowl
- **Action**: Attempt to update fowl details
- **Expected Result**: Operation fails with permission denied

#### Test Case 2.4: Fowl Update Changing Owner
- **Precondition**: Authenticated user who owns the fowl
- **Action**: Attempt to change ownerId field
- **Expected Result**: Operation fails with permission denied

#### Test Case 2.5: Admin Update Fowl
- **Precondition**: Authenticated admin user
- **Action**: Update any fowl details (but not ownerId)
- **Expected Result**: Operation succeeds

### 3. Schema Validation Tests

#### Test Case 3.1: Fowl Creation Missing Required Fields
- **Precondition**: Authenticated farmer or enthusiast
- **Action**: Create a fowl without required fields (name, breed, ownerId, createdAt)
- **Expected Result**: Operation fails with permission denied

#### Test Case 3.2: Fowl Creation With Required Fields
- **Precondition**: Authenticated farmer or enthusiast
- **Action**: Create a fowl with all required fields
- **Expected Result**: Operation succeeds

#### Test Case 3.3: Marketplace Listing Creation Missing Required Fields
- **Precondition**: Authenticated farmer or enthusiast
- **Action**: Create a marketplace listing without required fields (title, sellerId, priceInCoins, fowlId)
- **Expected Result**: Operation fails with permission denied

### 4. Transfer Log Update Validation

#### Test Case 4.1: Update Transfer Log Core Identifiers
- **Precondition**: Authenticated user, transfer log with status "PENDING"
- **Action**: Attempt to change core identifiers (fromUserId, toUserId, fowlId, initiatedAt)
- **Expected Result**: Operation fails with permission denied

#### Test Case 4.2: Update Transfer Log Non-Core Fields
- **Precondition**: Authenticated user, transfer log with status "PENDING"
- **Action**: Update non-core fields (verificationStatus, notes, etc.)
- **Expected Result**: Operation succeeds

## Storage Rules Test Plan

### 1. Proof Media Access Tests

#### Test Case 1.1: Owner Upload Proof Media
- **Precondition**: Authenticated user
- **Action**: Upload proof media to their fowl's proof directory
- **Expected Result**: Operation succeeds

#### Test Case 1.2: Owner Read Proof Media
- **Precondition**: Authenticated user with existing proof media
- **Action**: Read their own proof media
- **Expected Result**: Operation succeeds

#### Test Case 1.3: Non-Owner Read Proof Media
- **Precondition**: Authenticated user
- **Action**: Attempt to read another user's proof media
- **Expected Result**: Operation fails with permission denied

#### Test Case 1.4: Non-Owner Upload Proof Media
- **Precondition**: Authenticated user
- **Action**: Attempt to upload proof media to another user's directory
- **Expected Result**: Operation fails with permission denied

### 2. Thumbnail Access Tests

#### Test Case 2.1: Owner Upload Thumbnail
- **Precondition**: Authenticated user
- **Action**: Upload thumbnail to their fowl's thumbnail directory
- **Expected Result**: Operation succeeds

#### Test Case 2.2: Owner Read Thumbnail
- **Precondition**: Authenticated user with existing thumbnail
- **Action**: Read their own thumbnail
- **Expected Result**: Operation succeeds

#### Test Case 2.3: Non-Owner Read Thumbnail
- **Precondition**: Authenticated user
- **Action**: Attempt to read another user's thumbnail
- **Expected Result**: Operation fails with permission denied

### 3. Export Security Tests

#### Test Case 3.1: Owner Read Export (When Enabled)
- **Precondition**: Authenticated user with existing export, export sharing enabled
- **Action**: Read their own export
- **Expected Result**: Operation succeeds

#### Test Case 3.2: Owner Read Export (When Disabled)
- **Precondition**: Authenticated user with existing export, export sharing disabled
- **Action**: Attempt to read their own export
- **Expected Result**: Operation fails with permission denied

#### Test Case 3.3: Non-Owner Read Export
- **Precondition**: Authenticated user
- **Action**: Attempt to read another user's export
- **Expected Result**: Operation fails with permission denied

#### Test Case 3.4: Any User Write Export
- **Precondition**: Authenticated user
- **Action**: Attempt to write to export directory
- **Expected Result**: Operation fails with permission denied

## Test Execution Checklist

### Firestore Rules
- [ ] Transfer log immutability after VERIFIED status
- [ ] Transfer log immutability after REJECTED status
- [ ] Ownership-based writes for fowls
- [ ] Schema validation for fowl creation
- [ ] Schema validation for marketplace listings
- [ ] Transfer log update restrictions
- [ ] Admin override capabilities

### Storage Rules
- [ ] Proof media access control (owner read/write only)
- [ ] Thumbnail access control (owner read/write only)
- [ ] Export access control (owner read only when enabled)
- [ ] No write access to export directories
- [ ] File type and size validation

## Test Environments
1. **Development Environment**: For initial testing and debugging
2. **Staging Environment**: For comprehensive validation before production
3. **Production Environment**: For final validation (with caution)

## Test Tools
1. Firebase Emulator Suite for local testing
2. Firebase Rules Test SDK for unit testing
3. Manual testing with Firebase Console
4. Integration tests in the Android app

## Validation Criteria
- All tests must pass before merging to production
- Any failed tests must be addressed before deployment
- Test results must be documented and reviewed
- Security issues identified during testing must be fixed before release