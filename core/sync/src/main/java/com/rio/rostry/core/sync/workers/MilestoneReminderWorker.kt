package com.rio.rostry.core.sync.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.rio.rostry.core.data.repository.FowlRepository
import com.rio.rostry.core.notifications.NotificationService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Worker that checks for milestone events and sends reminders
 * Handles 5-week, 20-week, and weekly updates after 20 weeks
 */
class MilestoneReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val fowlRepository: FowlRepository,
    private val notificationService: NotificationService
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_LAST_WEEKLY_NOTIFICATION = "last_weekly_notification"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // Get all fowls that need attention
            val fowlsResult = fowlRepository.getFowlsNeedingAttention()
            
            if (fowlsResult is com.rio.rostry.core.common.model.Result.Success) {
                val fowls = fowlsResult.data
                
                for (fowl in fowls) {
                    checkMilestones(fowl)
                }
                
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private suspend fun checkMilestones(fowl: com.rio.rostry.core.database.entities.FowlEntity) {
        // Calculate fowl age in weeks
        val dob = fowl.dob.time
        val now = System.currentTimeMillis()
        val ageInWeeks = ((now - dob) / (7 * 24 * 60 * 60 * 1000L)).toInt()
        
        // Check for milestone events
        when (ageInWeeks) {
            5 -> {
                // 5-week milestone
                notificationService.sendMilestoneNotification(
                    fowlId = fowl.id,
                    milestoneType = "5_WEEK",
                    title = "5-Week Checkup Due",
                    message = "Time for the 5-week checkup for ${fowl.name ?: "your fowl"}"
                )
            }
            20 -> {
                // 20-week milestone
                notificationService.sendMilestoneNotification(
                    fowlId = fowl.id,
                    milestoneType = "20_WEEK",
                    title = "20-Week Checkup Due",
                    message = "Time for the 20-week checkup for ${fowl.name ?: "your fowl"}"
                )
            }
            else -> {
                // Weekly updates after 20 weeks
                if (ageInWeeks > 20) {
                    // Check if we've sent a notification recently
                    val lastNotificationTime = inputData.getLong(KEY_LAST_WEEKLY_NOTIFICATION, 0)
                    val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
                    
                    if (lastNotificationTime < oneWeekAgo) {
                        notificationService.sendMilestoneNotification(
                            fowlId = fowl.id,
                            milestoneType = "WEEKLY_UPDATE",
                            title = "Weekly Update Due",
                            message = "Time for the weekly update for ${fowl.name ?: "your fowl"}"
                        )
                        
                        // Update last notification time would happen in a real implementation
                    }
                }
            }
        }
    }
}