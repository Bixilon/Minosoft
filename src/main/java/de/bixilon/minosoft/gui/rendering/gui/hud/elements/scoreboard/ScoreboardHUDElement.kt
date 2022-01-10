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

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.scoreboard.ScoreboardPositions
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.LayoutedHUDElement
import de.bixilon.minosoft.gui.rendering.renderer.Drawable
import de.bixilon.minosoft.modding.event.events.scoreboard.*
import de.bixilon.minosoft.modding.event.events.scoreboard.team.TeamUpdateEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i

class ScoreboardHUDElement(hudRenderer: HUDRenderer) : LayoutedHUDElement<ScoreboardSideElement>(hudRenderer), Drawable {
    private val connection = hudRenderer.connection
    override val layout = ScoreboardSideElement(hudRenderer)

    override val layoutOffset: Vec2i
        get() = Vec2i(guiRenderer.scaledSize.x - layout.size.x, (guiRenderer.scaledSize.y - layout.size.y) / 2)

    override val skipDraw: Boolean
        get() = layout.objective == null

    override fun init() {
        connection.registerEvent(CallbackEventInvoker.of<ObjectivePositionSetEvent> {
            if (it.position != ScoreboardPositions.SIDEBAR) {
                return@of
            }

            layout.objective = it.objective
        })
        connection.registerEvent(CallbackEventInvoker.of<ScoreboardObjectiveUpdateEvent> {
            if (it.objective != layout.objective) {
                return@of
            }
            layout.updateName()
        })
        connection.registerEvent(CallbackEventInvoker.of<ScoreboardScoreRemoveEvent> {
            if (it.score.objective != layout.objective) {
                return@of
            }
            layout.removeScore(it.score)
        })
        connection.registerEvent(CallbackEventInvoker.of<ScoreboardScorePutEvent> {
            if (it.score.objective != layout.objective) {
                return@of
            }
            layout.updateScore(it.score)
        })
        connection.registerEvent(CallbackEventInvoker.of<ScoreTeamChangeEvent> {
            if (it.score.objective != layout.objective) {
                return@of
            }
            layout.updateScore(it.score)
        })
        connection.registerEvent(CallbackEventInvoker.of<TeamUpdateEvent> {
            val objective = layout.objective ?: return@of
            for ((_, score) in objective.scores) {
                if (it.team != score.team) {
                    continue
                }
                layout.updateScore(score)
            }
        })
    }

    override fun draw() {
        // check if content was changed, and we need to re-prepare before drawing
        if (!layout.cacheUpToDate) {
            layout.recalculateSize()
        }
    }

    companion object : HUDBuilder<ScoreboardHUDElement> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:scoreboard".toResourceLocation()

        override fun build(hudRenderer: HUDRenderer): ScoreboardHUDElement {
            return ScoreboardHUDElement(hudRenderer)
        }
    }
}
