package cz.muni.fi.rpg.ui.partyList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import cz.frantisekmasa.wfrp_master.common.core.domain.party.Party
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.Spacing
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.longToast
import cz.muni.fi.rpg.R
import cz.muni.fi.rpg.viewModels.PartyListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RemovePartyDialog(party: Party, viewModel: PartyListViewModel, onDismissRequest: () -> Unit) {
    var removing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                Text(stringResource(R.string.party_remove_confirmation))

                if (party.getPlayerCounts() > 0) {
                    Text(
                        stringResource(R.string.party_remove_multiple_members),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !removing,
                onClick = onDismissRequest
            ) {
                Text(stringResource(R.string.button_cancel).toUpperCase(Locale.current))
            }
        },
        confirmButton = {
            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current

            TextButton(
                enabled = !removing,
                onClick = {
                    removing = true
                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.archive(party.id)

                        withContext(Dispatchers.Main) {
                            onDismissRequest()
                            longToast(context, R.string.message_party_removed)
                        }
                    }
                },
            ) {
                Text(stringResource(R.string.button_remove).toUpperCase(Locale.current))
            }
        }
    )
}
