package cz.muni.fi.rpg.ui.common.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.material.Divider
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.gesture.longPressGestureFilter
import cz.frantisekmasa.wfrp_master.core.ui.primitives.ContextMenu
import cz.frantisekmasa.wfrp_master.core.ui.primitives.ItemIcon

@Composable
fun CardItem(
    name: String,
    description: String = "",
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    contextMenuItems: List<ContextMenu.Item>,
    badge: @Composable () -> Unit = {},
) {
    val menuOpened = mutableStateOf(false)

    ListItem(
        modifier = Modifier
            .clickable(onClick = onClick)
            .longPressGestureFilter { menuOpened.value = true },
        icon = { ItemIcon(iconRes, ItemIcon.Size.Small) },
        text = { Text(name) },
        secondaryText = if (description.isNotBlank()) ({ Text(description, maxLines = 1) }) else null,
        trailing = badge,
    )

    Divider()

    ContextMenu(
        items = contextMenuItems,
        onDismissRequest = { menuOpened.value = false },
        expanded = menuOpened.value
    )
}