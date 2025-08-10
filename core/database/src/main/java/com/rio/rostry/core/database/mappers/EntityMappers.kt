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
    email = email ?: "",
    displayName = displayName,
    userTier = UserTier.valueOf(userTier),
    verificationStatus = VerificationStatus.valueOf(verificationStatus),
    phoneNumber = phoneNumber,
    profilePhoto = profilePhoto,
    bio = bio,
    region = region,
    district = district,
    farmName = farmName,
    specializations = specializations.joinToString(","),
    rating = rating ?: 0.0,
    reviewCount = reviewCount ?: 0,
    fowlCount = fowlCount ?: 0,
    successfulTransactions = successfulTransactions ?: 0,
    language = language ?: "en",
    lastActiveAt = lastActiveAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    email = email,
    displayName = displayName,
    userTier = userTier.name,
    verificationStatus = verificationStatus.name,
    phoneNumber = phoneNumber,
    profilePhoto = profilePhoto,
    bio = bio,
    farmName = farmName,
    specializations = specializations?.takeIf { it.isNotEmpty() }?.split(",") ?: emptyList(),
    rating = rating,
    reviewCount = reviewCount,
    fowlCount = fowlCount,
    successfulTransactions = successfulTransactions,
    language = language,
    lastActiveAt = lastActiveAt,
    region = region,
    district = district,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Fowl entity mappings
 */
