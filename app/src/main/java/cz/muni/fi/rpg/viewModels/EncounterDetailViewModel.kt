package cz.muni.fi.rpg.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import arrow.core.Either
import cz.muni.fi.rpg.model.domain.armour.Armor
import cz.muni.fi.rpg.model.domain.character.Stats
import cz.muni.fi.rpg.model.domain.encounter.*
import cz.muni.fi.rpg.model.domain.encounters.EncounterId
import cz.muni.fi.rpg.model.domain.party.Party
import cz.muni.fi.rpg.model.domain.party.PartyNotFound
import cz.muni.fi.rpg.model.domain.party.PartyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.min

class EncounterDetailViewModel(
    private val encounterId: EncounterId,
    private val encounters: EncounterRepository,
    private val combatantRepository: CombatantRepository
) : ViewModel(), CoroutineScope by CoroutineScope(Dispatchers.IO) {
    val encounter: LiveData<Either<EncounterNotFound, Encounter>> = encounters.getLive(encounterId)
    val combatants: LiveData<List<Combatant>> = combatantRepository.findByEncounter(encounterId)

    suspend fun remove() {
        encounters.remove(encounterId)
    }

    suspend fun addCombatant(
        name: String,
        note: String,
        wounds: Wounds,
        stats: Stats,
        armor: Armor,
        enemy: Boolean,
        alive: Boolean,
        traits: List<String>,
        trappings: List<String>
    ) {
        combatantRepository.save(
            encounterId,
            Combatant(
                UUID.randomUUID(),
                name = name,
                note = note,
                wounds = wounds,
                stats = stats,
                armor = armor,
                enemy = enemy,
                alive = alive,
                traits = traits,
                trappings = trappings,
                position = combatantRepository.getNextPosition(encounterId)
            )
        )
    }

    suspend fun updateCombatant(
        id: UUID,
        name: String,
        note: String,
        maxWounds: Int,
        stats: Stats,
        armor: Armor,
        enemy: Boolean,
        alive: Boolean,
        traits: List<String>,
        trappings: List<String>
    ) {
        val combatant = combatantRepository.get(CombatantId(encounterId, id))

        combatant.update(
            name,
            note,
            Wounds(min(combatant.wounds.current, maxWounds), maxWounds),
            stats,
            armor,
            enemy,
            alive,
            traits,
            trappings
        )

        combatantRepository.save(encounterId, combatant)
    }

    /**
     * @throws CombatantNotFound
     */
    suspend fun getCombatant(combatantId: UUID): Combatant {
        return combatantRepository.get(CombatantId(encounterId, combatantId))
    }

    fun removeCombatant(combatantId: UUID) = launch {
        combatantRepository.remove(CombatantId(encounterId, combatantId))
    }
}