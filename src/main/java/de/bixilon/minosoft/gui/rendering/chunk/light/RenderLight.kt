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

package de.bixilon.minosoft.gui.rendering.chunk.light

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.minosoft.config.DebugOptions
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.RenderingStates
import de.bixilon.minosoft.gui.rendering.chunk.light.debug.LightmapDebugWindow
import de.bixilon.minosoft.util.KUtil.format
import de.bixilon.minosoft.util.delegate.JavaFXDelegate.observeFX

class RenderLight(val context: RenderContext) {
    private val connection = context.connection
    val map = Lightmap(this)
    private val debugWindow = if (DebugOptions.LIGHTMAP_DEBUG_WINDOW) LightmapDebugWindow(map) else null

    fun init() {
        map.init()

        context.input.bindings.register(RECALCULATE, KeyBinding(
            KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4),
            KeyActions.PRESS to setOf(KeyCodes.KEY_A),
        )
        ) {
            DefaultThreadPool += {
                connection.world.recalculateLight()
                connection.util.sendDebugMessage("Light recalculated and chunk cache cleared!")
            }
        }
        context.input.bindings.register(
            FULLBRIGHT,
            KeyBinding(
                KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4),
                KeyActions.STICKY to setOf(KeyCodes.KEY_C),
            ),
            pressed = connection.profiles.rendering.light.fullbright,
        ) {
            connection.profiles.rendering.light.fullbright = it
            connection.util.sendDebugMessage("Fullbright: ${it.format()}")
        }
        if (DebugOptions.LIGHTMAP_DEBUG_WINDOW) {
            context::state.observeFX(this) {
                if (it == RenderingStates.RUNNING) {
                    JavaFXUtil.runLater { debugWindow?.show() }
                } else if (it == RenderingStates.QUITTING) {
                    debugWindow?.close()
                }
            }
        }
    }

    fun updateAsync() {
        map.updateAsync()
    }

    fun update() {
        map.update()
        debugWindow?.update()
    }

    private companion object {
        val RECALCULATE = minosoft("recalculate_light")
        val FULLBRIGHT = minosoft("fullbright")
    }
}
