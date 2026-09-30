package top.stellortus.stellarmusic.ui

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.justlikecheese.nextoast.NexToast
import java.lang.ref.WeakReference

object ToastManager {
    private var currentToastRef: WeakReference<NexToast>? = null

    /** 供非 Composable 上下文（如协程）调用 */
    fun makeText(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        currentToastRef?.get()?.cancel()
        val newToast = NexToast.makeText(context.applicationContext, message, duration).apply {
            show()
        }
        currentToastRef = WeakReference(newToast)
    }

    @SuppressLint("ComposableNaming")
    @Composable
    fun makeText(message: String, duration: Int = Toast.LENGTH_SHORT) {
        makeText(LocalContext.current, message, duration)
    }
}