package com.mattiamularoni.saveeat.features.notifications.data.worker

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mattiamularoni.saveeat.R
import com.mattiamularoni.saveeat.features.notifications.domain.usecase.GetItemsDueForNotificationUseCase
import com.mattiamularoni.saveeat.features.notifications.domain.usecase.MarkItemsNotifiedUseCase
import java.util.Calendar

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val getItemsDueForNotification: GetItemsDueForNotificationUseCase,
    private val markItemsNotified: MarkItemsNotifiedUseCase,
) : CoroutineWorker(context, workerParams) {
    companion object {
        const val WORK_NAME = "pantry_notification_daily_check"
        const val CHANNEL_ID = "pantry_expiration"
        private const val NOTIFICATION_DAYS_AHEAD = 3
    }

    // areNotificationsEnabled() verifica il permesso POST_NOTIFICATIONS su API 33+;
    // @SuppressLint è necessario perché lint non riconosce questo check statico.
    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val notifManager = NotificationManagerCompat.from(applicationContext)

        // Se le notifiche sono disabilitate usciamo senza marcare nulla,
        // così il prodotto resta in coda per i tentativi futuri.
        if (!notifManager.areNotificationsEnabled()) {
            return Result.success()
        }

        createChannel()

        val windowEnd = computeWindowEnd()
        val items = getItemsDueForNotification(windowEnd)

        items.forEach { item ->
            val notification =
                NotificationCompat
                    .Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Prodotto in scadenza")
                    .setContentText("${item.name} scade tra 3 giorni")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()

            notifManager.notify(Math.abs(item.id.hashCode()), notification)
        }

        if (items.isNotEmpty()) {
            markItemsNotified(items.map { it.id })
        }

        return Result.success()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Scadenza prodotti",
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    // Restituisce mezzanotte di D+4, cioè la fine del giorno D+3.
    // Qualsiasi giorno il worker gira, recupera tutti i prodotti non ancora
    // notificati che scadono entro 3 giorni (inclusi i giorni già trascorsi
    // se WorkManager ha saltato un'esecuzione).
    private fun computeWindowEnd(): Long =
        Calendar
            .getInstance()
            .apply {
                add(Calendar.DAY_OF_YEAR, NOTIFICATION_DAYS_AHEAD + 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
}
