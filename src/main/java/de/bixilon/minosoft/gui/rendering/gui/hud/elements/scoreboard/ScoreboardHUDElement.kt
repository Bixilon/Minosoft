package de.bixilon.minosoft.gui.rendering.gui.hud.elements.scoreboard

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.scoreboard.ScoreboardPositions
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.LayoutedHUDElement
import de.bixilon.minosoft.modding.event.events.scoreboard.*
import de.bixilon.minosoft.modding.event.events.scoreboard.team.TeamUpdateEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i

class ScoreboardHUDElement(hudRenderer: HUDRenderer) : LayoutedHUDElement<ScoreboardSideElement>(hudRenderer) {
    private val connection = hudRenderer.connection
    override val layout = ScoreboardSideElement(hudRenderer)

    override val layoutOffset: Vec2i
        get() = Vec2i(hudRenderer.scaledSize.x - layout.size.x, (hudRenderer.scaledSize.y - layout.size.y) / 2)

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
            val objective = layout.objective ?: return@of
            layout.updateScore(it.score)
        })
        connection.registerEvent(CallbackEventInvoker.of<TeamUpdateEvent> {
            val objective = layout.objective ?: return@of
            for ((_, score) in objective.scores) {
                if (it.team !in score.teams) {
                    continue
                }
                layout.updateScore(score)
            }
        })
    }

    companion object : HUDBuilder<ScoreboardHUDElement> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:scoreboard".toResourceLocation()

        override fun build(hudRenderer: HUDRenderer): ScoreboardHUDElement {
            return ScoreboardHUDElement(hudRenderer)
        }
    }
}
