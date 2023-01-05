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

package de.bixilon.minosoft.gui.rendering.sky.planet

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.hash.HashUtil.murmur64
import de.bixilon.kutil.random.RandomUtil.nextFloat
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.world.time.DayPhases
import de.bixilon.minosoft.data.world.time.MoonPhases
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import java.util.*

class MoonRenderer(
    sky: SkyRenderer,
) : PlanetRenderer(sky) {
    override val texture = sky.context.textureManager.staticTextures.createTexture(MOON_PHASES)
    private var phase = MoonPhases.FULL_MOON

    private fun updateUV(phases: MoonPhases) {
        val coordinates = PHASE_UV[phases.ordinal]
        uvStart = Vec2(1.0f / 4 * coordinates.x, 1.0f / 2 * coordinates.y) * texture.textureArrayUV
        uvEnd = Vec2(1.0f / 4 * (coordinates.x + 1), 1.0f / 2 * (coordinates.y + 1)) * texture.textureArrayUV
        meshInvalid = true
    }

    override fun postInit() {
        super.postInit()
        updateUV(MoonPhases.FULL_MOON)
    }

    override fun calculateAngle(): Float {
        val time = sky.context.connection.world.time

        return ((time.time / ProtocolDefinition.TICKS_PER_DAYf) - 0.75f) * 360.0f
    }

    override fun calculateIntensity(): Float {
        val time = sky.context.connection.world.time
        return when (time.phase) {
            DayPhases.NIGHT -> 1.0f
            DayPhases.DAY -> 0.0f
            DayPhases.SUNSET -> time.progress
            DayPhases.SUNRISE -> 1.0f - time.progress
        }
    }

    override fun onTimeUpdate(time: WorldTime) {
        super.onTimeUpdate(time)
        val phase = time.moonPhase
        if (phase != this.phase) {
            this.phase = phase
            updateUV(phase)
            meshInvalid = true
        }
    }

    override fun calculateModifier(day: Long): Float {
        return Random((day / MoonPhases.VALUES.size).murmur64()).nextFloat(0.0f, 0.2f)
    }

    override fun draw() {
        if (!sky.effects.moon) {
            return
        }
        super.draw()
    }

    companion object {
        private val MOON_PHASES = minecraft("environment/moon_phases").texture()

        private val PHASE_UV = arrayOf(
            Vec2i(0, 0), // FULL_MOON
            Vec2i(3, 1), // WANING_GIBBOUS
            Vec2i(2, 1), // LAST_QUARTER
            Vec2i(1, 1), // WANING_CRESCENT
            Vec2i(0, 1), // NEW_MOON
            Vec2i(3, 0), // WAXING_CRESCENT
            Vec2i(2, 0), // FIRST_QUARTER
            Vec2i(1, 0), // WAXING_GIBBOUS
        )
    }
}