fun FowlEntity.toDomain(): Fowl = Fowl(
    id = id,
    ownerId = ownerId,
    name = name ?: "Unknown",
    breedPrimary = breedPrimary,
    breedSecondary = breedSecondary,
    gender = Gender.valueOf(gender),
    birthDate = birthDate,
    ageCategory = AgeCategory.valueOf(ageCategory),
    color = color,
    weight = weight ?: 0.0,
    height = height ?: 0.0,
    description = notes, // Map notes to description
    healthStatus = HealthStatus.valueOf(healthStatus),
    availabilityStatus = AvailabilityStatus.valueOf(availabilityStatus),
    fatherId = fatherId,
    motherId = motherId,
    generation = generation ?: 1,
    primaryPhoto = primaryPhoto,
    photos = photos,
    registrationNumber = registrationNumber,
    qrCode = qrCode,
    notes = notes,
    tags = tags,
    region = region,
    district = district,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Fowl.toEntity(): FowlEntity = FowlEntity(
    id = id,
    ownerId = ownerId,
    name = name,
    breedPrimary = breedPrimary,
    breedSecondary = breedSecondary,
    gender = gender.name,
    birthDate = birthDate,
    ageCategory = ageCategory.name,
    color = color ?: "Unknown",
    weight = weight ?: 0.0,
    height = height ?: 0.0,
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
    region = region,
    district = district,
    createdAt = createdAt,
    updatedAt = updatedAt
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
    price = basePrice,
    currency = currency,
    listingType = ListingType.valueOf(listingType),
    status = ListingStatus.valueOf(listingStatus),
    category = category ?: "POULTRY",
    breed = breed,
    gender = Gender.valueOf(gender),
    age = age,
    location = "$district, $region",
    photos = photos,
    features = highlights, // Map highlights to features
    healthCertified = vaccinated,
    lineageVerified = pedigreeAvailable,
    negotiable = listingType == "NEGOTIABLE",
    deliveryAvailable = deliveryAvailable,
    deliveryRadius = deliveryRadius,
    contactPreference = ContactPreference.PHONE, // Default value
    viewCount = views ?: 0,
    favoriteCount = favorites ?: 0,
    inquiryCount = inquiries ?: 0,
    region = region,
    district = district,
    expiresAt = expiresAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun MarketplaceListing.toEntity(): MarketplaceEntity = MarketplaceEntity(
    id = id,
    sellerId = sellerId,
    fowlId = fowlId,
    title = title,
    description = description,
    listingType = listingType.name,
    basePrice = price,
    currency = currency,
    listingStatus = status.name,
    breed = breed,
    gender = gender.name,
    age = age,
    weight = 0.0, // Default value
    color = "UNKNOWN", // Default value
    healthStatus = "HEALTHY", // Default value
    vaccinated = healthCertified,
    pedigreeAvailable = lineageVerified,
    registrationPapers = false, // Default value
    highlights = features,
    photos = photos,
    deliveryAvailable = deliveryAvailable,
    deliveryRadius = deliveryRadius ?: 0,
    category = category,
    region = region,
    district = district,
    expiresAt = expiresAt,
    createdAt = createdAt,
    updatedAt = updatedAt
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
    mediaType = mimeType,
    fowlId = cardId, // Use cardId for fowlId
    listingId = cardId, // Use cardId for listingId
    isRead = readAt != null,
    isDelivered = deliveredAt != null,
    readAt = readAt,
    deliveredAt = deliveredAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Message.toEntity(): MessageEntity = MessageEntity(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    messageType = messageType.name,
    textContent = textContent,
    mediaUrl = mediaUrl,
    mimeType = mediaType,
    cardId = fowlId ?: listingId, // Use fowlId or listingId for cardId
    sentAt = createdAt,
    readAt = if (isRead) readAt else null,
    deliveredAt = if (isDelivered) deliveredAt else null,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Conversation entity mappings
 */
fun ConversationEntity.toDomain(): Conversation = Conversation(
    id = id,
    conversationType = ConversationType.valueOf(conversationType),
    title = title ?: "",
    participants = participants,
    lastMessageId = lastMessageId,
    lastMessageAt = lastActivityAt,
    unreadCount = unreadCount,
    isArchived = false, // Default value - field not in entity
    isMuted = muteNotifications,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Conversation.toEntity(): ConversationEntity = ConversationEntity(
    id = id,
    conversationType = conversationType.name,
    title = title,
    participants = participants,
    lastMessageId = lastMessageId,
    lastActivityAt = lastMessageAt ?: createdAt,
    unreadCount = unreadCount,
    // isArchived field not available in entity
    muteNotifications = isMuted,
    createdAt = createdAt,
    updatedAt = updatedAt
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
    status = TransferStatus.valueOf(transferStatus),
    price = amount,
    currency = currency,
    paymentMethod = paymentMethod,
    transferDate = initiatedAt,
    completedAt = completedAt,
    notes = transferNotes,
    documents = verificationDocuments,
    region = region,
    district = district,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Transfer.toEntity(): TransferEntity = TransferEntity(
    id = id,
    fowlId = fowlId,
    fromUserId = fromUserId,
    toUserId = toUserId,
    transferType = transferType.name,
    transferStatus = status.name,
    transferMethod = "DIRECT", // Default value
    amount = price,
    currency = currency ?: "INR",
    paymentMethod = paymentMethod,
    initiatedAt = transferDate ?: createdAt,
    completedAt = completedAt,
    transferNotes = notes,
    verificationDocuments = documents,
    deliveryAddress = "", // Default value - required field
    region = region,
    district = district,
    createdAt = createdAt,
    updatedAt = updatedAt
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
    eggCount = eggsLaid,
    hatchCount = chicksHatched,
    breedingMethod = BreedingMethod.valueOf(breedingMethod),
    breedingPurpose = BreedingPurpose.valueOf(breedingPurpose),
    offspringIds = offspringIds?.split(",") ?: emptyList(),
    notes = notes,
    region = region,
    district = district,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun BreedingRecord.toEntity(): BreedingRecordEntity = BreedingRecordEntity(
    id = id,
    sireId = sireId,
    damId = damId,
    breederId = breederId,
    breedingDate = breedingDate,
    expectedHatchDate = expectedHatchDate,
    actualHatchDate = actualHatchDate,
    eggsLaid = eggCount,
    chicksHatched = hatchCount,
    breedingMethod = breedingMethod.name,
    breedingPurpose = breedingPurpose.name,
    offspringIds = offspringIds.joinToString(","),
    notes = notes,
    region = region,
    district = district,
    createdAt = createdAt,
    updatedAt = updatedAt
)
