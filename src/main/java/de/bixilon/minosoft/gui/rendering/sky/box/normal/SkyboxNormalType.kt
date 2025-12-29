/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.sky.box.normal

import de.bixilon.kmath.mat.mat4.f.MMat4f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.primitive.FloatUtil.rad
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.sky.box.SkyboxType
import de.bixilon.minosoft.gui.rendering.sky.box.color.SkyboxColorShader
import de.bixilon.minosoft.gui.rendering.sky.box.color.SkyboxMeshBuilder

class SkyboxNormalType(val sky: SkyRenderer) : SkyboxType {
    private val shader = sky.context.system.shader.create(minosoft("sky/skybox/normal")) { SkyboxNormalShader(it) }
    private val mesh = SkyboxMeshBuilder(sky.context).bake()
    private var updateMatrix = true


    override fun postInit() {
        shader.load()
        mesh.load()
        sky::matrix.observe(this) { updateMatrix = true }
    }


    private fun calculateSunPosition(): Vec3f {
        val matrix = MMat4f().apply {
            rotateZAssign((sky.sun.calculateAngle() + 90.0f).rad)
        }

        val barePosition = Vec3f(1.0f, 0.0f, 0.0f)

        return (matrix * barePosition).unsafe
    }


    override fun draw() {
        shader.use()

        shader.skyColor = sky.box.color.color.rgba()
        shader.sunPosition = calculateSunPosition()

        val weather = sky.session.world.weather
        shader.rain = weather.rain
        shader.thunder = weather.thunder

        shader.lightning = sky.box.color.lightning

        // TODO: lightning, rain, thunder

        if (updateMatrix) {
            shader.skyViewProjectionMatrix = sky.matrix
            updateMatrix = false
        }

        mesh.draw()
    }

}
