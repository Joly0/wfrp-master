package cz.muni.fi.rpg.ui.shell

import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticAmbientOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

@Composable
fun rememberSystemUiController(window: Window): SystemUiController {
    return remember { SystemUiController(window) }
}

/**
 * Use [ProvideActivity] to pass this
 */
val AmbientSystemUiController = staticAmbientOf<SystemUiController> {
    error("System UI Controller was not initialized. Did you forget to call ProvideActivity?")
}

class SystemUiController(private val window: Window) {
    fun setStatusBarColor(color: Color) {
        window.statusBarColor = color.toArgb()
    }

    fun setNavigationBarColor(color: Color) {
        window.navigationBarColor = color.toArgb()
    }
}