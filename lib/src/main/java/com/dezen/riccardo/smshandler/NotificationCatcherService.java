package com.dezen.riccardo.smshandler;

import android.app.Notification;
import android.provider.Telephony;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

//CODE-REVIEW DE ZEN
/**
 * Service meant to block incoming notifications if they pertain to this library.
 * The filtering work on messages is voluntarily repeated between this Service and SmsReceiver in
 * order to avoid forcing the Service to attach unnecessary resources to itself, thus making it less
 * likely to be shut down by the system.
 * @author Niccolò Turcato based on suggestion from Dr. Li Dao Hong.
 * Small tweaks from Riccardo De Zen to avoid redundant use of String objects.
 */
public class NotificationCatcherService extends NotificationListenerService {

    @Override
    public void onNotificationPosted (StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        String defaultSmsApplication;
        String notificationText;
        defaultSmsApplication = Telephony.Sms.getDefaultSmsPackage(getApplicationContext());
        if(notification.tickerText != null){
            notificationText = notification.tickerText.toString();
            //Expected structure of SMS Notification:
            // "ContactName: <#>text"
            if(sbn.getPackageName().equals(defaultSmsApplication) && !notificationText.equals("") && notificationText.contains(SmsHandler.APP_KEY))
                cancelNotification(sbn.getKey()); //blocks notifications
        }
    }
}
