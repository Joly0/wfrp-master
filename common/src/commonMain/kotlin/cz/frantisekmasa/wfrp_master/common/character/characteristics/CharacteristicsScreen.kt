package cz.frantisekmasa.wfrp_master.common.character.characteristics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Group
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.frantisekmasa.wfrp_master.common.ambitions.AmbitionsCard
import cz.frantisekmasa.wfrp_master.common.core.domain.Characteristic
import cz.frantisekmasa.wfrp_master.common.core.domain.Expression
import cz.frantisekmasa.wfrp_master.common.core.domain.Stats
import cz.frantisekmasa.wfrp_master.common.core.domain.character.Character
import cz.frantisekmasa.wfrp_master.common.core.domain.character.Points
import cz.frantisekmasa.wfrp_master.common.core.domain.character.Points.PointPool
import cz.frantisekmasa.wfrp_master.common.core.domain.identifiers.CharacterId
import cz.frantisekmasa.wfrp_master.common.core.domain.localizedName
import cz.frantisekmasa.wfrp_master.common.core.domain.party.Party
import cz.frantisekmasa.wfrp_master.common.core.shared.IO
import cz.frantisekmasa.wfrp_master.common.core.shared.Resources
import cz.frantisekmasa.wfrp_master.common.core.shared.drawableResource
import cz.frantisekmasa.wfrp_master.common.core.shared.rememberSoundPlayer
import cz.frantisekmasa.wfrp_master.common.core.ui.CharacterAvatar
import cz.frantisekmasa.wfrp_master.common.core.ui.dialogs.Dialog
import cz.frantisekmasa.wfrp_master.common.core.ui.dialogs.FullScreenDialog
import cz.frantisekmasa.wfrp_master.common.core.ui.forms.NumberPicker
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.FloatingActionsMenu
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.ItemIcon
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.MenuState
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.Spacing
import cz.frantisekmasa.wfrp_master.common.core.ui.responsive.Breakpoint
import cz.frantisekmasa.wfrp_master.common.core.ui.responsive.ColumnSize.FullWidth
import cz.frantisekmasa.wfrp_master.common.core.ui.responsive.ColumnSize.HalfWidth
import cz.frantisekmasa.wfrp_master.common.core.ui.responsive.Container
import cz.frantisekmasa.wfrp_master.common.core.ui.responsive.LocalBreakpoint
import cz.frantisekmasa.wfrp_master.common.core.ui.scaffolding.TopPanel
import cz.frantisekmasa.wfrp_master.common.localization.LocalStrings
import cz.frantisekmasa.wfrp_master.common.skillTest.Roll
import cz.frantisekmasa.wfrp_master.common.skillTest.RollResult
import cz.frantisekmasa.wfrp_master.common.skillTest.TestResultScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
internal fun CharacteristicsScreen(
    screenModel: CharacteristicsScreenModel,
    characterId: CharacterId,
    character: Character,
    party: Party,
    modifier: Modifier = Modifier,
) {
    var roll: Roll? by rememberSaveable { mutableStateOf(null) }

    roll?.let { currentRoll ->
        FullScreenDialog(onDismissRequest = { roll = null }) {
            val rollSound = rememberSoundPlayer(Resources.Sound.DiceRoll)

            LaunchedEffect(Unit) {
                rollSound.play()
            }

            val coroutineScope = rememberCoroutineScope()

            TestResultScreen(
                testName = LocalStrings.current.tests.roll,
                results = listOf(
                    RollResult(
                        characterId.toString(),
                        character.name,
                        currentRoll,
                    )
                ),
                onRerollRequest = {
                    roll = currentRoll.reroll()

                    coroutineScope.launch(Dispatchers.IO) { rollSound.play() }
                },
                onDismissRequest = { roll = null }
            )
        }
    }

    Scaffold(
        modifier = modifier.background(MaterialTheme.colors.background),
        floatingActionButton = {
            var menuState by remember { mutableStateOf(MenuState.COLLAPSED) }

            FloatingActionsMenu(
                state = menuState,
                onToggleRequest = { menuState = it },
                icon = drawableResource(Resources.Drawable.DiceRoll)
            ) {
                for (dice in listOf("1d100", "1d10")) {
                    ExtendedFloatingActionButton(
                        text = { Text(dice) },
                        onClick = {
                            menuState = MenuState.COLLAPSED

                            val expression = Expression.fromString(dice)
                            roll = Roll.Generic(expression, expression.evaluate())
                        }
                    )
                }
            }
        }
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = Spacing.bottomPaddingUnderFab),
        ) {
            val points = character.points

            val coroutineScope = rememberCoroutineScope()

            CharacterTopPanel(
                character,
                points,
                onUpdate = { coroutineScope.launch(Dispatchers.IO) { screenModel.updatePoints(it) } }
            )

            CharacteristicsCard(character.characteristics)

            CardRow(Modifier.padding(top = Spacing.small)) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                    CareerSection(character)
                }

                Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                    ExperiencePointsSection(
                        points = points,
                        save = screenModel::updatePoints,
                    )
                }
            }

            val strings = LocalStrings.current.ambition

            Container(
                Modifier.padding(top = Spacing.tiny),
                horizontalArrangement = Arrangement.spacedBy(Spacing.gutterSize()),
            ) {
                val size = if (breakpoint > Breakpoint.XSmall) HalfWidth else FullWidth

                column(size) {
                    AmbitionsCard(
                        title = strings.titleCharacterAmbitions,
                        ambitions = character.ambitions,
                        onSave = { screenModel.updateCharacterAmbitions(it) },
                    )
                }

                column(size) {
                    AmbitionsCard(
                        title = strings.titlePartyAmbitions,
                        ambitions = party.ambitions,
                        titleIcon = Icons.Rounded.Group,
                        onSave = null,
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterTopPanel(character: Character, points: Points, onUpdate: (Points) -> Unit) {
    TopPanel {
        Container(
            Modifier.padding(vertical = Spacing.large, horizontal = Spacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(Spacing.large),
        ) {
            val size = if (breakpoint >= Breakpoint.Small) HalfWidth else FullWidth

            column(size) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row {
                        CharacterAvatar(character.avatarUrl, ItemIcon.Size.Large)
                        Column(Modifier.padding(start = Spacing.medium)) {
                            Text(character.name, fontWeight = FontWeight.Bold)
                            Text(
                                character.race.localizedName +
                                    " " +
                                    character.career,
                                style = MaterialTheme.typography.caption
                            )
                        }
                    }

                    WoundsBadge(character, points, update = onUpdate)
                }
            }

            column(size) { PointsRow(points, update = onUpdate) }
        }
    }
}

@Composable
private fun WoundsBadge(character: Character, points: Points, update: (Points) -> Unit) {
    var dialogVisible by remember { mutableStateOf(false) }
    val strings = LocalStrings.current.points
    val wounds = character.wounds

    Button(onClick = { dialogVisible = true }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${wounds.current} / ${wounds.max}")
            Text(
                strings.wounds,
                fontWeight = FontWeight.Normal
            )
        }
    }

    if (!dialogVisible) {
        return
    }

    PointsDialog(onDismissRequest = { dialogVisible = false }) {
        NumberPicker(
            label = strings.wounds,
            value = wounds.current,
            onIncrement = {
                if (wounds.current < character.wounds.max) {
                    update(points.copy(wounds = wounds.current + 1))
                }
            },
            onDecrement = {
                if (wounds.current > 0) {
                    update(points.copy(wounds = wounds.current - 1))
                }
            }
        )
    }
}

@Composable
private fun PointsDialog(onDismissRequest: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.extraLarge),
                contentAlignment = Alignment.Center,
                content = content,
            )
        }
    }
}

