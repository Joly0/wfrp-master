package cz.muni.fi.rpg.model.domain.character

import com.fasterxml.jackson.annotation.JsonAlias
import cz.muni.fi.rpg.model.domain.common.Ambitions
import cz.muni.fi.rpg.model.domain.common.Money
import java.lang.IllegalArgumentException

data class Character(
    private var name: String,
    val userId: String?,
    private var career: String,
    private var socialClass: String,
    private var psychology: String,
    private var motivation: String,
    private var race: Race,
    @JsonAlias("stats") // TODO: Remove alias in 1.10
    private var characteristicsBase: Stats,
    private var characteristicsAdvances: Stats = Stats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    private var points: Points,
    private var ambitions: Ambitions = Ambitions("", ""),
    private var mutation: String = "",
    private var note: String = "",
    private var hardyTalent: Boolean = false,
    val id: String = userId ?: error("Either ID or UserId must be present") // TODO: Remove this fallback in 1.14
) {
    companion object {
        const val NAME_MAX_LENGTH = 50
        const val CAREER_MAX_LENGTH = 50
        const val SOCIAL_CLASS_MAX_LENGTH = 50
        const val PSYCHOLOGY_MAX_LENGTH = 200
        const val MOTIVATION_MAX_LENGTH = 200
        const val MUTATION_MAX_LENGTH = 200
        const val NOTE_MAX_LENGTH = 400
    }

    private var money: Money = Money.zero()

    init {
        require(listOf(name, userId, career).all { it?.isNotBlank() ?: true })
        require(name.length <= NAME_MAX_LENGTH) { "Character name is too long" }
        require(career.length <= CAREER_MAX_LENGTH) { "Career is too long" }
        require(socialClass.length <= SOCIAL_CLASS_MAX_LENGTH) { "Social class is too long" }
        require(psychology.length <= PSYCHOLOGY_MAX_LENGTH) { "Psychology is too long" }
        require(motivation.length <= MOTIVATION_MAX_LENGTH) { "Motivation is too long" }
        require(mutation.length <= MUTATION_MAX_LENGTH) { "Mutation is too long" }
        require(note.length <= NOTE_MAX_LENGTH) { "Note is too long" }
    }

    fun update(
        name: String,
        career: String,
        socialClass: String,
        race: Race,
        characteristicsBase: Stats,
        characteristicsAdvances: Stats,
        maxWounds: Int,
        psychology: String,
        motivation: String,
        note: String,
        hardyTalent: Boolean
    ) {
        require(listOf(name, career).all { it.isNotBlank() })
        require(name.length <= NAME_MAX_LENGTH) { "Character name is too long" }
        require(career.length <= CAREER_MAX_LENGTH) { "Career is too long" }
        require(socialClass.length <= SOCIAL_CLASS_MAX_LENGTH) { "Social class is too long" }
        require(psychology.length <= PSYCHOLOGY_MAX_LENGTH) { "Psychology is too long" }
        require(motivation.length <= MOTIVATION_MAX_LENGTH) { "Motivation is too long" }
        require(note.length <= NOTE_MAX_LENGTH) { "Note is too long" }
        require(maxWounds > 0) { "Max wounds cannot be less than 1" }

        this.name = name
        this.career = career
        this.socialClass = socialClass
        this.race = race
        this.characteristicsBase = characteristicsBase
        this.characteristicsAdvances = characteristicsAdvances
        points = points.withMaxWounds(maxWounds, if (hardyTalent) getCharacteristics().getToughnessBonus() else 0)
        this.psychology = psychology
        this.motivation = motivation
        this.note = note
        this.hardyTalent = hardyTalent
    }

    fun addMoney(amount: Money) {
        money += amount
    }

    /**
     * @throws NotEnoughMoney
     */
    fun subtractMoney(amount: Money) {
        try {
            money -= amount
        } catch (e: IllegalArgumentException) {
            throw NotEnoughMoney(amount, e)
        }
    }

    fun getName(): String = name

    fun getCareer(): String = career

    fun getRace(): Race = race

    fun getSocialClass(): String = socialClass

    fun getMoney() = money

    fun updatePoints(newPoints: Points) {
        require(
            (!hardyTalent && newPoints.hardyWoundsBonus == 0) ||
            (hardyTalent && newPoints.hardyWoundsBonus == getCharacteristics().getToughnessBonus())
        ) { "Hardy talent and wounds bonus are wrong" }
        points = newPoints
    }

    fun getPoints(): Points = points

    fun getCharacteristics(): Stats = characteristicsBase + characteristicsAdvances
    fun getCharacteristicsBase() = characteristicsBase
    fun getCharacteristicsAdvances() = characteristicsAdvances

    fun updateAmbitions(ambitions: Ambitions) {
        this.ambitions = ambitions
    }

    fun getAmbitions(): Ambitions = ambitions

    fun getPsychology() = psychology

    fun getMotivation() = motivation

    fun getNote() = note

    fun getMutation() = mutation

    fun hasHardyTalent() = hardyTalent
}
