package com.englishdoctor.vocabsolution.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.englishdoctor.vocabsolution.MainActivity
import com.englishdoctor.vocabsolution.R
import com.englishdoctor.vocabsolution.VocabApp
import com.englishdoctor.vocabsolution.widget.WordWidget
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class ReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as VocabApp
        val store = app.container.progress
        if (!store.loaded) store.load()
        WordWidget.refresh(applicationContext) // new Word of the Day
        val p = store.state.value
        val today = store.today()
        if (!p.settings.reminderEnabled || p.todayDone(today) >= p.dailyGoal) return Result.success()
        val due = p.dueKeys(today).size
        val text = if (due > 0) "আজ ${due.bn()}টি শব্দ রিভিউয়ের অপেক্ষায়। ৫ মিনিটেই শেষ করুন!"
        else "আজকের ${p.dailyGoal.bn()}টি নতুন শব্দ শিখে Streak ধরে রাখুন 🔥"
        notify(applicationContext, "Vocabsolution", text)
        return Result.success()
    }

    companion object {
        private const val CHANNEL = "daily_reminder"
        private const val WORK = "daily_reminder_work"

        fun schedule(context: Context, hour: Int) {
            val now = LocalDateTime.now()
            var next = now.withHour(hour).withMinute(0).withSecond(0)
            if (!next.isAfter(now)) next = next.plusDays(1)
            val delay = Duration.between(now, next).toMinutes()
            val req = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.UPDATE, req)
        }

        fun cancel(context: Context) = WorkManager.getInstance(context).cancelUniqueWork(WORK)

        fun notify(context: Context, title: String, text: String) {
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, context.getString(R.string.reminder_channel), NotificationManager.IMPORTANCE_DEFAULT)
            )
            val intent = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
            )
            val n = NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(intent)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(1, n)
        }
    }
}

private val BN = "০১২৩৪৫৬৭৮৯"
fun Int.bn(): String = toString().map { c -> if (c.isDigit()) BN[c - '0'] else c }.joinToString("")
fun Long.bn(): String = toString().map { c -> if (c.isDigit()) BN[c - '0'] else c }.joinToString("")
