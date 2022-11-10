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

package de.bixilon.minosoft.data.registries.dimension.effects

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.minecraft

object EndEffects : DimensionEffects {
    override val resourceLocation = minecraft("the_end")

    override val daylightCycle: Boolean get() = false
    override val skylight: Boolean get() = false
    override val fixedTexture: ResourceLocation = minecraft("environment/end_sky").texture()

    override val weather: Boolean get() = false
    override val sun: Boolean get() = false
    override val moon: Boolean get() = false
    override val stars: Boolean get() = false

    override val clouds: Boolean get() = false
    override fun getCloudHeight(connection: PlayConnection): IntRange = Broken()

    override val brighten = Vec3(0.99f, 1.12f, 1.0f) // bit more green

    override val fog: Boolean get() = false
}
