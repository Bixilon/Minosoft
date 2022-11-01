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

package de.bixilon.minosoft.gui.rendering.sky.box

import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.gui.rendering.sky.SkyChildRenderer
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.util.KUtil.minosoft
import java.util.*

class SkyboxRenderer(
    private val sky: SkyRenderer,
) : SkyChildRenderer {
    private val shader = sky.renderSystem.createShader(minosoft("sky/skybox"))
    private val mesh = SkyboxMesh(sky.renderWindow)
    private var updateColor = true
    private var updateMatrix = true
    private var color: RGBColor by watched(ChatColors.BLUE)

    init {
        sky::matrix.observe(this) { updateMatrix = true }
        this::color.observe(this) { updateColor = true }
    }

    override fun onTimeUpdate(time: WorldTime) {
        color = RGBColor(Random(time.time.toLong()).nextInt())
    }

    override fun init() {
        shader.load()
    }

    override fun postInit() {
        mesh.load()
    }

    private fun updateUniforms() {
        if (updateColor) {
            shader.setRGBColor("uSkyColor", color)
            updateColor = false
        }
        if (updateMatrix) {
            shader.setMat4("uSkyViewProjectionMatrix", sky.matrix)
            updateMatrix = false
        }
    }

    override fun draw() {
        shader.use()
        updateUniforms()

        mesh.draw()
    }
}
