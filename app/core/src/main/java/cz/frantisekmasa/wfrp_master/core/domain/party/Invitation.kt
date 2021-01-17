package cz.frantisekmasa.wfrp_master.core.domain.party

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Invitation(
    val partyId: PartyId,
    val partyName: String,
    val accessCode: String
) : Parcelable