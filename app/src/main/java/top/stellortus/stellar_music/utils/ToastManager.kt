package top.stellortus.stellar_music.utils

import android.content.Context
import android.widget.Toast
import io.github.justlikecheese.nextoast.NexToast
import java.lang.ref.WeakReference

object ToastManager {
    @Volatile
    private var appContext: Context? = null

    private var currentToastRef: WeakReference<NexToast>? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun makeText(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        currentToastRef?.get()?.cancel()
        val newToast = NexToast.makeText(context.applicationContext, message, duration).apply {
            show()
        }
        currentToastRef = WeakReference(newToast)
    }

    fun makeText(message: String, duration: Int = Toast.LENGTH_SHORT) =
        makeText(
            checkNotNull(appContext) {
                "ToastManager is not initialized. Call init(context) before showing a toast."
            },
            message,
            duration,
        )

}
