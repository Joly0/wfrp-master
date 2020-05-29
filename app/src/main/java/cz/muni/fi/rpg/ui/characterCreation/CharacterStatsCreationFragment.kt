package cz.muni.fi.rpg.ui.characterCreation

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import cz.muni.fi.rpg.R
import cz.muni.fi.rpg.model.domain.character.Points
import cz.muni.fi.rpg.model.domain.character.Stats
import kotlinx.android.synthetic.main.fragment_character_stats_creation.*
import kotlinx.android.synthetic.main.fragment_character_stats_creation.view.*

class CharacterStatsCreationFragment : Fragment(R.layout.fragment_character_stats_creation) {
    lateinit var listener: CharacterStatsCreationListener

    public interface CharacterStatsCreationListener {
        fun previousFragment()
        fun saveCharacter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_previous.setOnClickListener{
            listener.previousFragment()
        }

        button_finish.setOnClickListener{
            if(checkValues(view)) {
                listener.saveCharacter()
            }
        }

    }

    private fun checkValues(view: View): Boolean {
        if (view.WeaponSkillTextFill.text.toString().isBlank()) view.WeaponSkillTextFill .setText("0")
        if (view.BallisticSkillTextFill.text.toString().isBlank()) view.BallisticSkillTextFill.setText("0")
        if (view.MagicTextFill.text.toString().isBlank()) view.MagicTextFill.setText("0")
        if (view.StrengthTextFill.text.toString().isBlank()) view.StrengthTextFill.setText("0")
        if (view.ToughnessTextFill.text.toString().isBlank()) view.ToughnessTextFill.setText("0")
        if (view.AgilityTextFill.text.toString().isBlank()) view.AgilityTextFill.setText("0")
        if (view.IntelligenceTextFill.text.toString().isBlank()) view.IntelligenceTextFill.setText("0")
        if (view.WillPowerTextFill.text.toString().isBlank()) view.WillPowerTextFill.setText("0")
        if (view.FellowshipTextFill.text.toString().isBlank()) view.FellowshipTextFill.setText("0")
        if (view.WoundsTextFill.text.toString().isBlank()) view.WoundsTextFill.setText("0")
        if (view.FateTextFill.text.toString().isBlank()) view.FateTextFill.setText("0")

        if (view.WoundsTextFill.text.toString().toInt() == 0) {
            showError(view.WoundsTextFill)
            return false
        }
        return true
    }

    private fun showError(input: EditText) {
        input.error = ("Value cannot be 0")
    }

    fun getData() : Pair <Stats,Points> {
        val stats = Stats(view?.WeaponSkillTextFill?.text.toString().toInt(), view?.BallisticSkillTextFill?.text.toString().toInt(),
            view?.StrengthTextFill?.text.toString().toInt(), view?.ToughnessTextFill?.text.toString().toInt(), view?.AgilityTextFill?.text.toString().toInt(),
            view?.IntelligenceTextFill?.text.toString().toInt(), view?.WillPowerTextFill?.text.toString().toInt(), view?.FellowshipTextFill?.text.toString().toInt(),
            view?.MagicTextFill?.text.toString().toInt())
        val points = Points(0, view?.FateTextFill?.text.toString().toInt(), view?.FateTextFill?.text.toString().toInt(),
            view?.WoundsTextFill?.text.toString().toInt(), view?.WoundsTextFill?.text.toString().toInt())

        return Pair(stats, points)
    }

    fun setCharacterStatsCreationListener(callback: CharacterStatsCreationListener): CharacterStatsCreationFragment {
        this.listener = callback
        return this
    }

}