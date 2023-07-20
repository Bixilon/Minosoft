/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.scoreboard

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedMap
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.scoreboard.ScoreboardObjective
import de.bixilon.minosoft.data.scoreboard.ScoreboardPositions
import de.bixilon.minosoft.data.scoreboard.ScoreboardScore
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.ChildedElement
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.manager.SingleChildrenManager
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.renderer.drawable.AsyncDrawable
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.modding.event.events.scoreboard.*
import de.bixilon.minosoft.modding.event.events.scoreboard.team.TeamUpdateEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.util.Initializable
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class ScoreboardSideElement(guiRenderer: GUIRenderer) : Element(guiRenderer), LayoutedElement, Initializable, AsyncDrawable, ChildedElement {
    override val children = SingleChildrenManager()
    private val backgroundElement = ColorElement(guiRenderer, size = Vec2.EMPTY, color = RenderConstants.TEXT_BACKGROUND_COLOR)
    private val nameBackgroundElement = ColorElement(guiRenderer, size = Vec2.EMPTY, color = RenderConstants.TEXT_BACKGROUND_COLOR)
    private val nameElement = TextElement(guiRenderer, "", background = null, parent = this)
    private val scores: LockMap<ScoreboardScore, ScoreboardScoreElement> = lockMapOf()

    override val layoutOffset: Vec2
        get() = super.size.let { return@let Vec2(guiRenderer.screen.scaled.x - it.x, (guiRenderer.screen.scaled.y - it.y) / 2) }
    override val skipDraw: Boolean
        get() = objective == null

    var objective: ScoreboardObjective? = null
        set(value) {
            if (field == value) {
                return
            }
            field = value
            scores.clear()
            tryUpdate()
        }

    init {
        preferredSize = Vec2(MAX_SCOREBOARD_WIDTH, -1)
        tryUpdate()
    }

    private var recalculate = true

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        backgroundElement.render(offset, consumer, options)
        nameBackgroundElement.render(offset, consumer, options)

        nameElement.render(offset + Vec2(HorizontalAlignments.CENTER.getOffset(size.x, nameElement.size.x), 0), consumer, options)
        offset.y += TEXT_PROPERTIES.lineHeight

        this.scores.lock.acquire()
        val scores = this.scores.unsafe.entries.sortedWith { a, b -> a.key.compareTo(b.key) }
        this.scores.lock.release()

        var index = 0
        for ((_, score) in scores) {
            score.render(offset, consumer, options)
            offset.y += TEXT_PROPERTIES.lineHeight

            if (++index >= MAX_SCORES) {
                break
            }
        }
    }

    override fun update() {
        val objective = objective
        if (objective == null) {
            size = Vec2.EMPTY
            return
        }

        this.scores.lock.lock()
        this.scores.unsafe.clear()
        objective.scores.lock.acquire()
        for (score in objective.scores.values) {
            this.scores.unsafe.getOrPut(score) { ScoreboardScoreElement(guiRenderer, score, this) }
        }
        objective.scores.lock.release()
        this.scores.lock.unlock()

        updateName()

        queueSizeRecalculation()
    }

    fun recalculateSize() {
        recalculate = false
        val objective = objective
        if (objective == null) {
            size = Vec2.EMPTY
            return
        }
        val size = Vec2(MIN_WIDTH, TEXT_PROPERTIES.lineHeight)
        size.x = maxOf(size.x, nameElement.size.x)

        val scores = scores.toSynchronizedMap()


        for ((_, element) in scores) {
            element.tryUpdate()
            size.x = maxOf(size.x, element.wishedSize.x)
        }

        size.y += TEXT_PROPERTIES.lineHeight * minOf(MAX_SCORES, scores.size)



        this.size = size
        nameBackgroundElement.preferredSize = Vec2(size.x, TEXT_PROPERTIES.lineHeight)
        backgroundElement.preferredSize = size


        for ((_, element) in scores) {
            element.applySize()
        }
    }

    private fun queueSizeRecalculation() {
        recalculate = true
    }

    fun removeScore(score: ScoreboardScore) {
        scores.remove(score) ?: return
        queueSizeRecalculation()
    }

    fun updateScore(score: ScoreboardScore) {
        scores.synchronizedGetOrPut(score) { ScoreboardScoreElement(guiRenderer, score, this) }
        queueSizeRecalculation()
    }

    fun updateName() {
        nameElement.text = objective?.displayName ?: return
        queueSizeRecalculation()
    }

    override fun init() {
        val connection = context.connection
        connection.events.listen<ObjectivePositionSetEvent> {
            if (it.position != ScoreboardPositions.SIDEBAR) {
                return@listen
            }

            this.objective = it.objective
        }
        connection.events.listen<ScoreboardObjectiveUpdateEvent> {
            if (it.objective != this.objective) {
                return@listen
            }
            this.updateName()
        }
        connection.events.listen<ScoreboardScoreRemoveEvent> {
            if (it.score.objective != this.objective) {
                return@listen
            }
            this.removeScore(it.score)
        }
        connection.events.listen<ScoreboardScorePutEvent> {
            if (it.score.objective != this.objective) {
                return@listen
            }
            this.updateScore(it.score)
        }
        connection.events.listen<ScoreTeamChangeEvent> {
            if (it.score.objective != this.objective) {
                return@listen
            }
            this.updateScore(it.score)
        }
        connection.events.listen<TeamUpdateEvent> {
            val objective = this.objective ?: return@listen
            for ((_, score) in objective.scores) {
                if (it.team != score.team) {
                    continue
                }
                this.updateScore(score)
            }
        }
    }

    override fun drawAsync() {
        // check if content was changed, and we need to re-prepare before drawing
        if (recalculate) {
            recalculateSize()
        }
    }

    companion object : HUDBuilder<LayoutedGUIElement<ScoreboardSideElement>> {
        override val identifier: ResourceLocation = "minosoft:scoreboard".toResourceLocation()
        val TEXT_PROPERTIES = TextRenderProperties()
        const val MAX_SCORES = 15
        const val MIN_WIDTH = 30
        const val MAX_SCOREBOARD_WIDTH = 200

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<ScoreboardSideElement> {
            return LayoutedGUIElement(ScoreboardSideElement(guiRenderer))
        }
    }
}
