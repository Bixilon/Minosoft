/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedMap
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.scoreboard.ScoreboardObjective
import de.bixilon.minosoft.data.scoreboard.ScoreboardPositions
import de.bixilon.minosoft.data.scoreboard.ScoreboardScore
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.Initializable
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.renderer.drawable.AsyncDrawable
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.modding.event.events.scoreboard.*
import de.bixilon.minosoft.modding.event.events.scoreboard.team.TeamUpdateEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class ScoreboardSideElement(guiRenderer: GUIRenderer) : Element(guiRenderer), LayoutedElement, Initializable, AsyncDrawable {
    private val backgroundElement = ColorElement(guiRenderer, size = Vec2i.EMPTY, color = RenderConstants.TEXT_BACKGROUND_COLOR)
    private val nameBackgroundElement = ColorElement(guiRenderer, size = Vec2i.EMPTY, color = RenderConstants.TEXT_BACKGROUND_COLOR)
    private val nameElement = TextElement(guiRenderer, "", background = false, parent = this)
    private val scores: LockMap<ScoreboardScore, ScoreboardScoreElement> = lockMapOf()

    override val layoutOffset: Vec2i
        get() = super.size.let { return@let Vec2i(guiRenderer.scaledSize.x - it.x, (guiRenderer.scaledSize.y - it.y) / 2) }
    override val skipDraw: Boolean
        get() = objective == null

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

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        backgroundElement.render(offset, consumer, options)
        nameBackgroundElement.render(offset, consumer, options)

        nameElement.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, nameElement.size.x), 0), consumer, options)
        offset.y += Font.TOTAL_CHAR_HEIGHT

        this.scores.lock.acquire()
        val scores = this.scores.unsafe.entries.sortedWith { a, b -> a.key.compareTo(b.key) }
        this.scores.lock.release()

        var index = 0
        for ((_, score) in scores) {
            score.render(offset, consumer, options)
            offset.y += Font.TOTAL_CHAR_HEIGHT

            if (++index >= MAX_SCORES) {
                break
            }
        }
    }

    override fun forceSilentApply() {
        val objective = objective
        if (objective == null) {
            _size = Vec2i.EMPTY
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
        val objective = objective
        if (objective == null) {
            _size = Vec2i.EMPTY
            return
        }
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

    private fun queueSizeRecalculation() {
        cacheUpToDate = false
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
        val connection = renderWindow.connection
        connection.registerEvent(CallbackEventInvoker.of<ObjectivePositionSetEvent> {
            if (it.position != ScoreboardPositions.SIDEBAR) {
                return@of
            }

            this.objective = it.objective
        })
        connection.registerEvent(CallbackEventInvoker.of<ScoreboardObjectiveUpdateEvent> {
            if (it.objective != this.objective) {
                return@of
            }
            this.updateName()
        })
        connection.registerEvent(CallbackEventInvoker.of<ScoreboardScoreRemoveEvent> {
            if (it.score.objective != this.objective) {
                return@of
            }
            this.removeScore(it.score)
        })
        connection.registerEvent(CallbackEventInvoker.of<ScoreboardScorePutEvent> {
            if (it.score.objective != this.objective) {
                return@of
            }
            this.updateScore(it.score)
        })
        connection.registerEvent(CallbackEventInvoker.of<ScoreTeamChangeEvent> {
            if (it.score.objective != this.objective) {
                return@of
            }
            this.updateScore(it.score)
        })
        connection.registerEvent(CallbackEventInvoker.of<TeamUpdateEvent> {
            val objective = this.objective ?: return@of
            for ((_, score) in objective.scores) {
                if (it.team != score.team) {
                    continue
                }
                this.updateScore(score)
            }
        })
    }

    override fun drawAsync() {
        // check if content was changed, and we need to re-prepare before drawing
        if (!cacheUpToDate) {
            recalculateSize()
        }
    }

    companion object : HUDBuilder<LayoutedGUIElement<ScoreboardSideElement>> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:scoreboard".toResourceLocation()
        const val MAX_SCORES = 15
        const val MIN_WIDTH = 30
        const val SCORE_HEIGHT = Font.TOTAL_CHAR_HEIGHT
        const val MAX_SCOREBOARD_WIDTH = 200

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<ScoreboardSideElement> {
            return LayoutedGUIElement(ScoreboardSideElement(guiRenderer))
        }
    }
}
