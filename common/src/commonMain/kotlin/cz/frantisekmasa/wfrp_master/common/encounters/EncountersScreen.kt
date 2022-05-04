package cz.frantisekmasa.wfrp_master.common.encounters

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cz.frantisekmasa.wfrp_master.common.core.domain.party.PartyId
import cz.frantisekmasa.wfrp_master.common.core.shared.Resources
import cz.frantisekmasa.wfrp_master.common.core.shared.drawableResource
import cz.frantisekmasa.wfrp_master.common.core.ui.flow.collectWithLifecycle
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.DraggableListFor
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.EmptyUI
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.Spacing
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.VisualOnlyIconDescription
import cz.frantisekmasa.wfrp_master.common.encounters.domain.Encounter
import cz.frantisekmasa.wfrp_master.common.localization.LocalStrings


@Composable
fun EncountersScreen(
    partyId: PartyId,
    screenModel: EncountersScreenModel,
    modifier: Modifier,
    onEncounterClick: (Encounter) -> Unit,
) {
    val encounters = screenModel.encounters.collectWithLifecycle(null).value

    if (encounters == null) {
        Box(modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
        return
    }

    var createDialogOpened by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            AddEncounterButton(
                onCreateEncounterRequest = { createDialogOpened = true },
            )
        }
    ) {

        if (createDialogOpened) {
            EncounterDialog(
                existingEncounter = null,
                screenModel = screenModel,
                onDismissRequest = { createDialogOpened = false }
            )
        }

        EncounterList(encounters, screenModel, onEncounterClick)
    }
}

@Composable
private fun AddEncounterButton(onCreateEncounterRequest: () -> Unit) {
    FloatingActionButton(onClick = onCreateEncounterRequest) {
        Icon(Icons.Rounded.Add, LocalStrings.current.encounters.buttonAdd)
    }
}

@Composable
private fun EncounterList(
    encounters: List<Encounter>,
    screenModel: EncountersScreenModel,
    onClick: (Encounter) -> Unit
) {
    if (encounters.isEmpty()) {
        val strings = LocalStrings.current.encounters
        EmptyUI(
            text = strings.messages.noEncounters,
            subText = strings.messages.noEncountersSubtext,
            icon = Resources.Drawable.Encounter,
        )

        return
    }

    val icon = drawableResource(Resources.Drawable.Encounter)
    val iconSize = 28.dp

    Column(
        Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.bodyPaddingWithFab),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DraggableListFor(
            encounters,
            modifier = Modifier.fillMaxHeight(),
            itemSpacing = Spacing.small,
            onReorder = {
                screenModel.reorderEncounters(
                    it.mapIndexed { index, encounter -> encounter.id to index }.toMap()
                )
            },
        ) { _, encounter, isDragged ->
            Card(
                elevation = if (isDragged) 6.dp else 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(encounter) }
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        icon,
                        VisualOnlyIconDescription,
                        Modifier.size(iconSize),
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
                    )
                    Text(
                        encounter.name,
                        modifier = Modifier.padding(start = 8.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
    }
}