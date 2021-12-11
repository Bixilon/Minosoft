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

package de.bixilon.minosoft.gui.rendering.gui.hud.atlas

import de.bixilon.minosoft.assets.util.FileUtil.readJsonObject
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.toVec2i
import de.bixilon.minosoft.util.KUtil.mapCast
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class HUDAtlasManager(private val hudRenderer: HUDRenderer) {
    private lateinit var elements: Map<ResourceLocation, HUDAtlasElement>

    fun init() {
        val data = hudRenderer.connection.assetsManager[ATLAS_DATA].readJsonObject()
        val versionId = hudRenderer.connection.version.versionId

        val elements: MutableMap<ResourceLocation, HUDAtlasElement> = mutableMapOf()

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
            val versionData = versions[versionToUse.toString()]?.mapCast()!!

            val texture = hudRenderer.renderWindow.textureManager.staticTextures.createTexture(versionData["texture"].toResourceLocation())
            val start = versionData["start"].toVec2i()
            val end = versionData["end"].toVec2i()
            val slots: MutableMap<Int, Vec2iBinding> = mutableMapOf()

            versionData["slots"]?.mapCast()?.let {
                for ((slotId, slotData) in it) {
                    val slot = slotData.mapCast()!!
                    slots[slotId.toInt()] = Vec2iBinding(
                        start = slot["start"].toVec2i(),
                        end = slot["end"].toVec2i(),
                    )
                }
            }

            val atlasElement = HUDAtlasElement(
                texture = texture,
                start = start,
                end = end,
                slots = slots,
            )

            elements[resourceLocation] = atlasElement
        }

        this.elements = elements
    }

    fun postInit() {
        for (element in elements.values) {
            element.uvStart = element.texture.singlePixelSize * element.start
            element.uvEnd = element.texture.singlePixelSize * element.end
        }
    }

    operator fun get(resourceLocation: ResourceLocation): HUDAtlasElement? {
        return elements[resourceLocation]
    }

    operator fun get(resourceLocation: String): HUDAtlasElement? {
        return elements[resourceLocation.toResourceLocation()]
    }

    companion object {
        private val ATLAS_DATA = "minosoft:mapping/atlas.json".toResourceLocation()
    }
}
