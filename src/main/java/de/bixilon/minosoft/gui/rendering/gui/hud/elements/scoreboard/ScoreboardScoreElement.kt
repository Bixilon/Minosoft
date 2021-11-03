package de.bixilon.minosoft.gui.rendering.gui.hud.elements.scoreboard

import de.bixilon.minosoft.data.scoreboard.ScoreboardScore
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import glm_.vec2.Vec2i

class ScoreboardScoreElement(
    hudRenderer: HUDRenderer,
    val score: ScoreboardScore,
    parent: Element?,
) : Element(hudRenderer) {
    private val nameElement = TextElement(hudRenderer, "", background = false, parent = this)
    private val scoreElement = TextElement(hudRenderer, "", background = false, parent = this)

    init {
        nameElement.prefMaxSize = Vec2i(-1, ScoreboardSideElement.SCORE_HEIGHT)
        scoreElement.prefMaxSize = Vec2i(-1, ScoreboardSideElement.SCORE_HEIGHT)
        forceSilentApply()
        _parent = parent
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        nameElement.render(offset, z, consumer, options)

        scoreElement.render(offset + Vec2i(HorizontalAlignments.RIGHT.getOffset(size.x, scoreElement.size.x), 0), z, consumer, options)

        return TextElement.LAYERS
    }

    override fun silentApply(): Boolean {
        forceSilentApply()
        return true
    }

    override fun forceSilentApply() {
        val entityName = ChatComponent.of(score.entity)
        nameElement.text = score.team?.decorateName(entityName) ?: entityName

        scoreElement.text = TextComponent(score.value).color(ChatColors.RED)

        _prefSize = Vec2i(nameElement.size.x + scoreElement.size.x + SCORE_MIN_MARGIN, ScoreboardSideElement.SCORE_HEIGHT)
        cacheUpToDate = false
    }

    fun applySize() {
        _size = parent?.size?.let { return@let Vec2i(it.x, ScoreboardSideElement.SCORE_HEIGHT) } ?: _prefSize
    }

    override fun onChildChange(child: Element) = Unit


    companion object {
        private const val SCORE_MIN_MARGIN = 5
    }
}
