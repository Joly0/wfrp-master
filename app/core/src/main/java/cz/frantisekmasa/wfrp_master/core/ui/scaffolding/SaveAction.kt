package cz.frantisekmasa.wfrp_master.core.ui.scaffolding

import androidx.compose.runtime.Composable
import cz.frantisekmasa.wfrp_master.core.R

@Composable
fun SaveAction(onClick: () -> Unit, enabled: Boolean = true) {
    TopBarAction(
        textRes = R.string.button_save,
        enabled = enabled,
        onClick = onClick
    )
}
