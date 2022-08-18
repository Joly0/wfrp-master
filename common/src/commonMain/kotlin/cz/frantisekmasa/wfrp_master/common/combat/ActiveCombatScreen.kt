package cz.frantisekmasa.wfrp_master.common.combat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cz.frantisekmasa.wfrp_master.common.character.CharacterDetailScreen
import cz.frantisekmasa.wfrp_master.common.core.auth.LocalUser
import cz.frantisekmasa.wfrp_master.common.core.domain.party.PartyId
import cz.frantisekmasa.wfrp_master.common.core.shared.IO
import cz.frantisekmasa.wfrp_master.common.core.shared.Resources
import cz.frantisekmasa.wfrp_master.common.core.ui.CharacterAvatar
import cz.frantisekmasa.wfrp_master.common.core.ui.StatBlock
import cz.frantisekmasa.wfrp_master.common.core.ui.StatBlockData
import cz.frantisekmasa.wfrp_master.common.core.ui.buttons.BackButton
import cz.frantisekmasa.wfrp_master.common.core.ui.flow.collectWithLifecycle
import cz.frantisekmasa.wfrp_master.common.core.ui.forms.NumberPicker
import cz.frantisekmasa.wfrp_master.common.core.ui.menu.DropdownMenuItem
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.DraggableListFor
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.FullScreenProgress
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.ItemIcon
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.Spacing
import cz.frantisekmasa.wfrp_master.common.core.ui.primitives.rememberScreenModel
import cz.frantisekmasa.wfrp_master.common.core.ui.scaffolding.LocalPersistentSnackbarHolder
import cz.frantisekmasa.wfrp_master.common.core.ui.scaffolding.OptionsAction
import cz.frantisekmasa.wfrp_master.common.core.ui.scaffolding.Subtitle
import cz.frantisekmasa.wfrp_master.common.encounters.CombatantItem
import cz.frantisekmasa.wfrp_master.common.encounters.domain.Wounds
import cz.frantisekmasa.wfrp_master.common.localization.LocalStrings
import cz.frantisekmasa.wfrp_master.common.npcs.NpcDetailScreen
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActiveCombatScreen(
    private val partyId: PartyId,
): Screen {
    @Composable
    override fun Content() {
        val viewModel: CombatScreenModel = rememberScreenModel(arg = partyId)

        AutoCloseOnEndedCombat(viewModel)

        val coroutineScope = rememberCoroutineScope()

        val party = viewModel.party.collectWithLifecycle(null).value
        val combatants = remember { viewModel.combatants() }.collectWithLifecycle(null).value
        val isGameMaster = LocalUser.current.id == party?.gameMasterId

        var openedCombatant by remember { mutableStateOf<CombatantItem?>(null) }
        val bottomSheetState = rememberNotSavedModalBottomSheetState()

        ModalBottomSheetLayout(
            sheetState = bottomSheetState,
            sheetShape = MaterialTheme.shapes.small,
            sheetContent = {
                if (!bottomSheetState.isVisible) {
                    Box(Modifier.height(1.dp))
                    return@ModalBottomSheetLayout
                }

                openedCombatant?.let { combatant ->
                    if (party == null) {
                        return@let
                    }

                    /*
                 There are two things happening
                 1. We always need fresh version of given combatant,
                    because user may have edited combatant, i.e. by changing her advantage.
                    So we cannot used value from saved mutable state.
                 2. We have to show sheet only if fresh combatant is in the collection,
                    because she may have been removed from combat.
                 */
                    val freshCombatant =
                        combatants?.firstOrNull { it.areSameEntity(combatant) } ?: return@let

                    val advantageCap by derivedStateOf {
                        party.settings.advantageCap.calculate(freshCombatant.characteristics)
                    }

                    CombatantSheet(freshCombatant, viewModel, advantageCap)
                }
            },
        ) {
            Column {
                val strings = LocalStrings.current

                Scaffold(
                    modifier = Modifier.weight(1f),
                    topBar = {
                        TopAppBar(
                            navigationIcon = { BackButton() },
                            title = {
                                Column {
                                    Text(strings.combat.title)
                                    party?.let { Subtitle(it.name) }
                                }
                            },
                            actions = {
                                if (!isGameMaster) {
                                    return@TopAppBar
                                }

                                OptionsAction {
                                    DropdownMenuItem(
                                        content = { Text(strings.combat.buttonEndCombat) },
                                        onClick = {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                viewModel.endCombat()
                                            }
                                        }
                                    )
                                }
                            }
                        )
                    },
                ) {
                    val round = viewModel.round.collectWithLifecycle(null).value
                    val turn = viewModel.turn.collectWithLifecycle(null).value

                    if (combatants == null || round == null || turn == null || party == null) {
                        FullScreenProgress()
                        return@Scaffold
                    }

                    Column {
                        Column(
                            Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            CombatantList(
                                coroutineScope = coroutineScope,
                                combatants = combatants,
                                viewModel = viewModel,
                                turn = turn,
                                isGameMaster = isGameMaster,
                                onCombatantClicked = {
                                    openedCombatant = it
                                    coroutineScope.launch { bottomSheetState.show() }
                                }
                            )
                        }

                        if (isGameMaster) {
                            BottomBar(turn, round, viewModel)
                        }
                    }
                }
            }
        }
    }

    private fun canEditCombatant(userId: String, isGameMaster: Boolean, combatant: CombatantItem) =
        isGameMaster || (combatant is CombatantItem.Character && combatant.userId == userId)

    @Composable
    private fun BottomBar(turn: Int, round: Int, viewModel: CombatScreenModel) {
        val coroutineScope = rememberCoroutineScope()
        val strings = LocalStrings.current.combat

        BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    enabled = round > 1 || turn > 1,
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) { viewModel.previousTurn() }
                    },
                ) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        strings.iconPreviousTurn,
                    )
                }

                Text(strings.nthRound(round))

                IconButton(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) { viewModel.nextTurn() }
                    }
                ) {
                    Icon(
                        Icons.Rounded.ArrowForward,
                        strings.iconNextTurn,
                    )
                }
            }
        }
    }

    @Composable
    private fun rememberNotSavedModalBottomSheetState(): ModalBottomSheetState {
        return remember {
            ModalBottomSheetState(
                initialValue = ModalBottomSheetValue.Hidden,
                animationSpec = SwipeableDefaults.AnimationSpec,
                confirmStateChange = { true },
            )
        }
    }

    @Composable
    private fun AutoCloseOnEndedCombat(screenModel: CombatScreenModel) {
        val isCombatActive = screenModel.isCombatActive.collectWithLifecycle(true).value
        val message = LocalStrings.current.combat.messages.noActiveCombat
        val snackbarHostState = LocalPersistentSnackbarHolder.current

        if (!isCombatActive) {
            val navigator = LocalNavigator.currentOrThrow

            LaunchedEffect(Unit) {
                Napier.d("Closing combat screen")

                snackbarHostState.showSnackbar(message)

                navigator.pop()
            }
        }
    }

    @Composable
    private fun CombatantList(
        coroutineScope: CoroutineScope,
        combatants: List<CombatantItem>,
        viewModel: CombatScreenModel,
        turn: Int,
        isGameMaster: Boolean,
        onCombatantClicked: (CombatantItem) -> Unit,
    ) {
        val userId = LocalUser.current.id

        DraggableListFor(
            combatants,
            onReorder = { items ->
                coroutineScope.launch(Dispatchers.IO) {
                    viewModel.reorderCombatants(items.map { it.combatant })
                }
            },
            modifier = Modifier.padding(Spacing.bodyPadding),
            itemSpacing = Spacing.small,
        ) { index, combatant, isDragged ->
            CombatantListItem(
                onTurn = index == turn - 1,
                combatant,
                isDragged = isDragged,
                modifier = when {
                    canEditCombatant(userId, isGameMaster, combatant) -> Modifier.clickable {
                        onCombatantClicked(combatant)
                    }
                    else -> Modifier
                }
            )
        }
    }

    @Composable
    private fun CombatantSheet(
        combatant: CombatantItem,
        viewModel: CombatScreenModel,
        advantageCap: Int,
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            val navigator = LocalNavigator.currentOrThrow

            Row(
                Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    combatant.name,
                    style = MaterialTheme.typography.h6,
                )

                IconButton(
                    onClick = {
                        navigator.push(
                            when (combatant) {
                                is CombatantItem.Npc -> NpcDetailScreen(combatant.npcId)
                                is CombatantItem.Character -> CharacterDetailScreen(
                                    combatant.characterId,
                                    comingFromCombat = true,
                                )
                            }
                        )
                    },
                ) {
                    Icon(
                        Icons.Rounded.OpenInNew,
                        LocalStrings.current.commonUi.buttonDetail,
                        tint = MaterialTheme.colors.primary
                    )
                }
            }

            var statBlockData: StatBlockData? by rememberSaveable { mutableStateOf(null) }

            StatBlock(combatant.characteristics, statBlockData)

            LaunchedEffect(combatant.combatant.id) {
                withContext(Dispatchers.IO) {
                    statBlockData = viewModel.getStatBlockData(combatant)
                }
            }

            Divider()

            Row(Modifier.padding(bottom = Spacing.medium)) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                    CombatantWounds(combatant, viewModel)
                }

                Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                    CombatantAdvantage(combatant, viewModel, advantageCap)
                }
            }
        }
    }

    @Composable
    private fun CombatantWounds(combatant: CombatantItem, viewModel: CombatScreenModel) {
        val coroutineScope = rememberCoroutineScope()
        val updateWounds = { wounds: Wounds ->
            coroutineScope.launch(Dispatchers.IO) {
                viewModel.updateWounds(combatant, wounds)
            }
        }

        val wounds = combatant.wounds

        NumberPicker(
            label = LocalStrings.current.points.wounds,
            value = wounds.current,
            onIncrement = { updateWounds(wounds.restore(1)) },
            onDecrement = { updateWounds(wounds.lose(1)) },
        )
    }

    @Composable
    private fun CombatantAdvantage(
        combatant: CombatantItem,
        viewModel: CombatScreenModel,
        advantageCap: Int,
    ) {
        val coroutineScope = rememberCoroutineScope()
        val updateAdvantage = { advantage: Int ->
            coroutineScope.launch(Dispatchers.IO) {
                viewModel.updateAdvantage(combatant.combatant, advantage)
            }
        }

        val advantage = combatant.combatant.advantage

        NumberPicker(
            label = LocalStrings.current.combat.labelAdvantage,
            value = advantage,
            onIncrement = { updateAdvantage((advantage + 1).coerceAtMost(advantageCap)) },
            onDecrement = { updateAdvantage(advantage - 1) },
        )
    }

    @Composable
    private fun CombatantListItem(
        onTurn: Boolean,
        combatant: CombatantItem,
        isDragged: Boolean,
        modifier: Modifier
    ) {
        Surface(
            modifier = modifier,
            elevation = if (isDragged) 6.dp else 2.dp,
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(Modifier.height(IntrinsicSize.Max)) { /* TODO: REMOVE COMMENT */
                Box(
                    Modifier
                        .fillMaxHeight()
                        .background(
                            if (onTurn)
                                MaterialTheme.colors.primary
                            else MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
                        )
                        .width(Spacing.small)
                )

                ListItem(
                    icon = {
                        when (combatant) {
                            is CombatantItem.Character -> {
                                CharacterAvatar(combatant.avatarUrl, ItemIcon.Size.Small)
                            }
                            is CombatantItem.Npc -> {
                                ItemIcon(Resources.Drawable.Npc, ItemIcon.Size.Small)
                            }
                        }
                    },
                    text = { Text(combatant.name) },
                    trailing = {
                        val advantage = combatant.combatant.advantage

                        if (advantage > 0) {
                            Text("$advantage A", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }

}