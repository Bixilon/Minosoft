/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.config.game.graphics

import com.squareup.moshi.Json
import de.bixilon.minosoft.config.config.game.particles.ParticleConfig

data class GraphicsGameConfig(
    var animations: AnimationsGameConfig = AnimationsGameConfig(),
    var particles: ParticleConfig = ParticleConfig(),
    @Json(name = "biome_blend_radius") var biomeBlendRadius: Int = 3,
    @Json(name = "fast_biome_noise") var fastBiomeNoise: Boolean = false,
    @Json(name = "fog_enabled") var fogEnabled: Boolean = true,
)
