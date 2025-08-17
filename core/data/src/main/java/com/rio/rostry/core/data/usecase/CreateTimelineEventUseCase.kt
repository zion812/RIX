package com.rio.rostry.core.data.usecase

import com.rio.rostry.core.data.repository.TimelineRepository
import com.rio.rostry.core.common.model.Result
import javax.inject.Inject

/**
 * Use case for creating timeline events
 */
class CreateTimelineEventUseCase @Inject constructor(
    private val timelineRepository: TimelineRepository
) {
    /**
     * Create a timeline event with the provided data
     *
     * @param timelineData The data for the timeline event
     * @return Result containing the ID of the created event or an error
     */
    suspend operator fun invoke(timelineData: TimelineCreationData): Result<String> {
        return timelineRepository.createTimelineEvent(
            fowlId = timelineData.fowlId,
            eventType = timelineData.eventType,
            title = timelineData.title,
            description = timelineData.description,
            mediaReferences = timelineData.mediaReferences,
            metadata = timelineData.metadata,
            latitude = timelineData.latitude,
            longitude = timelineData.longitude,
            createdBy = timelineData.createdBy
        )
    }
}

/**
 * Data class representing the creation data for a timeline event
 */
data class TimelineCreationData(
    val fowlId: String,
    val eventType: String,
    val title: String,
    val description: String? = null,
    val mediaReferences: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdBy: String
)