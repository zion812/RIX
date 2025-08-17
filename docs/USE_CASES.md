# ROSTRY Platform - Use Cases Documentation

This document describes the use cases implemented in the ROSTRY platform for handling core functionality such as verified transfers and fowl management.

## Verified Transfer Workflow

The verified transfer workflow ensures that fowl transfers between users are properly validated by the receiving party. This workflow includes three main operations:

### 1. Initiate Transfer

Initiates a new transfer request from one user to another.

#### Parameters:
- `fowlId`: The ID of the fowl being transferred
- `fromUserId`: The ID of the user initiating the transfer
- `toUserId`: The ID of the user receiving the transfer
- `transferData`: Contains:
  - `price`: Expected price in coins
  - `color`: Expected color of the fowl
  - `ageInWeeks`: Expected age in weeks
  - `weightInGrams`: Expected weight in grams
  - `photoReference`: Reference to a photo for verification

#### Returns:
- `Result<String>`: Contains the ID of the created transfer or an error

#### Usage:
```kotlin
val transferData = TransferInitiationData(
    price = 150,
    color = "brown",
    ageInWeeks = 12,
    weightInGrams = 2100,
    photoReference = "photo_12345"
)

val result = initiateTransferUseCase(
    fowlId = "fowl_abc",
    fromUserId = "user_xyz",
    toUserId = "user_123",
    transferData = transferData
)
```

### 2. Verify Transfer

Verifies a transfer by the receiving user. This confirms that the received fowl matches the expected characteristics.

#### Parameters:
- `transferId`: The ID of the transfer to verify
- `verifierId`: The ID of the user verifying the transfer
- `verificationData`: Contains:
  - `price`: Price of the fowl
  - `color`: Color of the fowl
  - `ageInWeeks`: Age in weeks
  - `weightInGrams`: Weight in grams
  - `photoReference`: Reference to a photo for verification

#### Returns:
- `Result<Boolean>`: True if verification was successful, false otherwise

#### Usage:
```kotlin
val verificationData = TransferVerificationData(
    price = 150,
    color = "brown",
    ageInWeeks = 12,
    weightInGrams = 2100,
    photoReference = "photo_67890"
)

val result = verifyTransferUseCase(
    transferId = "transfer_def",
    verifierId = "user_123",
    verificationData = verificationData
)
```

### 3. Reject Transfer

Rejects a transfer by the receiving user. This cancels the transfer if the received fowl doesn't match expectations.

#### Parameters:
- `transferId`: The ID of the transfer to reject
- `rejectorId`: The ID of the user rejecting the transfer
- `reason`: The reason for rejecting the transfer

#### Returns:
- `Result<Boolean>`: True if rejection was successful, false otherwise

#### Usage:
```kotlin
val result = rejectTransferUseCase(
    transferId = "transfer_def",
    rejectorId = "user_123",
    reason = "Fowl doesn't match description"
)
```

## Fowl Management

### Create Fowl

Creates a new fowl in the system.

#### Parameters:
- `fowlData`: Contains:
  - `ownerId`: The ID of the fowl owner
  - `name`: Name of the fowl
  - `breed`: Breed of the fowl
  - `isForSale`: Whether the fowl is for sale
  - `priceInCoins`: Price in coins if for sale
  - `photos`: List of photo references

#### Returns:
- `Result<String>`: Contains the ID of the created fowl or an error

#### Usage:
```kotlin
val fowlData = FowlCreationData(
    ownerId = "user_xyz",
    name = "Henrietta",
    breed = "Rhode Island Red",
    isForSale = true,
    priceInCoins = 150,
    photos = listOf("photo_123", "photo_456")
)

val result = createFowlUseCase(fowlData)
```

## Synchronization

### Sync Pending Operations

Synchronizes pending operations from the outbox to the server.

#### Parameters:
- `limit`: Maximum number of operations to sync (default: 50)

#### Returns:
- `Result<SyncResult>`: Contains counts of synced and failed operations

#### Usage:
```kotlin
val result = syncPendingOperationsUseCase(limit = 25)
```

## Implementation Details

All use cases follow these patterns:

1. **Error Handling**: All use cases return a `Result` type that encapsulates either success or failure
2. **Offline Support**: Operations are stored in an outbox for later synchronization when offline
3. **Validation**: Appropriate validation is performed before executing operations
4. **Security**: Operations are validated to ensure users can only perform actions they're authorized for

## Testing

Each use case has corresponding unit tests that verify:
- Successful operation execution
- Proper error handling
- Correct interaction with repositories
- Appropriate data validation

Tests can be found in:
- [TransferRepositoryTest.kt](file://C:\Users\rowdy\AndroidStudioProjects\RIX\core\data\src\test\java\com\rio\rostry\core\data\repository\TransferRepositoryTest.kt)
- [FowlRepositoryTest.kt](file://C:\Users\rowdy\AndroidStudioProjects\RIX\core\data\src\test\java\com\rio\rostry\core\data\repository\FowlRepositoryTest.kt)
- [VerifyTransferUseCaseTest.kt](file://C:\Users\rowdy\AndroidStudioProjects\RIX\core\data\src\test\java\com\rio\rostry\core\data\usecase\VerifyTransferUseCaseTest.kt)
- [InitiateTransferUseCaseTest.kt](file://C:\Users\rowdy\AndroidStudioProjects\RIX\core\data\src\test\java\com\rio\rostry\core\data\usecase\InitiateTransferUseCaseTest.kt)