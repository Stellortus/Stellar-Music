package top.stellortus.stellarmusic.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.justlikecheese.nextoast.NexToast
import java.lang.ref.WeakReference

object ToastManager {
    private var currentToastRef: WeakReference<NexToast>? = null

    @Composable
    fun MakeText(message: String, duration: Int = Toast.LENGTH_SHORT) {
        val context = LocalContext.current
        currentToastRef?.get()?.cancel()
        val newToast = NexToast.makeText(context.applicationContext, message, duration).apply {
            show()
        }
        currentToastRef = WeakReference(newToast)
    }
}