/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
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

import de.bixilon.kutil.hash.HashUtil.murmur64
import de.bixilon.kutil.random.RandomUtil.nextFloat
import de.bixilon.minosoft.data.entities.EntityRotation.Companion.CIRCLE_DEGREE
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.world.time.DayPhases
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import java.util.*
import kotlin.math.pow

class SunRenderer(
    sky: SkyRenderer,
) : PlanetRenderer(sky) {
    override val texture = sky.context.textures.static.create(SUN)

    public override fun calculateAngle(): Float {
        val time = sky.context.session.world.time

        // 270: sunrise (23k-0k)
        // 0: day (0-12k)
        // 90: sunset (12k-13k)
        // 180: night (13k-23k)


        return ((time.time / WorldTime.TICKS_PER_DAYf) - 0.25f) * CIRCLE_DEGREE
    }

    override fun calculateIntensity(): Float {
        val time = sky.context.session.world.time
        return when (time.phase) {
            DayPhases.NIGHT -> 0.0f
            DayPhases.DAY -> 1.0f
            DayPhases.SUNSET -> (1.0f - time.progress).pow(2)
            DayPhases.SUNRISE -> time.progress.pow(2)
        }
    }

    override fun calculateModifier(day: Long): Float {
        return Random(day.murmur64()).nextFloat(0.0f, 0.2f)
    }

    override fun draw() {
        if (!sky.effects.sun) {
            return
        }
        super.draw()
    }

    companion object {
        private val SUN = minecraft("environment/sun").texture()
    }
}
