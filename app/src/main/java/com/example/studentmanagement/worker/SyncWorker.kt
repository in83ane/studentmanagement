package com.example.studentmanagement.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.studentmanagement.domain.usecase.SyncStudentsUseCase
import com.example.studentmanagement.data.remote.TokenStorage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncStudents: SyncStudentsUseCase,
    private val storage: TokenStorage
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Log.i("StudentSyncWorker", "START id=$id attempt=$runAttemptCount")
        if (storage.getToken().isNullOrBlank()) {
            Log.i("StudentSyncWorker", "FAILURE: login required")
            return Result.failure()
        }
        return try {
            val result = syncStudents()
            when {
                result.retryable > 0 -> { Log.i("StudentSyncWorker", "RETRY synced=${result.synced} failed=${result.failed}"); Result.retry() }
                result.failed > 0 -> { Log.i("StudentSyncWorker", "FAILURE non-retryable=${result.failed}"); Result.failure() }
                else -> { Log.i("StudentSyncWorker", "SUCCESS synced=${result.synced}"); Result.success() }
            }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            Log.i("StudentSyncWorker", "FAILURE type=${e.javaClass.simpleName}")
            Result.failure()
        }
    }
}
