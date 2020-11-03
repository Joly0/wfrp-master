package cz.muni.fi.rpg.ui.compendium

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.ListItem
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraintsScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cz.muni.fi.rpg.R
import cz.muni.fi.rpg.model.domain.compendium.Talent
import cz.muni.fi.rpg.ui.common.composables.*
import cz.muni.fi.rpg.ui.common.composables.dialog.DialogState
import cz.muni.fi.rpg.viewModels.CompendiumViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@ExperimentalLayout
@Composable
fun WithConstraintsScope.TalentCompendiumTab(viewModel: CompendiumViewModel) {
    val coroutineScope = rememberCoroutineScope()

    CompendiumTab(
        liveItems = viewModel.talents,
        emptyUI = {
            EmptyUI(
                textId = R.string.no_talents_in_compendium,
                subTextId = R.string.no_talents_in_compendium_sub_text,
                drawableResourceId = R.drawable.ic_skills
            )
        },
        onRemove = { coroutineScope.launch(Dispatchers.IO) { viewModel.remove(it) } },
        dialog = { TalentDialog(it, viewModel) },
        width = maxWidth,
    ) { talent ->
        ListItem(
            icon = { ItemIcon(R.drawable.ic_skills) },
            text = { Text(talent.name) }
        )
        Divider()
    }
}

private data class TalentFormData(
    val id: UUID,
    val name: MutableState<String>,
    val maxTimesTaken: MutableState<String>,
    val description: MutableState<String>,
) : CompendiumItemFormData<Talent> {
    companion object {
        @Composable
        fun fromState(state: DialogState.Opened<Talent?>) = TalentFormData(
            id = remember(state) { state.item?.id ?: UUID.randomUUID() },
            name = savedInstanceState(state) { state.item?.name ?: "" },
            maxTimesTaken = savedInstanceState(state) { state.item?.maxTimesTaken ?: "" },
            description = savedInstanceState(state) { state.item?.description ?: "" },
        )
    }

    override fun toItem() = Talent(
        id = id,
        name = name.value,
        maxTimesTaken = maxTimesTaken.value,
        description = description.value,
    )

    override fun isValid() =
        name.value.isNotBlank() &&
                name.value.length <= Talent.NAME_MAX_LENGTH &&
                maxTimesTaken.value.length <= Talent.MAX_TIMES_TAKEN_MAX_LENGTH &&
                description.value.length <= Talent.DESCRIPTION_MAX_LENGTH
}

@ExperimentalLayout
@Composable
private fun TalentDialog(
    dialogState: MutableState<DialogState<Talent?>>,
    viewModel: CompendiumViewModel
) {
    val dialogStateValue = dialogState.value

    if (dialogStateValue !is DialogState.Opened) {
        return
    }

    val formData = TalentFormData.fromState(dialogStateValue)

    CompendiumItemDialog(
        onDismissRequest = { dialogState.value = DialogState.Closed() },
        title = stringResource(
            if (dialogStateValue.item == null) R.string.title_talent_add else R.string.title_talent_edit
        ),
        formData = formData,
        saver = viewModel::save,
    ) { validate ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(BodyPadding),
        ) {
            TextInput(
                label = stringResource(R.string.label_name),
                value = formData.name.value,
                onValueChange = { formData.name.value = it },
                validate = validate,
                maxLength = Talent.NAME_MAX_LENGTH
            )

            TextInput(
                label = stringResource(R.string.label_talent_max_times_taken),
                value = formData.maxTimesTaken.value,
                onValueChange = { formData.maxTimesTaken.value = it },
                validate = validate,
                maxLength = Talent.MAX_TIMES_TAKEN_MAX_LENGTH,
            )

            TextInput(
                label = stringResource(R.string.label_description),
                value = formData.description.value,
                onValueChange = { formData.description.value = it },
                validate = validate,
                maxLength = Talent.DESCRIPTION_MAX_LENGTH,
                multiLine = true,
            )
        }
    }
}
