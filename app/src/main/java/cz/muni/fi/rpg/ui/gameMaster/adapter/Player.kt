package cz.muni.fi.rpg.ui.gameMaster.adapter

import cz.muni.fi.rpg.model.domain.character.Character

sealed class Player {
    data class UserWithoutCharacter(val userId: String) : Player()
    data class ExistingCharacter(val character: Character) : Player()
}