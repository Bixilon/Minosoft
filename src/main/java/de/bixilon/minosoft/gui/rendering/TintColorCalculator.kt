/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.data.assets.MinecraftAssetsManager
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.biomes.Biome
import de.bixilon.minosoft.data.text.RGBColor

import de.bixilon.minosoft.gui.rendering.textures.Texture
import glm_.vec3.Vec3i

class TintColorCalculator {
    private lateinit var grassColorMap: Array<RGBColor>
    private lateinit var foliageColorMap: Array<RGBColor>

    fun init(assetsManager: MinecraftAssetsManager) {
        grassColorMap = assetsManager.readPixelArrayAsset(Texture.getResourceTextureIdentifier(textureName = "colormap/grass.png"))
        foliageColorMap = assetsManager.readPixelArrayAsset(Texture.getResourceTextureIdentifier(textureName = "colormap/foliage.png"))
    }

    fun calculateTint(tint: ResourceLocation, biome: Biome, blockPosition: Vec3i): RGBColor? {
        return when (tint) {
            ResourceLocation("water_tint") -> biome.waterColor
            ResourceLocation("grass_tint"), ResourceLocation("sugar_cane_tint"), ResourceLocation("shearing_double_plant_tint") -> {
                val colorMapPixelIndex = biome.downfallColorMapCoordinate shl 8 or biome.temperatureColorMapCoordinate
                var color = if (colorMapPixelIndex > grassColorMap.size) {
                    RenderConstants.GRASS_OUT_OF_BOUNDS_COLOR
                } else {
                    grassColorMap[colorMapPixelIndex]
                }
                if (color == RenderConstants.WHITE_COLOR) {
                    color = RenderConstants.GRASS_FAILOVER_COLOR
                }
                biome.grassColorModifier.modifier.invoke(color)
            }
            ResourceLocation("foliage_tint") -> {
                foliageColorMap[biome.downfallColorMapCoordinate shl 8 or biome.getClampedTemperature(blockPosition.y)] // ToDo: hardcoded color values
            }
            ResourceLocation("lily_pad_tint") -> RenderConstants.LILY_PAD_BLOCK_COLOR
            else -> null
        }
    }


    companion object {
        fun getJsonColor(color: Int): RGBColor? {
            if (color == 0) {
                return null
            }
            return RGBColor.noAlpha(color)
        }
    }
}
