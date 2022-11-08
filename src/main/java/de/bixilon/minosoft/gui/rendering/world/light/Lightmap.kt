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

import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatch
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.world.light.updater.DebugLightUpdater
import de.bixilon.minosoft.gui.rendering.world.light.updater.FullbrightLightUpdater
import de.bixilon.minosoft.gui.rendering.world.light.updater.LegacyLightmapUpdater
import de.bixilon.minosoft.gui.rendering.world.light.updater.LightmapUpdater

class Lightmap(private val light: RenderLight) {
    private val profile = light.renderWindow.connection.profiles.rendering
    private val buffer = LightmapBuffer(light.renderWindow.renderSystem)
    private var updater: LightmapUpdater = FullbrightLightUpdater
        set(value) {
            field = value
            force = true
        }
    private var force: Boolean = true

    private val defaultUpdater: LightmapUpdater = LegacyLightmapUpdater(light.renderWindow.connection)

    fun init() {
        buffer.init()
        profile.light::fullbright.profileWatch(this, profile = profile) { setLightmapUpdater() }
        setLightmapUpdater()
    }

    private fun setLightmapUpdater() {
        this.updater = getLightmapUpdater()
    }

    private fun getLightmapUpdater(): LightmapUpdater {
        if (StaticConfiguration.LIGHT_DEBUG_MODE) {
            return DebugLightUpdater
        }
        if (profile.light.fullbright) {
            return FullbrightLightUpdater
        }
        return defaultUpdater
    }

    fun use(shader: Shader, bufferName: String = "uLightMapBuffer") {
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
