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

package de.bixilon.minosoft.gui.rendering.gui.atlas

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJsonObject
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.toVec2
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.toVec2i
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class AtlasManager(private val context: RenderContext) {
    private lateinit var elements: Map<ResourceLocation, AtlasElement>
    var initialized = false
        private set

    @Synchronized
    fun init() {
        check(!initialized)
        val data = context.connection.assetsManager[ATLAS_DATA].readJsonObject()
        val versionId = context.connection.version.versionId

        val elements: MutableMap<ResourceLocation, AtlasElement> = mutableMapOf()

        for ((resourceLocationString, versions) in data) {
            val resourceLocation = resourceLocationString.toResourceLocation()
            check(versions is Map<*, *>)

            var versionToUse = -1

            for (idString in versions.keys) {
                val id = idString.toInt()
                if (id in (versionToUse + 1)..versionId) {
                    versionToUse = id
                    break
                }
            }
            if (versionToUse == -1) {
                // can not find version that matches our one
                continue
            }
            val versionData = versions[versionToUse.toString()].asJsonObject()

            val texture = context.textureManager.staticTextures.createTexture(versionData["texture"].toResourceLocation().texture(), mipmaps = false)
            val start = versionData["start"].toVec2i()
            val end = versionData["end"].toVec2i()
            val slots: Int2ObjectOpenHashMap<AtlasSlot> = Int2ObjectOpenHashMap()

            versionData["slots"].toJsonObject()?.let {
                for ((slotId, slotData) in it) {
                    val slot = slotData.asJsonObject()
                    slots[slotId.toInt()] = AtlasSlot(
                        start = slot["start"].toVec2(),
                        end = slot["end"].toVec2(),
                    )
                }
            }

            val areas: MutableMap<String, AtlasArea> = mutableMapOf()
            versionData["areas"].toJsonObject()?.let {
                for ((areaName, areaPosition) in it) {
                    val position = areaPosition.asJsonObject()
                    areas[areaName] = AtlasArea(
                        start = position["start"].toVec2(),
                        end = position["end"].toVec2(),
                    )
                }
            }

            val resolution = versionData["resolution"]?.toVec2i()

            val atlasElement = AtlasElement(
                texture = texture,
                start = start,
                resolution = resolution,
                end = end,
                slots = slots,
                areas = areas,
            )

            elements[resourceLocation] = atlasElement
        }

        this.elements = elements
        this.initialized = true
    }

    fun postInit() {
        for (element in elements.values) {
            val singePixelSize = element.resolution?.let { Vec2(1.0f) / it / (Vec2(element.texture.array.size) / it) } ?: ATLAS_SINGLE_DEFAULT_PIXEL_SIZE
            element.uvStart = singePixelSize * element.start
            element.uvEnd = singePixelSize * element.end
        }
    }

    operator fun get(resourceLocation: ResourceLocation): AtlasElement? {
        return elements[resourceLocation]
    }

    operator fun get(resourceLocation: String): AtlasElement? {
        return elements[resourceLocation.toResourceLocation()]
    }

    companion object {
        private val ATLAS_DATA = "minosoft:rendering/atlas.json".toResourceLocation()

        private val ATLAS_DEFAULT_SIZE = Vec2i(256, 256)
        private val ATLAS_SINGLE_DEFAULT_PIXEL_SIZE = Vec2(1.0f) / ATLAS_DEFAULT_SIZE
    }
}
