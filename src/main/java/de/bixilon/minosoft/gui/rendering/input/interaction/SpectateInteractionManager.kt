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

package de.bixilon.minosoft.gui.rendering.input.interaction

import de.bixilon.kutil.rate.RateLimiter
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.other.game.event.handlers.gamemode.GamemodeChangeEvent
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.modding.event.events.CameraSetEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class SpectateInteractionManager(
    private val renderWindow: RenderWindow,
) {
    private val connection = renderWindow.connection
    private val rateLimiter = RateLimiter()

    fun init() {
        renderWindow.inputHandler.registerKeyCallback(STOP_SPECTATING, KeyBinding(
            mapOf(
                KeyActions.PRESS to setOf(KeyCodes.KEY_LEFT_SHIFT),
            ),
        )) { spectate(null) }

        renderWindow.connection.registerEvent(CallbackEventInvoker.of<GamemodeChangeEvent> { spectate(null) })
        renderWindow.connection.registerEvent(CallbackEventInvoker.of<CameraSetEvent> { spectate(it.entity) })
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
