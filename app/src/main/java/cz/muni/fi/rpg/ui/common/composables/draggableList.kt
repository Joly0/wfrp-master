package cz.muni.fi.rpg.ui.common.composables

import androidx.compose.animation.animatedFloat
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.gesture.*
import androidx.compose.ui.gesture.scrollorientationlocking.Orientation
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import timber.log.Timber
import java.util.*
import kotlin.math.roundToInt

private fun <T> List<T>.moveItem(sourceIndex: Int, targetIndex: Int): List<T> {
    return toMutableList()
        .apply {
            if (sourceIndex <= targetIndex) {
                Collections.rotate(subList(sourceIndex, targetIndex + 1), -1);
            } else {
                Collections.rotate(subList(targetIndex, sourceIndex + 1), 1);
            }
        }
}

@Composable
fun <T> DraggableListFor(
    items: List<T>,
    itemHeight: Dp,
    onReorder: (List<T>) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (item: T, isDragged: Boolean, modifier: Modifier) -> Unit
) {
    val dragY = animatedFloat(0f)
    val draggedIndex = remember { mutableStateOf<Int?>(null) }
    val yOffset = with(DensityAmbient.current) { dragY.value.toDp() }

    val draggedIndexValue = draggedIndex.value
    val currentlyAboveIndex = draggedIndexValue?.let {
        ((itemHeight * it + yOffset) / itemHeight.value).value.roundToInt().coerceIn(items.indices)
    } ?: 0

    Column(Modifier.then(modifier)) {
        for (index in items.indices) {
            val isDragged = draggedIndex.value == index

            val offsetY = when {
                isDragged -> yOffset
                draggedIndexValue != null
                -> (if (draggedIndexValue < index) -itemHeight else 0.dp) +
                        if (currentlyAboveIndex + 1 <= index) itemHeight else 0.dp
                else -> 0.dp
            }

            itemContent(
                items[index],
                isDragged,
                Modifier
                    .height(itemHeight)
                    .offset(y = offsetY)
                    .zIndex(if (isDragged) 2f else 1f)
                    .fillMaxWidth()
                    .scrollFriendlyDraggable(
                        onDrag = { delta ->
                            dragY.snapTo(dragY.value + delta)
                        },
                        onDragStarted = {
                            dragY.snapTo(0f)
                            draggedIndex.value = index
                        },
                        onDragStopped = {
                            if (index != currentlyAboveIndex) {
                                onReorder(items.moveItem(index, currentlyAboveIndex))
                            }
                            draggedIndex.value = null
                        },
                    )
            )
        }
    }
}

private fun Modifier.scrollFriendlyDraggable(
    onDragStarted: () -> Unit = {},
    onDragStopped: () -> Unit = {},
    onDrag: Density.(Float) -> Unit
) = composed {
    val density = DensityAmbient.current

    longPressDragGestureFilter(
        DragCallback(
            onDragStarted = onDragStarted,
            onDragStopped = onDragStopped,
            onDrag = onDrag,
            density = density,
        )
    )
}


private class DragCallback(
    private val onDragStarted: () -> Unit = {},
    private val onDragStopped: () -> Unit = {},
    private val onDrag: Density.(Float) -> Unit,
    private val density: Density,
) : LongPressDragObserver {

    override fun onLongPress(pxPosition: Offset) {
        onDragStarted()
    }

    override fun onDrag(dragDistance: Offset): Offset {
        with(density) { onDrag(dragDistance.y) }

        return dragDistance
    }

    override fun onCancel() {
        onDragStopped()
    }

    override fun onStop(velocity: Offset) {
        onDragStopped()
    }
}