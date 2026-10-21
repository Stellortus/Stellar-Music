package top.stellortus.stellar_music.ui

import android.content.Context
import android.widget.Toast
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

}