@Composable
private fun PointsRow(points: Points, update: (Points) -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        val pools =
            listOf(PointPool.FATE, PointPool.FORTUNE, PointPool.RESOLVE, PointPool.RESILIENCE)

        val modifier = Modifier.weight(1f)

        pools.forEach { pool ->
            var dialogVisible by remember { mutableStateOf(false) }
            val value = points.get(pool)

            Column(
                modifier.clickable { dialogVisible = true },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    value.toString(),
                    style = MaterialTheme.typography.h6
                )
                Text(
                    pool.localizedName,
                    style = MaterialTheme.typography.caption,
                )
            }

            if (dialogVisible) {
                PointsDialog(onDismissRequest = { dialogVisible = false }) {
                    NumberPicker(
                        label = pool.localizedName,
                        value = value,
                        onIncrement = { points.modify(pool, +1).onSuccess(update) },
                        onDecrement = { points.modify(pool, -1).onSuccess(update) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CardRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Surface(modifier = modifier, elevation = 2.dp) {
        Row(
            Modifier.padding(horizontal = Spacing.large, vertical = Spacing.large),
            content = content,
        )
    }
}

@Composable
private fun CharacteristicsCard(values: Stats) {
    CardRow {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            val breakpoint = LocalBreakpoint.current
            val characteristics = remember(breakpoint) {
                listOf(
                    Characteristic.WEAPON_SKILL,
                    Characteristic.BALLISTIC_SKILL,
                    Characteristic.STRENGTH,
                    Characteristic.TOUGHNESS,
                    Characteristic.INITIATIVE,
                    Characteristic.AGILITY,
                    Characteristic.DEXTERITY,
                    Characteristic.INTELLIGENCE,
                    Characteristic.WILL_POWER,
                    Characteristic.FELLOWSHIP,
                ).let { it.chunked(if (breakpoint > Breakpoint.XSmall) it.size else it.size / 2) }
            }

            characteristics.forEach { characteristicInRow ->
                Row(Modifier.fillMaxWidth()) {
                    characteristicInRow.forEach { characteristic ->
                        Column(
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                characteristic.getShortcutName(),
                                style = MaterialTheme.typography.caption,
                            )
                            Text(
                                values.get(characteristic).toString(),
                                style = MaterialTheme.typography.h6,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExperiencePointsSection(
    points: Points,
    save: suspend (Points) -> Unit,
) {
    var experiencePointsDialogVisible by rememberSaveable { mutableStateOf(false) }

    if (experiencePointsDialogVisible) {
        ExperiencePointsDialog(
            value = points,
            save = save,
            onDismissRequest = { experiencePointsDialogVisible = false },
        )
    }

    Column(
        Modifier
            .clickable(onClick = { experiencePointsDialogVisible = true })
            .padding(horizontal = Spacing.large),
    ) {
        val strings = LocalStrings.current.points

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.tiny)) {
            Text(points.experience.toString(), fontWeight = FontWeight.Bold)
            Text(strings.experience)
        }

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                strings.spentExperience(points.spentExperience),
                style = MaterialTheme.typography.caption,
            )
        }
    }
}

@Composable
private fun CareerSection(character: Character) {
    val status = character.status

    Column {
        Text(character.career, fontWeight = FontWeight.Bold)
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                "${character.socialClass} · ${status.tier.localizedName} ${status.standing}",
                style = MaterialTheme.typography.caption
            )
        }
    }
}