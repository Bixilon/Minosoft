package de.bixilon.minosoft.gui.rendering.gui.hud.elements.scoreboard

import de.bixilon.minosoft.data.scoreboard.ScoreboardObjective
import de.bixilon.minosoft.data.scoreboard.ScoreboardScore
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.EMPTY
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import glm_.vec2.Vec2i

class ScoreboardSideElement(hudRenderer: HUDRenderer) : Element(hudRenderer) {
    private val backgroundElement = ColorElement(hudRenderer, size = Vec2i.EMPTY, color = RenderConstants.TEXT_BACKGROUND_COLOR)
    private val nameBackgroundElement = ColorElement(hudRenderer, size = Vec2i.EMPTY, color = RenderConstants.TEXT_BACKGROUND_COLOR)
    private val nameElement = TextElement(hudRenderer, "", background = false, parent = this)
    private val scores: MutableMap<ScoreboardScore, ScoreboardScoreElement> = synchronizedMapOf()


    var objective: ScoreboardObjective? = null
        set(value) {
            if (field == value) {
                return
            }
            field = value
            scores.clear()
            forceSilentApply()
        }

    init {
        _prefMaxSize = Vec2i(MAX_SCOREBOARD_WIDTH, -1)
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        backgroundElement.render(offset, z, consumer, options)
        nameBackgroundElement.render(offset, z + 1, consumer, options)

        nameElement.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, nameElement.size.x), 0), z + 2, consumer, options)
        offset.y += Font.TOTAL_CHAR_HEIGHT

        val scores = scores.toSynchronizedMap().entries.sortedWith { a, b -> a.key.compareTo(b.key) }
        var index = 0
        for ((_, score) in scores) {
            score.render(offset, z + 2, consumer, options)
            offset.y += score.size.y

            if (++index >= MAX_SCORES) {
                break
            }
        }

        return TextElement.LAYERS + 2 // 2 backgrounds
    }

    override fun forceSilentApply() {
        val objective = objective
        if (objective == null) {
            _size = Vec2i.EMPTY
            return
        }

        this.scores.clear()

        updateName()

        queueSizeRecalculation()
    }

    fun recalculateSize() {
        val size = Vec2i(MIN_WIDTH, Font.TOTAL_CHAR_HEIGHT)
        size.x = maxOf(size.x, nameElement.size.x)

        val scores = scores.toSynchronizedMap()


        for ((_, element) in scores) {
            element.forceSilentApply()
            size.x = maxOf(size.x, element.prefSize.x)
        }

        size.y += SCORE_HEIGHT * minOf(MAX_SCORES, scores.size)



        _size = size
        nameBackgroundElement.size = Vec2i(size.x, SCORE_HEIGHT)
        backgroundElement.size = size


        for ((_, element) in scores) {
            element.applySize()
        }
    }

    @Synchronized
    private fun queueSizeRecalculation() {
        cacheUpToDate = false
    }

    fun removeScore(score: ScoreboardScore) {
        scores.remove(score) ?: return
        queueSizeRecalculation()
    }

    fun updateScore(score: ScoreboardScore) {
        scores.getOrPut(score) { ScoreboardScoreElement(hudRenderer, score, this) }
        queueSizeRecalculation()
    }

    fun updateName() {
        nameElement.text = objective?.displayName ?: return
        queueSizeRecalculation()
    }

    companion object {
        const val MAX_SCORES = 15
        const val MIN_WIDTH = 30
        const val SCORE_HEIGHT = Font.TOTAL_CHAR_HEIGHT
        const val MAX_SCOREBOARD_WIDTH = 200
    }
}
