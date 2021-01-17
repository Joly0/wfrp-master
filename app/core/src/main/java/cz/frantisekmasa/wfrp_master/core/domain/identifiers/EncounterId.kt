package cz.frantisekmasa.wfrp_master.core.domain.identifiers

import android.os.Parcelable
import cz.frantisekmasa.wfrp_master.core.domain.party.PartyId
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import java.util.*

@Parcelize
data class EncounterId(
    val partyId: PartyId,

    val encounterId: UUID
) : Parcelable