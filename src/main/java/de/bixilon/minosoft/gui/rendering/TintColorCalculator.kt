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

package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.textures.Texture
import glm_.vec3.Vec3i

class TintColorCalculator(val world: World) {
    val colorMaps: MutableMap<ResourceLocation, Array<RGBColor>> = mutableMapOf()

    fun init(assetsManager: AssetsManager) {
        colorMaps[DEFAULT_GRASS_COLOR_MAP] = assetsManager.readPixelArrayAsset(Texture.getResourceTextureIdentifier(textureName = "colormap/grass.png"))
        colorMaps[DEFAULT_FOLIAGE_COLOR_MAP] = assetsManager.readPixelArrayAsset(Texture.getResourceTextureIdentifier(textureName = "colormap/foliage.png"))
    }

    fun getAverageTint(biome: Biome?, blockState: BlockState, blockPosition: Vec3i): RGBColor? {
        val biomeBlendRadius = Minosoft.config.config.game.graphics.biomeBlendRadius
        val selfTint = getTint(biome, blockState, blockPosition)

        if (selfTint == null || biomeBlendRadius == 0) {
            return selfTint
        }

        val blendStart = Vec3i(blockPosition.x - biomeBlendRadius, blockPosition.y, blockPosition.z - biomeBlendRadius)
        val blendEnd = Vec3i(blockPosition.x + biomeBlendRadius, blockPosition.y + 1, blockPosition.z + biomeBlendRadius)


        var totalRed = 0L
        var totalGreen = 0L
        var totalBlue = 0L
        var count = 0


        for (z in blendStart.z until blendEnd.z) {
            for (x in blendStart.x until blendEnd.x) {
                val blendBlockPosition = Vec3i(x, blockPosition.y, z)
                getTint(world.getBiome(blendBlockPosition), blockState, blendBlockPosition)?.let {
                    totalRed += it.red
                    totalGreen += it.green
                    totalBlue += it.blue

                    count++
                }
            }
        }

        if ((totalRed == 0L && totalGreen == 0L && totalBlue == 0L) || count == 0) {
            return null
        }

        return RGBColor((totalRed / count).toInt(), (totalGreen / count).toInt(), (totalBlue / count).toInt())
    }

    fun getTint(biome: Biome?, blockState: BlockState, blockPosition: Vec3i): RGBColor? {
        return when {
            biome == null -> null
            StaticConfiguration.BIOME_DEBUG_MODE -> RGBColor(biome.hashCode())
            blockState.tintColor != null -> blockState.tintColor
            blockState.block.tint != null -> calculateTint(blockState.block.tint!!, biome, blockPosition)
            else -> null
        }
    }

    private fun calculateTint(tint: ResourceLocation, biome: Biome, blockPosition: Vec3i): RGBColor? {
        return TINTS[tint]?.invoke(biome, blockPosition, colorMaps)
    }


    companion object {
        val TINTS: MutableMap<ResourceLocation, (biome: Biome, blockPosition: Vec3i, colorMaps: Map<ResourceLocation, Array<RGBColor>>) -> RGBColor?> = mutableMapOf()
        private val DEFAULT_GRASS_COLOR_MAP = ResourceLocation("minecraft:grass_colormap")
        private val DEFAULT_FOLIAGE_COLOR_MAP = ResourceLocation("minecraft:foliage_colormap")

        private val WATER_TINT_RESOURCE_LOCATION = ResourceLocation("water_tint")
        private val GRASS_TINT_RESOURCE_LOCATION = ResourceLocation("grass_tint")
        private val SUGAR_CANE_TINT_RESOURCE_LOCATION = ResourceLocation("sugar_cane_tint")
        private val SHEARING_DOUBLE_PLANT_TINT_RESOURCE_LOCATION = ResourceLocation("shearing_double_plant_tint")
        private val FOLIAGE_TINT_RESOURCE_LOCATION = ResourceLocation("foliage_tint")
        private val LILY_PAD_TINT_RESOURCE_LOCATION = ResourceLocation("lily_pad_tint")

        init {
            TINTS[WATER_TINT_RESOURCE_LOCATION] = { biome, _, _ ->
                biome.waterColor
            }
            val grassTintCalculator = tint@{ biome: Biome, _: Vec3i, colorMaps: Map<ResourceLocation, Array<RGBColor>> ->
                biome.grassColorOverride?.let { return@tint it }
                val grassColorMap = colorMaps[DEFAULT_GRASS_COLOR_MAP]!!

                val colorMapPixelIndex = biome.downfallColorMapCoordinate shl 8 or biome.temperatureColorMapCoordinate
                var color = if (colorMapPixelIndex > grassColorMap.size) {
                    RenderConstants.GRASS_OUT_OF_BOUNDS_COLOR
                } else {
                    grassColorMap[colorMapPixelIndex]
                }
                if (color == RenderConstants.WHITE_COLOR) {
                    color = RenderConstants.GRASS_FAILOVER_COLOR
                }
                biome.grassColorModifier.modifier(color)
            }
            TINTS[GRASS_TINT_RESOURCE_LOCATION] = grassTintCalculator
            TINTS[SUGAR_CANE_TINT_RESOURCE_LOCATION] = grassTintCalculator
            TINTS[SHEARING_DOUBLE_PLANT_TINT_RESOURCE_LOCATION] = grassTintCalculator


            TINTS[FOLIAGE_TINT_RESOURCE_LOCATION] = tint@{ biome, blockPosition, colorMaps ->
                biome.foliageColorOverride?.let { return@tint it }
                val foliageColorMap = colorMaps[DEFAULT_FOLIAGE_COLOR_MAP]!!

                foliageColorMap[biome.downfallColorMapCoordinate shl 8 or biome.getClampedTemperature(blockPosition.y)]
            }

            TINTS[LILY_PAD_TINT_RESOURCE_LOCATION] = { _, _, _ -> RenderConstants.LILY_PAD_BLOCK_COLOR }
        }


        fun getJsonColor(color: Int): RGBColor? {
            if (color == 0) {
                return null
            }
            return color.asRGBColor()
        }
    }
}
