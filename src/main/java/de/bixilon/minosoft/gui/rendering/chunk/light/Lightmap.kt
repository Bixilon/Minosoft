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

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.config.DebugOptions
import de.bixilon.minosoft.gui.rendering.chunk.light.updater.DebugLightUpdater
import de.bixilon.minosoft.gui.rendering.chunk.light.updater.FullbrightLightUpdater
import de.bixilon.minosoft.gui.rendering.chunk.light.updater.LightmapUpdater
import de.bixilon.minosoft.gui.rendering.chunk.light.updater.normal.NormalLightmapUpdater
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader

class Lightmap(private val light: RenderLight) {
    private val profile = light.context.connection.profiles.rendering
    val buffer = LightmapBuffer(light.context.system)
    private var updater: LightmapUpdater = FullbrightLightUpdater
        set(value) {
            field = value
            force = true
        }
    private var force: Boolean = true

    private lateinit var defaultUpdater: LightmapUpdater

    fun init() {
        defaultUpdater = NormalLightmapUpdater(light.context.connection, light.context.renderer[SkyRenderer])
        // defaultUpdater = LegacyLightmapUpdater(light.context.connection)
        buffer.init()
        profile.light::fullbright.observe(this) { setLightmapUpdater() }
        setLightmapUpdater()
    }

    private fun setLightmapUpdater() {
        this.updater = getLightmapUpdater()
    }

    private fun getLightmapUpdater(): LightmapUpdater {
        if (DebugOptions.LIGHT_DEBUG_MODE) {
            return DebugLightUpdater
        }
        if (profile.light.fullbright) {
            return FullbrightLightUpdater
        }
        return defaultUpdater
    }

    fun use(shader: NativeShader, bufferName: String = "uLightMapBuffer") {
        buffer.use(shader, bufferName)
    }

    fun updateAsync() {
        updater.update(force, buffer)
        if (force) {
            force = false
        }
    }

    fun update() {
        buffer.upload()
    }
}
