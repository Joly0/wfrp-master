package cz.muni.fi.rpg.viewModels

import androidx.lifecycle.ViewModel
import cz.frantisekmasa.wfrp_master.common.core.domain.Ambitions
import cz.frantisekmasa.wfrp_master.common.core.domain.character.CharacterRepository
import cz.frantisekmasa.wfrp_master.common.core.domain.identifiers.CharacterId
import cz.frantisekmasa.wfrp_master.common.core.domain.party.Party
import cz.frantisekmasa.wfrp_master.common.core.domain.party.PartyId
import cz.frantisekmasa.wfrp_master.common.core.domain.party.PartyRepository
import cz.frantisekmasa.wfrp_master.common.core.domain.time.DateTime
import cz.frantisekmasa.wfrp_master.common.core.utils.right
import cz.muni.fi.rpg.ui.gameMaster.adapter.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.filterNotNull

class GameMasterViewModel(
    private val partyId: PartyId,
    private val parties: PartyRepository,
    private val characterRepository: CharacterRepository
) : ViewModel() {

    val party: Flow<Party> = parties.getLive(partyId).right()

    /**
     * Returns LiveData which emits either CharacterId of players current character or NULL if user
     * didn't create character yet
     */
    val players: Flow<List<Player>?> =
        party.filterNotNull().combineTransform(characterRepository.inParty(partyId)) { party, characters ->
            val players = characters.map { Player.ExistingCharacter(it) }
            val usersWithoutCharacter = party.getPlayers()
                .filter { userId -> players.none { it.character.userId == userId.toString() } }
                .map { Player.UserWithoutCharacter(it.toString()) }

            emit(players + usersWithoutCharacter)
        }

    suspend fun archiveCharacter(id: CharacterId) {
        val character = characterRepository.get(id)

        character.archive()

        characterRepository.save(id.partyId, character)
    }

    suspend fun changeTime(change: (DateTime) -> DateTime) {
        val party = parties.get(partyId)

        party.changeTime(change(party.getTime()))

        parties.save(party)
    }

    suspend fun updatePartyAmbitions(ambitions: Ambitions) {
        val party = parties.get(partyId)

        party.updateAmbitions(ambitions)

        parties.save(party)
    }
}
