# Manual Test Script: Fowl Transfer Flow

## Overview
This document provides step-by-step instructions for manually testing the fowl transfer functionality in the ROSTRY platform. This test covers the complete happy path from initiating a transfer to verifying it.

## Prerequisites
- Two user accounts (one as giver, one as receiver)
- A fowl registered to the giver's account
- Access to the ROSTRY mobile application
- Network connectivity

## Test Steps

### 1. Create Fowl (Giver's perspective)
1. Log in as the giver user
2. Navigate to "My Fowls" section
3. Tap "Add New Fowl"
4. Fill in fowl details:
   - Breed: Aseel
   - Gender: Male
   - Date of Birth: 2023-01-15
   - Parent IDs: (if available)
5. Save the fowl
6. Verify the fowl appears in "My Fowls" list

### 2. Add Records to Fowl
1. From the fowl details page, tap "Add Record"
2. Add a vaccination record:
   - Record Type: Vaccination
   - Details: Newcastle vaccine on 2023-02-15
   - Upload proof photo
3. Add a 5-week record:
   - Record Type: 5-week
   - Details: Weight 1.2kg, Color black
   - Upload proof photo
4. Verify records appear in fowl's timeline

### 3. Create Marketplace Listing
1. From the fowl details page, tap "Create Listing"
2. Fill in listing details:
   - Purpose: Breeding
   - Price: ₹1,500
   - Location: Vijayawada, Andhra Pradesh
3. Save the listing
4. Verify listing appears in Marketplace

### 4. Initiate Transfer (Giver's perspective)
1. Navigate to fowl details or listing
2. Tap "Transfer" button
3. Enter receiver's information (email/phone)
4. Confirm transfer initiation
5. Verify transfer status shows as "Pending"

### 5. Receive Transfer Request (Receiver's perspective)
1. Log in as the receiver user
2. Navigate to "Transfers" section
3. Verify pending transfer request is visible
4. Tap on the transfer to view details
5. Verify fowl details and giver information are correct

### 6. Verify Transfer (Receiver's perspective)
1. From the transfer details page, tap "Verify Transfer"
2. Fill in verification details:
   - Color: Black
   - Weight: 2.5 kg (±5% tolerance)
   - Age: 20 weeks (±2 weeks tolerance)
   - Location: Vijayawada, Andhra Pradesh
   - Agreed Price: ₹1,500
   - Upload verification photos
3. Submit verification
4. Verify transfer status changes to "Verified"

### 7. Confirm Ownership Transfer
1. Log in as the receiver user
2. Navigate to "My Fowls" section
3. Verify the transferred fowl now appears in the list
4. Verify fowl details are correct

### 8. Confirm Listing Status Update
1. Log in as either user
2. Navigate to Marketplace
3. Find the listing for the transferred fowl
4. Verify listing status shows as "Sold"

### 9. Verify Coin Transactions
1. Log in as the giver user
2. Navigate to "Coins" section
3. Verify coin balance reflects earnings from the sale
4. Check coin ledger for transaction details

## Expected Results
- Fowl ownership successfully transfers from giver to receiver
- Marketplace listing status updates to "Sold"
- Both users can see the transfer in their history
- Coin transactions are recorded correctly
- All verification details are stored securely
- No errors occur during the process

## Edge Cases to Test
1. Rejecting a transfer instead of verifying
2. Attempting to verify a transfer with data outside tolerance ranges
3. Attempting to transfer a fowl that's already in transfer
4. Network interruption during transfer process

## Troubleshooting
- If transfer doesn't appear, check network connectivity and refresh
- If verification fails, ensure all required fields are filled with valid data
- If coin transactions don't appear, check user account status and refresh

## Success Criteria
- [ ] Fowl created successfully with records
- [ ] Marketplace listing created
- [ ] Transfer initiated successfully
- [ ] Transfer verified with proper details
- [ ] Ownership transferred correctly
- [ ] Listing marked as sold
- [ ] Coins credited to giver's account
- [ ] All data persisted in database