package com.mattiamularoni.saveeat.common.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mattiamularoni.saveeat.R
import com.mattiamularoni.saveeat.features.pantry.data.local.PantryDao
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val pantryDao: PantryDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "pantry_notification_daily_check"
        const val CHANNEL_ID = "pantry_expiration"
        private const val NOTIFICATION_DAYS_AHEAD = 3
    }

    override suspend fun doWork(): Result {
        val notifManager = NotificationManagerCompat.from(applicationContext)

        // Se le notifiche sono disabilitate usciamo senza marcare nulla,
        // così il prodotto resta in coda per i tentativi futuri.
        if (!notifManager.areNotificationsEnabled()) {
            return Result.success()
        }

        createChannel()

        val (windowStart, windowEnd) = computeWindow()
        val items = pantryDao.getItemsDueForNotification(windowStart, windowEnd)
        val now = System.currentTimeMillis()

        items.forEach { item ->
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Prodotto in scadenza")
                .setContentText("${item.name} scade tra 3 giorni")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notifManager.notify(item.id.hashCode(), notification)
            pantryDao.markAsNotified(item.id, now)
        }

        return Result.success()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Scadenza prodotti",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    // Restituisce [mezzanotte del giorno D+3, mezzanotte del giorno D+4),
    // usando Calendar per rispettare il fuso orario locale.
    private fun computeWindow(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, NOTIFICATION_DAYS_AHEAD)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = start + TimeUnit.DAYS.toMillis(1)
        return start to end
    }
}
