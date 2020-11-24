package cz.muni.fi.rpg.viewModels

import arrow.core.Either
import arrow.core.extensions.list.foldable.exists
import cz.muni.fi.rpg.model.domain.character.Character
import cz.muni.fi.rpg.model.domain.character.CharacterId
import cz.muni.fi.rpg.model.domain.character.CharacterRepository
import cz.muni.fi.rpg.model.domain.common.Ambitions
import cz.muni.fi.rpg.model.domain.party.Party
import cz.muni.fi.rpg.model.domain.party.PartyNotFound
import cz.muni.fi.rpg.model.domain.party.PartyRepository
import cz.muni.fi.rpg.model.domain.party.time.DateTime
import cz.muni.fi.rpg.model.right
import cz.muni.fi.rpg.ui.gameMaster.adapter.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.zip
import java.util.*

class GameMasterViewModel(
    private val partyId: UUID,
    private val parties: PartyRepository,
    private val characterRepository: CharacterRepository
) {

    val party: Flow<Either<PartyNotFound, Party>> = parties.getLive(partyId)
    val characters: Flow<List<Character>> = characterRepository.inParty(partyId)

    /**
     * Returns flow which emits either CharacterId of players current character or NULL if user
     * didn't create character yet
     */
    fun getPlayers(): Flow<List<Player>> {
        return party.right().zip(characters) { party, characters ->
            val players = characters.map { Player.ExistingCharacter(it) }
            val usersWithoutCharacter = party.users
                .filter { it != party.gameMasterId }
                .filter { userId -> !players.exists { it.character.userId == userId } }
                .map { Player.UserWithoutCharacter(it) }

            players + usersWithoutCharacter
        }
    }

    suspend fun archiveCharacter(id: CharacterId) {
        val character = characterRepository.get(id)

        character.archive()

        characterRepository.save(id.partyId, character)
    }

    suspend fun renameParty(newName: String) {
        val party = parties.get(partyId)

        party.rename(newName)

        parties.save(party)
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