package cz.frantisekmasa.wfrp_master.common.core.ui.primitives

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cz.frantisekmasa.wfrp_master.common.core.shared.Resources
import cz.frantisekmasa.wfrp_master.common.core.shared.drawableResource

object EmptyUI {
    enum class Size {

        /**
         * Should be used, when there are more lists on a screen and this empty UI is only related
         * to part of it
         */
        Small,

        /**
         * Should be used when main content of screen is empty
         */
        Large;

        val modifier: Modifier
            get() = when (this) {
                Small ->
                    Modifier
                        .width(60.dp)
                        .padding(top = 16.dp)
                Large -> Modifier.width(64.dp)
            }

        val textStyle: TextStyle
            @Composable
            get() = when (this) {
                Small -> MaterialTheme.typography.subtitle1
                Large -> MaterialTheme.typography.h6
            }
    }
}

@Composable
fun EmptyUI(
    text: String,
    icon: Resources.Drawable,
    subText: String? = null,
    size: EmptyUI.Size = EmptyUI.Size.Large
) {
    EmptyUI(text, drawableResource(icon), subText, size)
}

@Composable
fun EmptyUI(
    text: String,
    icon: ImageVector,
    subText: String? = null,
    size: EmptyUI.Size = EmptyUI.Size.Large
) {
    EmptyUI(text, rememberVectorPainter(icon), subText, size)
}

@Composable
fun EmptyUI(
    text: String,
    iconPainter: Painter,
    subText: String? = null,
    size: EmptyUI.Size = EmptyUI.Size.Large
) {
    val disabledColor = MaterialTheme.colors.onSurface.copy(ContentAlpha.disabled)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (size == EmptyUI.Size.Large) {
            Spacer(Modifier.fillMaxHeight(0.35f))
        }

        Image(
            iconPainter,
            contentDescription = VisualOnlyIconDescription,
            modifier = size.modifier,
            colorFilter = ColorFilter.tint(disabledColor),
        )

        Text(text, style = size.textStyle)

        subText?.let {
            Text(it, textAlign = TextAlign.Center, color = disabledColor)
        }
    }
}