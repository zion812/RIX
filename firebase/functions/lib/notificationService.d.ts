/**
 * Comprehensive FCM-based notification service for RIO platform
 * Supports real-time messaging with offline-first architecture integration
 */
export interface NotificationPayload {
    title: string;
    body: string;
    imageUrl?: string;
    data?: {
        [key: string]: string;
    };
    category: NotificationCategory;
    priority: NotificationPriority;
    targetUsers?: string[];
    targetTopics?: string[];
    scheduledTime?: Date;
    deepLink?: string;
    actions?: NotificationAction[];
}
export interface NotificationAction {
    id: string;
    title: string;
    icon?: string;
    deepLink?: string;
}
export declare enum NotificationCategory {
    MARKETPLACE = "marketplace",
    TRANSFER = "transfer",
    COMMUNICATION = "communication",
    BREEDING = "breeding",
    PAYMENT = "payment",
    SYSTEM = "system"
}
export declare enum NotificationPriority {
    LOW = "low",
    NORMAL = "normal",
    HIGH = "high",
    URGENT = "urgent"
}
/**
 * Send notification to specific users or topics
 */
export declare const sendNotification: any;
/**
 * Process and send notification with proper targeting and personalization
 */
export declare function processNotification(payload: NotificationPayload, senderId: string): Promise<string>;
/**
 * Process scheduled notifications
 */
export declare const processScheduledNotifications: any;
//# sourceMappingURL=notificationService.d.ts.map