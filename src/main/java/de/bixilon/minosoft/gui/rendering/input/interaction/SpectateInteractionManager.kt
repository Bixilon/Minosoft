package de.bixilon.minosoft.gui.rendering.input.interaction

import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.other.game.event.handlers.gamemode.GamemodeChangeEvent
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.modding.event.events.CameraSetEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.RateLimiter
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class SpectateInteractionManager(
    private val renderWindow: RenderWindow,
) {
    private val connection = renderWindow.connection
    private val rateLimiter = RateLimiter()

    fun init() {
        renderWindow.inputHandler.registerKeyCallback(STOP_SPECTATING, KeyBinding(
            mapOf(
                KeyAction.PRESS to setOf(KeyCodes.KEY_LEFT_SHIFT),
            ),
        )) { spectate(null) }

        renderWindow.connection.registerEvent(CallbackEventInvoker.of<GamemodeChangeEvent> {
            spectate(null)
        })
        renderWindow.connection.registerEvent(CallbackEventInvoker.of<CameraSetEvent> {
            spectate(it.entity)
        })
    }


    fun spectate(entity: Entity?) {
        var entity = entity ?: connection.player
        if (connection.player.gamemode != Gamemodes.SPECTATOR) {
            entity = connection.player
        }
        renderWindow.camera.matrixHandler.entity = entity
    }

    fun draw(delta: Double) {
        rateLimiter.work()
    }

    companion object {
        private val STOP_SPECTATING = "minosoft:stop_spectating".toResourceLocation()
    }
}
