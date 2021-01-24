package cz.frantisekmasa.wfrp_master.inventory.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

typealias InventoryItemId = UUID

@Parcelize
/* internal */ data class InventoryItem(
    val id: InventoryItemId,
    val name: String,
    val description: String,
    val quantity: Int,
    val encumbrance: Encumbrance = Encumbrance.Zero,
) : Parcelable {
    companion object {
        const val NAME_MAX_LENGTH = 50
        const val DESCRIPTION_MAX_LENGTH = 200
    }

    init {
        require(name.isNotBlank()) { "Inventory item must have non-blank name" }
        require(quantity > 0) { "Inventory item quantity must be > 0" }
        require(name.length <= NAME_MAX_LENGTH) { "Maximum allowed name length is $NAME_MAX_LENGTH" }
        require(description.length <= DESCRIPTION_MAX_LENGTH) { "Maximum allowed description length is $DESCRIPTION_MAX_LENGTH" }
    }
}