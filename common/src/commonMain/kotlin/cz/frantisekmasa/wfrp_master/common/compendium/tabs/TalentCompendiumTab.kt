package cz.frantisekmasa.wfrp_master.common.compendium.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import cz.frantisekmasa.wfrp_master.common.compendium.CompendiumItemDialog
import cz.frantisekmasa.wfrp_master.common.compendium.CompendiumItemFormData
import cz.frantisekmasa.wfrp_master.common.compendium.CompendiumScreenModel
import cz.frantisekmasa.wfrp_master.common.compendium.CompendiumTab
import cz.frantisekmasa.wfrp_master.common.compendium.domain.Talent
import cz.frantisekmasa.wfrp_master.common.core.shared.Resources
import cz.frantisekmasa.wfrp_master.common.core.ui.dialogs.DialogState
import cz.frantisekmasa.wfrp_master.common.core.ui.forms.InputValue
import cz.frantisekmasa.wfrp_master.common.core.ui.forms.Rules
import cz.frantisekmasa.wfrp_master.common.core.ui.forms.TextInput
import cz.frantisekmasa.wfrp_master.common.core.ui.forms.inputValue
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.EmptyUI
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.ItemIcon
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.Spacing
import cz.frantisekmasa.wfrp_master.common.localization.LocalStrings


@Composable
fun TalentCompendiumTab(screenModel: CompendiumScreenModel, width: Dp) {
    val strings = LocalStrings.current.talents.messages

    CompendiumTab(
        liveItems = screenModel.talents,
        emptyUI = {
            EmptyUI(
                text = strings.noTalentsInCompendium,
                subText = strings.noTalentsInCompendiumSubtext,
                icon = Resources.Drawable.Skill,
            )
        },
        remover = screenModel::remove,
        saver = screenModel::save,
        dialog = { TalentDialog(it, screenModel) },
        width = width,
    ) { talent ->
        ListItem(
            icon = { ItemIcon(Resources.Drawable.Skill) },
            text = { Text(talent.name) }
        )
        Divider()
    }
}

@Stable
private data class TalentFormData(
    val id: Uuid,
    val name: InputValue,
    val maxTimesTaken: InputValue,
    val description: InputValue,
) : CompendiumItemFormData<Talent> {
    companion object {
        @Composable
        fun fromTalent(talent: Talent?) = TalentFormData(
            id = remember { talent?.id ?: uuid4() },
            name = inputValue(talent?.name ?: "", Rules.NotBlank()),
            maxTimesTaken = inputValue(talent?.maxTimesTaken ?: ""),
            description = inputValue(talent?.description ?: ""),
        )
    }

    override fun toValue() = Talent(
        id = id,
        name = name.value,
        maxTimesTaken = maxTimesTaken.value,
        description = description.value,
    )

    override fun isValid() = listOf(name, maxTimesTaken, description).all { it.isValid() }
}

@Composable
private fun TalentDialog(
    dialogState: MutableState<DialogState<Talent?>>,
    screenModel: CompendiumScreenModel
) {
    val dialogStateValue = dialogState.value

    if (dialogStateValue !is DialogState.Opened) {
        return
    }

    val item = dialogStateValue.item
    val formData = TalentFormData.fromTalent(item)

    val strings = LocalStrings.current.talents

    CompendiumItemDialog(
        onDismissRequest = { dialogState.value = DialogState.Closed() },
        title = if (item == null) strings.titleNew else strings.titleEdit,
        formData = formData,
        saver = screenModel::save,
    ) { validate ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(Spacing.bodyPadding),
        ) {
            TextInput(
                label = strings.labelName,
                value = formData.name,
                validate = validate,
                maxLength = Talent.NAME_MAX_LENGTH
            )

            TextInput(
                label = strings.labelMaxTimesTaken,
                value = formData.maxTimesTaken,
                validate = validate,
                maxLength = Talent.MAX_TIMES_TAKEN_MAX_LENGTH,
            )

            TextInput(
                label = strings.labelDescription,
                value = formData.description,
                validate = validate,
                maxLength = Talent.DESCRIPTION_MAX_LENGTH,
                multiLine = true,
            )
        }
    }
}