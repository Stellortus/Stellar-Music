package top.stellortus.stellarmusic.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun <T> rememberPreference(
    preferences: ArgsPreferences,
    key: Key,
    vararg inputs: Any?,
    init: () -> MutableState<T>,
): MutableState<T> {
    return remember(*inputs) {
        val initialState = init()
        val initialValue = preferences.read(key)
            ?.toValueOrNull(initialState.value)
            ?: initialState.value

        PreferenceBackedMutableState(initialValue) { newValue ->
            preferences.store(key, newValue as Any)
        }
    }
}

private class PreferenceBackedMutableState<T>(
    initialValue: T,
    private val onValueChanged: (T) -> Unit,
) : MutableState<T> {
    private val backingState = mutableStateOf(initialValue)

    override var value: T
        get() = backingState.value
        set(newValue) {
            backingState.value = newValue
            onValueChanged(newValue)
        }

    override fun component1(): T = value

    override fun component2(): (T) -> Unit = { value = it }
}

@Suppress("UNCHECKED_CAST")
private fun <T> String.toValueOrNull(defaultValue: T): T? {
    return runCatching {
        when (defaultValue) {
            is String -> this
            is Int -> toInt()
            is Long -> toLong()
            is Float -> toFloat()
            is Double -> toDouble()
            is Boolean -> toBooleanStrictOrNull()
            else -> null
        } as T?
    }.getOrNull()
}