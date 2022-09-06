package cz.frantisekmasa.wfrp_master.common.characterEdit

import androidx.compose.foundation.clickable
import androidx.compose.material.ListItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cz.frantisekmasa.wfrp_master.common.ambitions.ChangeAmbitionsDialog
import cz.frantisekmasa.wfrp_master.common.character.CharacterScreenModel
import cz.frantisekmasa.wfrp_master.common.character.characteristics.ExperiencePointsDialog
import cz.frantisekmasa.wfrp_master.common.core.domain.character.Character
import cz.frantisekmasa.wfrp_master.common.core.ui.buttons.BackButton
import cz.frantisekmasa.wfrp_master.common.core.ui.settings.SettingsCard
import cz.frantisekmasa.wfrp_master.common.core.ui.settings.SettingsTitle
import cz.frantisekmasa.wfrp_master.common.localization.LocalStrings

@Composable
fun ExperienceSection(character: Character, screenModel: CharacterScreenModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton() },
                title = { Text(LocalStrings.current.character.titleExperience) },
            )
        }
    ) {
        SettingsCard {
            ExperiencePointsSection(character, screenModel)
            AmbitionsSection(character, screenModel)
        }
    }
}

@Composable
private fun ExperiencePointsSection(character: Character, screenModel: CharacterScreenModel) {
    var experienceDialogVisible by rememberSaveable { mutableStateOf(false) }

    if (experienceDialogVisible) {
        ExperiencePointsDialog(
            value = character.points,
            save = {
                screenModel.update { character -> character.updatePoints(it) }
            },
            onDismissRequest = { experienceDialogVisible = false }
        )
    }

    SettingsTitle(LocalStrings.current.points.experience)

    ListItem(
        text = { Text(LocalStrings.current.points.labelCurrentExperience) },
        secondaryText = { Text(character.points.experience.toString()) },
        modifier = Modifier.clickable { experienceDialogVisible = true }
    )

    ListItem(
        text = { Text(LocalStrings.current.points.labelSpentExperience) },
        secondaryText = { Text(character.points.spentExperience.toString()) },
        modifier = Modifier.clickable { experienceDialogVisible = true }
    )
}
@Composable
private fun AmbitionsSection(character: Character, screenModel: CharacterScreenModel) {
    var ambitionsDialogVisible by rememberSaveable { mutableStateOf(false) }

    if (ambitionsDialogVisible) {
        ChangeAmbitionsDialog(
            title = LocalStrings.current.ambition.titleCharacterAmbitions,
            defaults = character.ambitions,
            save = {
                screenModel.update { character -> character.updateAmbitions(it) }
            },
            onDismissRequest = { ambitionsDialogVisible = false },
        )
    }

    SettingsTitle(LocalStrings.current.ambition.titleCharacterAmbitions)

    ListItem(
        text = { Text(LocalStrings.current.ambition.labelShortTerm) },
        secondaryText = { Text(character.ambitions.shortTerm) },
        modifier = Modifier.clickable { ambitionsDialogVisible = true }
    )

    ListItem(
        text = { Text(LocalStrings.current.ambition.labelLongTerm) },
        secondaryText = { Text(character.ambitions.longTerm) },
        modifier = Modifier.clickable { ambitionsDialogVisible = true }
    )
}
