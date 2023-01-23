package cz.frantisekmasa.wfrp_master.common.compendium.domain.importer.parsers

import java.io.Closeable
import java.io.InputStream
import java.io.Writer

expect class TextPosition {
    fun getFontSizeInPt(): Float
    fun getX(): Float
    fun getEndX(): Float
    fun getHeight(): Float
    fun getFont(): Font
    fun getUnicode(): String
}

expect class Document : Closeable

expect abstract class Font {
    abstract fun getName(): String
}

expect abstract class PdfTextStripper constructor() {
    fun setStartPage(page: Int)
    fun setEndPage(page: Int)
    fun setSortByPosition(enabled: Boolean)

    protected val textCharactersByArticle: List<List<TextPosition>>
    protected abstract fun onTextLine(text: String, textPositions: List<TextPosition>)

    fun writeText(document: Document, writer: Writer)
    protected abstract fun onPageEnter()
    protected abstract fun onFinish()
}

expect fun loadDocument(inputStream: InputStream): Document
