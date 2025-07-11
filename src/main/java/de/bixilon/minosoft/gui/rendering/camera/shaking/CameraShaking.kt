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

package de.bixilon.minosoft.gui.rendering.camera.shaking

import glm_.mat4x4.Mat4
import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import glm_.vec3.swizzle.xz
import de.bixilon.kutil.avg._float.FloatAverage
import de.bixilon.kutil.math.Trigonometry.sin
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.config.profile.profiles.rendering.camera.shaking.ShakingC
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.Z
import de.bixilon.minosoft.protocol.network.session.play.tick.Ticks.Companion.ticks
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import kotlin.time.Duration.Companion.milliseconds

class CameraShaking(
    private val camera: Camera,
    private val profile: ShakingC,
) : Drawable {
    private var rotation = 0.0f
    private var strength = FloatAverage(5.ticks.duration, 1.0f)
    private val speed = FloatAverage(5.ticks.duration, 0.0f)

    val isEmpty: Boolean get() = rotation == 0.0f

    override fun draw() {
        this.strength += 1.0f
        val strength = this.strength.avg * profile.amplifier // strength affects how far it goes

        val physics = camera.context.session.camera.entity.physics
        val velocity = physics.velocity.xz.length2().toFloat() // velocity affects how quick it goes
        if (velocity > 0.003 && physics.onGround) {
            this.speed += velocity
        } else {
            this.speed += 0.0f // TODO: remove this, kutil 1.21
        }
        val time = (millis() % 100L).toFloat() / 100.0f

        this.rotation = sin(time * minOf(this.speed.avg, 0.5f) / 3.0f) * strength * 0.03f
    }

    fun onDamage() {
        strength += 1000.0f
        speed += 0.05f
    }

    fun transform(): Mat4? {
        if (rotation == 0.0f) return null
        return Mat4()
            .rotateAssign(rotation, Vec3f.Z)
    }
}
