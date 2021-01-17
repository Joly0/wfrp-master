package cz.muni.fi.rpg.ui.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.collectAsState
import cz.frantisekmasa.wfrp_master.core.auth.AmbientUser
import cz.muni.fi.rpg.ui.startup.StartupScreen
import cz.muni.fi.rpg.viewModels.provideAuthenticationViewModel

@Composable
fun ProvideUser(content: @Composable () -> Unit) {
    val auth = provideAuthenticationViewModel()
    val user = auth.user.collectAsState().value

    if (user == null) {
        StartupScreen(auth)
        return
    }

    Providers(AmbientUser provides user, content = content)
}