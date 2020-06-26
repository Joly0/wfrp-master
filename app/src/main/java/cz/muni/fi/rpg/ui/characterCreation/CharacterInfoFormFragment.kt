package cz.muni.fi.rpg.ui.characterCreation

import android.os.Bundle
import android.view.View
import cz.muni.fi.rpg.R
import cz.muni.fi.rpg.model.domain.character.Character
import cz.muni.fi.rpg.model.domain.character.Race
import cz.muni.fi.rpg.ui.common.forms.Form
import kotlinx.android.synthetic.main.fragment_character_info_form.*

class CharacterInfoFormFragment :
    CharacterFormStep<CharacterInfoFormFragment.Data>(R.layout.fragment_character_info_form) {
    companion object {
        private const val STATE_NAME = "infoName"
        private const val STATE_RACE = "infoRace"
        private const val STATE_CAREER = "infoCareer"
        private const val STATE_SOCIAL_CLASS = "infoClass"
        private const val STATE_PSYCHOLOGY = "infoPsychology"
        private const val STATE_MOTIVATION = "infoMotivation"
        private const val STATE_NOTE = "infoNote"
    }

    var character: Character? = null

    data class Data(
        val name: String,
        val race: Race,
        val career: String,
        val socialClass: String,
        val psychology: String,
        val motivation: String,
        val note: String
    )

    private lateinit var form: Form

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_NAME, nameInput.getValue())
            outState.putInt(STATE_RACE, radioGroup.checkedRadioButtonId)
            outState.putString(STATE_CAREER, careerInput.getValue())
            outState.putString(STATE_SOCIAL_CLASS, socialClassInput.getValue())
            outState.putString(STATE_PSYCHOLOGY, psychologyInput.getValue())
            outState.putString(STATE_MOTIVATION, motivationInput.getValue())
            outState.putString(STATE_NOTE, noteInput.getValue())

        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        form = Form(requireContext()).apply {
            addTextInput(nameInput).apply {
                setNotBlank(getString(R.string.error_cannot_be_empty))
                setMaxLength(Character.NAME_MAX_LENGTH, showCounter = false)
            }

            addTextInput(careerInput).apply {
                setNotBlank(getString(R.string.error_cannot_be_empty))
                setMaxLength(Character.CAREER_MAX_LENGTH, showCounter = false)
            }

            addTextInput(socialClassInput).apply {
                setNotBlank(getString(R.string.error_cannot_be_empty))
                setMaxLength(Character.SOCIAL_CLASS_MAX_LENGTH, showCounter = false)
            }

            addTextInput(psychologyInput).apply {
                setMaxLength(Character.PSYCHOLOGY_MAX_LENGTH, showCounter = false)
            }

            addTextInput(motivationInput).apply {
                setMaxLength(Character.MOTIVATION_MAX_LENGTH, showCounter = false)
            }

            addTextInput(noteInput).apply {
                setMaxLength(Character.NOTE_MAX_LENGTH, showCounter = false)
            }
        }

        setDefaultValues()

        savedInstanceState?.let {
            it.getString(STATE_NAME)?.let(nameInput::setDefaultValue)
            it.getInt(STATE_RACE).let(radioGroup::check)
            it.getString(STATE_CAREER)?.let(careerInput::setDefaultValue)
            it.getString(STATE_SOCIAL_CLASS)?.let(socialClassInput::setDefaultValue)
            it.getString(STATE_NOTE)?.let(noteInput::setDefaultValue)
        }
    }

    override fun submit(): Data? {
        if (!form.validate()) {
            return null
        }

        return createCharacterInfo()
    }

    override fun setCharacterData(character: Character) {
        this.character = character
        setDefaultValues()
    }

    private fun setDefaultValues() {
        val character = this.character ?: return

        nameInput.setDefaultValue(character.getName())
        careerInput.setDefaultValue(character.getCareer())
        socialClassInput.setDefaultValue(character.getSocialClass())
        psychologyInput.setDefaultValue(character.getPsychology())
        motivationInput.setDefaultValue(character.getMotivation())
        noteInput.setDefaultValue(character.getNote())

        when (character.getRace()) {
            Race.HUMAN -> radioButtonRaceHuman.isChecked = true
            Race.DWARF -> radioButtonRaceDwarf.isChecked = true
            Race.HIGH_ELF -> radioButtonRaceElf.isChecked = true
            Race.WOOD_ELF -> radioButtonRaceGnome.isChecked = true
            Race.HALFLING -> radioButtonRaceHalfling.isChecked = true
        }
    }

    private fun createCharacterInfo(): Data {
        val name = nameInput.getValue()
        val career = careerInput.getValue()
        val socialClass = socialClassInput.getValue()

        val race: Race = when (radioGroup.checkedRadioButtonId) {
            R.id.radioButtonRaceHuman -> Race.HUMAN
            R.id.radioButtonRaceDwarf -> Race.DWARF
            R.id.radioButtonRaceElf -> Race.HIGH_ELF
            R.id.radioButtonRaceGnome -> Race.WOOD_ELF
            R.id.radioButtonRaceHalfling -> Race.HALFLING
            else -> error("No race selected")
        }

        return Data(
            name = name,
            race = race,
            career = career,
            socialClass = socialClass,
            psychology = psychologyInput.getValue(),
            motivation = motivationInput.getValue(),
            note = noteInput.getValue()
        )
    }
}
