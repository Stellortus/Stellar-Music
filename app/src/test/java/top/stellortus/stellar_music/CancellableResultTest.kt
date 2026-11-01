package top.stellortus.stellar_music

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Test
import top.stellortus.stellar_music.utils.extensions.runCatchingCancellable

class CancellableResultTest {
    @Test fun normalResultsAndFailuresRemainAvailableToTheCaller() = runBlocking {
        assertEquals(42, runCatchingCancellable { 42 }.getOrThrow())
        val error = IOException("offline")
        assertSame(error, runCatchingCancellable<Unit> { throw error }.exceptionOrNull())
    }

    @Test fun cancellationIsRethrownWithoutCallingResultHandlersAndFinallyResetsLoading() = runBlocking {
        val cancellation = CancellationException("leaving page")
        var loading = true
        var successes = 0
        var failures = 0
        val escaped = runCatching {
            try {
                runCatchingCancellable<Unit> { throw cancellation }
                    .onSuccess { successes++ }
                    .onFailure { failures++ }
            } finally {
                loading = false
            }
        }.exceptionOrNull()
        assertSame(cancellation, escaped)
        assertFalse(loading)
        assertEquals(0, successes)
        assertEquals(0, failures)
    }

    @Test fun cancellingAWaitingPageDoesNotShowFailureAndClearsSubmitting() = runBlocking {
        val response = CompletableDeferred<Unit>()
        var submitting = true
        var failures = 0
        val task = launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                runCatchingCancellable { response.await() }.onFailure { failures++ }
            } finally {
                submitting = false
            }
        }
        task.cancelAndJoin()
        assertFalse(submitting)
        assertEquals(0, failures)
    }

    @Test fun lateResultsAfterCancellationCannotTriggerSuccessOrFailureUi() = runBlocking {
        val response = CompletableDeferred<Int>()
        val callbacks = mutableListOf<String>()
        val task = launch(start = CoroutineStart.UNDISPATCHED) {
            runCatchingCancellable { withContext(NonCancellable) { response.await() } }
                .onSuccess { callbacks += "success" }
                .onFailure { callbacks += "failure" }
        }
        task.cancel()
        response.complete(42)
        task.join()
        assertTrue(callbacks.isEmpty())
    }
}
