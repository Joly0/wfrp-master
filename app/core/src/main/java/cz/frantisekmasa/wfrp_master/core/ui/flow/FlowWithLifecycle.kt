package cz.frantisekmasa.wfrp_master.core.ui.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect

@Composable
fun <T> StateFlow<T>.collectWithLifecycle(): State<T> = collectWithLifecycle(value)

@Composable
fun <T> Flow<T>.collectWithLifecycle(initialValue: T): State<T> {
    val lifecycleOwner = LocalLifecycleOwner.current.lifecycle

    return produceState(initialValue, this, lifecycleOwner) {
        flowWithLifecycle(lifecycleOwner, Lifecycle.State.STARTED)
            .collect { value = it }
    }
}
