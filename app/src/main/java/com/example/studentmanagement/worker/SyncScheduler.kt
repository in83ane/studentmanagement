package com.example.studentmanagement.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

object SyncScheduler {
    private fun constraints() = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    fun syncNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS).build()
        WorkManager.getInstance(context).enqueueUniqueWork("student-sync-once", ExistingWorkPolicy.KEEP, request)
        Log.i("StudentSyncWorker", "ENQUEUE one-time id=${request.id} network=CONNECTED")
    }
    fun schedulePeriodicSync(context: Context) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setInitialDelay(15, TimeUnit.MINUTES).setConstraints(constraints()).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("student-sync-periodic", ExistingPeriodicWorkPolicy.KEEP, request)
        Log.i("StudentSyncWorker", "REGISTER periodic interval=15min network=CONNECTED")
    }
    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("student-sync-once")
        WorkManager.getInstance(context).cancelUniqueWork("student-sync-periodic")
    }
}
