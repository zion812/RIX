package com.rio.rostry.core.database.mappers

import com.rio.rostry.core.database.entities.*
import com.rio.rostry.shared.domain.model.*

/**
 * Extension functions for mapping between database entities and domain models
 */

/**
 * User entity mappings
 */
fun UserEntity.toDomain(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    userTier = UserTier.valueOf(userTier),
    verificationStatus = VerificationStatus.valueOf(verificationStatus),
    phoneNumber = phoneNumber,
    profilePhoto = profilePhoto,
    bio = bio,
    region = regionalMetadata.region,
    district = regionalMetadata.district,
    farmName = farmName,
    specializations = specializations,
    rating = rating,
    reviewCount = reviewCount,
    fowlCount = fowlCount,
    successfulTransactions = successfulTransactions,
    language = language,
    lastActiveAt = lastActiveAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun User.toEntity(syncMetadata: SyncMetadata = SyncMetadata()): UserEntity = UserEntity(
    id = id,
    email = email,
    displayName = displayName,
    userTier = userTier.name,
    verificationStatus = verificationStatus.name,
    phoneNumber = phoneNumber,
    profilePhoto = profilePhoto,
    bio = bio,
    farmName = farmName,
    specializations = specializations,
    rating = rating,
    reviewCount = reviewCount,
    fowlCount = fowlCount,
    successfulTransactions = successfulTransactions,
    language = language,
    lastActiveAt = lastActiveAt,
    regionalMetadata = RegionalMetadata(
        region = region,
        district = district
    ),
    syncMetadata = syncMetadata.copy(
        createdAt = createdAt,
        updatedAt = updatedAt
    )
)

/**
 * Fowl entity mappings
 */
fun FowlEntity.toDomain(): Fowl = Fowl(
    id = id,
    ownerId = ownerId,
    name = name,
    breedPrimary = breedPrimary,
    breedSecondary = breedSecondary,
    gender = Gender.valueOf(gender),
    birthDate = birthDate,
    ageCategory = AgeCategory.valueOf(ageCategory),
    color = color,
    weight = weight,
    height = height,
    healthStatus = HealthStatus.valueOf(healthStatus),
    availabilityStatus = AvailabilityStatus.valueOf(availabilityStatus),
    fatherId = fatherId,
    motherId = motherId,
    generation = generation,
    primaryPhoto = primaryPhoto,
    photos = photos,
    registrationNumber = registrationNumber,
    qrCode = qrCode,
    notes = notes,
    tags = tags,
    region = regionalMetadata.region,
    district = regionalMetadata.district,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Fowl.toEntity(syncMetadata: SyncMetadata = SyncMetadata()): FowlEntity = FowlEntity(
    id = id,
    ownerId = ownerId,
    name = name,
    breedPrimary = breedPrimary,
    breedSecondary = breedSecondary,
    gender = gender.name,
    birthDate = birthDate,
    ageCategory = ageCategory.name,
    color = color,
    weight = weight,
    height = height,
    combType = "SINGLE", // Default value
    legColor = "YELLOW", // Default value
    eyeColor = "RED", // Default value
    healthStatus = healthStatus.name,
    availabilityStatus = availabilityStatus.name,
    fatherId = fatherId,
    motherId = motherId,
    generation = generation,
    primaryPhoto = primaryPhoto,
    photos = photos,
    registrationNumber = registrationNumber,
    qrCode = qrCode,
    notes = notes,
    tags = tags,
    regionalMetadata = RegionalMetadata(
        region = region,
        district = district
    ),
    syncMetadata = syncMetadata.copy(
        createdAt = createdAt,
        updatedAt = updatedAt
    )
)

/**
 * Marketplace entity mappings
 */
fun MarketplaceEntity.toDomain(): MarketplaceListing = MarketplaceListing(
    id = id,
    sellerId = sellerId,
    fowlId = fowlId,
    title = title,
    description = description,
    listingType = ListingType.valueOf(listingType),
    basePrice = basePrice,
    currentBid = currentBid,
    buyNowPrice = buyNowPrice,
    listingStatus = ListingStatus.valueOf(listingStatus),
    breed = breed,
    gender = Gender.valueOf(gender),
    age = age,
    weight = weight,
    healthStatus = HealthStatus.valueOf(healthStatus),
    primaryPhotoUrl = primaryPhotoUrl,
    photos = photos,
    deliveryAvailable = deliveryAvailable,
    auctionStartTime = auctionStartTime,
    auctionEndTime = auctionEndTime,
    views = views,
    favorites = favorites,
    region = regionalMetadata.region,
    district = regionalMetadata.district,
    publishedAt = publishedAt,
    expiresAt = expiresAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun MarketplaceListing.toEntity(syncMetadata: SyncMetadata = SyncMetadata()): MarketplaceEntity = MarketplaceEntity(
    id = id,
    sellerId = sellerId,
    fowlId = fowlId,
    title = title,
    description = description,
    listingType = listingType.name,
    basePrice = basePrice,
    currentBid = currentBid,
    buyNowPrice = buyNowPrice,
    listingStatus = listingStatus.name,
    breed = breed,
    gender = gender.name,
    age = age,
    weight = weight,
    color = "UNKNOWN", // Default value
    healthStatus = healthStatus.name,
    primaryPhotoUrl = primaryPhotoUrl,
    photos = photos,
    deliveryAvailable = deliveryAvailable,
    auctionStartTime = auctionStartTime,
    auctionEndTime = auctionEndTime,
    views = views,
    favorites = favorites,
    category = "POULTRY", // Default value
    regionalMetadata = RegionalMetadata(
        region = region,
        district = district
    ),
    syncMetadata = syncMetadata.copy(
        createdAt = createdAt,
        updatedAt = updatedAt
    ),
    publishedAt = publishedAt,
    expiresAt = expiresAt
)

/**
 * Message entity mappings
 */
fun MessageEntity.toDomain(): Message = Message(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    messageType = MessageType.valueOf(messageType),
    textContent = textContent,
    mediaUrl = mediaUrl,
    mediaCaption = mediaCaption,
    cardId = cardId,
    cardData = cardData,
    latitude = latitude,
    longitude = longitude,
    replyToMessageId = replyToMessageId,
    forwarded = forwarded,
    edited = edited,
    sentAt = sentAt,
    deliveryStatus = DeliveryStatus.valueOf(deliveryStatus),
    deliveredAt = deliveredAt,
    readAt = readAt,
    reactions = reactions
)

fun Message.toEntity(syncMetadata: SyncMetadata = SyncMetadata()): MessageEntity = MessageEntity(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    messageType = messageType.name,
    textContent = textContent,
    mediaUrl = mediaUrl,
    mediaCaption = mediaCaption,
    cardId = cardId,
    cardData = cardData,
    latitude = latitude,
    longitude = longitude,
    replyToMessageId = replyToMessageId,
    forwarded = forwarded,
    edited = edited,
    sentAt = sentAt,
    deliveryStatus = deliveryStatus.name,
    deliveredAt = deliveredAt,
    readAt = readAt,
    reactions = reactions,
    syncMetadata = syncMetadata
)

/**
 * Conversation entity mappings
 */
fun ConversationEntity.toDomain(): Conversation = Conversation(
    id = id,
    conversationType = ConversationType.valueOf(conversationType),
    title = title,
    participants = participants,
    lastMessageId = lastMessageId,
    lastActivityAt = lastActivityAt,
    messageCount = messageCount,
    unreadCount = unreadCount,
    relatedListingId = relatedListingId,
    relatedFowlId = relatedFowlId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Conversation.toEntity(syncMetadata: SyncMetadata = SyncMetadata()): ConversationEntity = ConversationEntity(
    id = id,
    conversationType = conversationType.name,
    title = title,
    participants = participants,
    lastMessageId = lastMessageId,
    lastActivityAt = lastActivityAt,
    messageCount = messageCount,
    unreadCount = unreadCount,
    relatedListingId = relatedListingId,
    relatedFowlId = relatedFowlId,
    syncMetadata = syncMetadata.copy(
        createdAt = createdAt,
        updatedAt = updatedAt
    )
)

/**
 * Transfer entity mappings
 */
fun TransferEntity.toDomain(): Transfer = Transfer(
    id = id,
    fowlId = fowlId,
    fromUserId = fromUserId,
    toUserId = toUserId,
    transferType = TransferType.valueOf(transferType),
    transferStatus = TransferStatus.valueOf(transferStatus),
    amount = amount,
    paymentStatus = PaymentStatus.valueOf(paymentStatus),
    verificationRequired = verificationRequired,
    verificationStatus = VerificationStatus.valueOf(verificationStatus),
    deliveryAddress = deliveryAddress,
    trackingNumber = trackingNumber,
    transferNotes = transferNotes,
    relatedListingId = relatedListingId,
    initiatedAt = initiatedAt,
    completedAt = completedAt,
    region = regionalMetadata.region,
    district = regionalMetadata.district
)

fun Transfer.toEntity(syncMetadata: SyncMetadata = SyncMetadata()): TransferEntity = TransferEntity(
    id = id,
    fowlId = fowlId,
    fromUserId = fromUserId,
    toUserId = toUserId,
    transferType = transferType.name,
    transferStatus = transferStatus.name,
    amount = amount,
    paymentStatus = paymentStatus.name,
    verificationRequired = verificationRequired,
    verificationStatus = verificationStatus.name,
    deliveryAddress = deliveryAddress,
    trackingNumber = trackingNumber,
    transferNotes = transferNotes,
    relatedListingId = relatedListingId,
    initiatedAt = initiatedAt,
    completedAt = completedAt,
    regionalMetadata = RegionalMetadata(
        region = region,
        district = district
    ),
    syncMetadata = syncMetadata
)

/**
 * Breeding record entity mappings
 */
fun BreedingRecordEntity.toDomain(): BreedingRecord = BreedingRecord(
    id = id,
    sireId = sireId,
    damId = damId,
    breederId = breederId,
    breedingDate = breedingDate,
    expectedHatchDate = expectedHatchDate,
    actualHatchDate = actualHatchDate,
    eggsLaid = eggsLaid,
    chicksHatched = chicksHatched,
    breedingMethod = BreedingMethod.valueOf(breedingMethod),
    breedingPurpose = BreedingPurpose.valueOf(breedingPurpose),
    offspringIds = offspringIds,
    notes = notes,
    region = regionalMetadata.region,
    district = regionalMetadata.district,
    createdAt = createdAt
)

fun BreedingRecord.toEntity(syncMetadata: SyncMetadata = SyncMetadata()): BreedingRecordEntity = BreedingRecordEntity(
    id = id,
    sireId = sireId,
    damId = damId,
    breederId = breederId,
    breedingDate = breedingDate,
    expectedHatchDate = expectedHatchDate,
    actualHatchDate = actualHatchDate,
    eggsLaid = eggsLaid,
    chicksHatched = chicksHatched,
    breedingMethod = breedingMethod.name,
    breedingPurpose = breedingPurpose.name,
    offspringIds = offspringIds,
    notes = notes,
    regionalMetadata = RegionalMetadata(
        region = region,
        district = district
    ),
    syncMetadata = syncMetadata.copy(
        createdAt = createdAt
    )
)
