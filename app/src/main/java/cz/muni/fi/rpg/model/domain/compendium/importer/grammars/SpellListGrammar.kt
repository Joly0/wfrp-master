package cz.muni.fi.rpg.model.domain.compendium.importer.grammars

import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.oneOrMore
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.regexToken
import cz.muni.fi.rpg.model.domain.compendium.Spell
import java.util.*

class SpellListGrammar(private val loreName: String) : Grammar<List<Spell>>() {
    private val spellNameWithCastingNumber by regexToken("\\n*[a-zA-Zâ ’\\-]+\\nCN: \\d+")

    private val range by regexToken("[\\n ]Range: ([a-zA-Z0-9 ’]+)(?=\n)")
    private val target by regexToken("[\\n ]Target: ([a-zA-Z0-9 ()’,]+)(?=\n)")
    private val duration by regexToken("[\\n ]Duration: ([a-zA-Z0-9 +’]+)\n")

    private val sentence by regexToken("((?!CN:)(?!Range:)(?!Target:)(?!Duration:)[a-zA-Z0-9 \\[\\],\\n()+\\-–—:;‘’…/!])+?[.\\n]+[ ]*")

    private val effect = oneOrMore(sentence) map { it.joinToString(separator = "") { sentence -> sentence.text } }

    private val spell by
    spellNameWithCastingNumber *
        range *
        target *
        duration *
        effect map { (nameWithCastingNumber, range, target, duration, effect) ->
        val parts = nameWithCastingNumber.text.split("\nCN: ")

        Spell(
            id = UUID.randomUUID(),
            name = parts[0].trim(),
            castingNumber = parts[1].toInt(),
            range = extractTextValue(range),
            target = extractTextValue(target),
            duration = extractTextValue(duration),
            effect = effect.trim(),
            lore = loreName,
        )
    }

    private fun extractTextValue(value: TokenMatch) = value.text.split(':', limit = 2)[1].trim()

    override val rootParser = skip(zeroOrMore(sentence)) *
        oneOrMore(spell) map { it }
}
