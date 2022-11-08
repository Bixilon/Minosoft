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

package de.bixilon.minosoft.gui.rendering.world.light

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.world.WorldRenderer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class RenderLight(val renderWindow: RenderWindow) {
    private val connection = renderWindow.connection
    val map = Lightmap(this)

    fun init() {
        map.init()

        renderWindow.inputHandler.registerKeyCallback(
            "minosoft:recalculate_light".toResourceLocation(),
            KeyBinding(
                KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4),
                KeyActions.PRESS to setOf(KeyCodes.KEY_A),
            )
        ) {
            DefaultThreadPool += {
                connection.world.recalculateLight()
                renderWindow.renderer[WorldRenderer]?.silentlyClearChunkCache()
                connection.util.sendDebugMessage("Light recalculated and chunk cache cleared!")
            }
        }
    }

    fun update() {
        map.update()
    }
}
