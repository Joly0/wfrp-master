package cz.muni.fi.rpg.ui.character.skills.adapter

import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import cz.muni.fi.rpg.R
import cz.muni.fi.rpg.common.EntityListener
import cz.muni.fi.rpg.model.domain.skills.Skill
import cz.muni.fi.rpg.model.domain.skills.SkillCharacteristic
import kotlinx.android.synthetic.main.skill_item.view.*

class SkillHolder(
    private val view: View,
    private val onClickListener: EntityListener<Skill>,
    private val onRemoveListener: EntityListener<Skill>
) : RecyclerView.ViewHolder(view) {
    fun bind(skill: Skill) {
        view.skillItemTitle.text = skill.name;
        view.skillItemDescription.text = skill.description

        if (skill.description.isBlank()) {
            view.skillItemDescription.visibility = View.GONE

            ConstraintSet().apply {
                clone(view.skillItemLayout)
                connect(
                    R.id.skillItemTitle,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )
                applyTo(view.skillItemLayout)
            }
        }

        view.skillIcon.setImageResource(
            when (skill.characteristic) {
                SkillCharacteristic.AGILITY -> R.drawable.ic_agility
                SkillCharacteristic.INTELLIGENCE -> R.drawable.ic_intelligence
                SkillCharacteristic.FELLOWSHIP -> R.drawable.ic_fellowship
                SkillCharacteristic.STRENGTH -> R.drawable.ic_strength
                SkillCharacteristic.TOUGHNESS -> R.drawable.ic_toughness
                SkillCharacteristic.WILL_POWER -> R.drawable.ic_will_power
            }
        )

        view.skillItemMastery.setImageResource(
            when (skill.mastery) {
                1 -> R.drawable.ic_skill_mastery_1
                2 -> R.drawable.ic_skill_mastery_2
                3 -> R.drawable.ic_skill_mastery_3
                else -> error("Invalid skill mastery")
            }
        )
        
        view.setOnCreateContextMenuListener { menu, v, _ ->
            menu.add(0, v.id, 0, R.string.remove)
                .setOnMenuItemClickListener {
                    onRemoveListener(skill)

                    false
                }
        }
        view.setOnClickListener { onClickListener(skill) };
    }
}