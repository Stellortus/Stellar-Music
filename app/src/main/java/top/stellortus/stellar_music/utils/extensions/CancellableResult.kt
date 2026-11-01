package top.stellortus.stellar_music.utils.extensions

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Cancellation exits the coroutine instead of becoming a user-visible failure. */
internal suspend inline fun <T> runCatchingCancellable(block: () -> T): Result<T> = try {
    currentCoroutineContext().ensureActive()
    val value = block()
    currentCoroutineContext().ensureActive()
    Result.success(value)
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (error: Throwable) {
    currentCoroutineContext().ensureActive()
    Result.failure(error)
}
