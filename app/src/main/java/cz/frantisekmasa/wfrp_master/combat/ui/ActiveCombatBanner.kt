package cz.frantisekmasa.wfrp_master.combat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import cz.frantisekmasa.wfrp_master.common.core.domain.party.PartyId
import cz.frantisekmasa.wfrp_master.common.core.ui.flow.collectWithLifecycle
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.Spacing
import cz.frantisekmasa.wfrp_master.common.core.viewModel.PartyViewModel
import cz.frantisekmasa.wfrp_master.common.core.viewModel.viewModel
import cz.frantisekmasa.wfrp_master.common.localization.LocalStrings
import cz.frantisekmasa.wfrp_master.navigation.Route
import cz.frantisekmasa.wfrp_master.navigation.Routing
import org.koin.core.parameter.parametersOf

@Composable
fun ActiveCombatBanner(partyId: PartyId, routing: Routing<*>) {
    val partyViewModel: PartyViewModel by viewModel { parametersOf(partyId) }

    partyViewModel.party
        .collectWithLifecycle(null)
        .value
        ?.getActiveCombat() ?: return

    Surface(elevation = 8.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(Spacing.bodyPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(LocalStrings.current.combat.messages.combatInProgress)
            TextButton(onClick = { routing.navigateTo(Route.ActiveCombat(partyId)) }) {
                Text(LocalStrings.current.commonUi.buttonOpen.toUpperCase(Locale.current))
            }
        }
    }
}
