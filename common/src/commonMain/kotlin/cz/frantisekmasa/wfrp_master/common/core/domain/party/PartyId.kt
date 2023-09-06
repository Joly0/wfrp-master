package cz.frantisekmasa.wfrp_master.common.core.domain.party

import androidx.compose.runtime.Immutable
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
@Parcelize
@Immutable
value class PartyId(@Contextual private val value: Uuid) : Parcelable {
    companion object {
        fun generate(): PartyId = PartyId(uuid4())
    }

    override fun toString() = value.toString()
}
