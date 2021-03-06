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

package de.bixilon.minosoft.gui.rendering.hud

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.hud.atlas.TextureLike
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class ElementMesh {
    private val elements: MutableList<ElementElement> = mutableListOf()
    val size: Vec2 = Vec2()
    var forceX: Int? = null
    var forceY: Int? = null

    fun addElement(start: Vec2 = Vec2(), end: Vec2, textureLike: TextureLike, z: Int = 1, tintColor: RGBColor? = null) {
        elements.add(ElementElement(start, end, textureLike, z, tintColor))

        checkSize(start)
        checkSize(end)
    }

    private fun checkSize(vec2: Vec2) {
        if (vec2.x > size.x) {
            size.x = vec2.x
        }
        if (vec2.y > size.y) {
            size.y = vec2.y
        }
    }


    fun addToMesh(hudElementProperties: HUDElementProperties, hudMatrix: Mat4, hudMesh: HUDMesh, hudScale: HUDScale, screenDimensions: Vec2) {
        if (!hudElementProperties.enabled) {
            return
        }

        val realScaleFactor = hudElementProperties.scale * hudScale.scale
        val realSize = Vec2(forceX ?: size.x, forceY ?: size.y) * realScaleFactor

        val elementStart = getRealPosition(realSize, hudElementProperties, screenDimensions)


        for (elementElement in elements) {
            val scaledStart = elementElement.start * realScaleFactor
            val scaledEnd = elementElement.end * realScaleFactor
            drawImage(addToStart(elementStart, scaledStart), addToEnd(elementStart, scaledEnd), hudMesh, elementElement.textureLike, hudMatrix, elementElement.z, elementElement.tintColor)
        }
    }


    private fun addToStart(start: Vec2, elementPosition: Vec2): Vec2 {
        return Vec2(start.x + elementPosition.x, start.y - elementPosition.y)
    }

    private fun addToEnd(start: Vec2, elementPosition: Vec2): Vec2 {
        return Vec2(start.x + elementPosition.x, start.y - elementPosition.y)
    }

    private fun getRealPosition(elementSize: Vec2, elementProperties: HUDElementProperties, screenDimensions: Vec2): Vec2 {
        val halfScreenDimensions = screenDimensions / 2
        val halfElementSize = elementSize / 2
        val realPosition = elementProperties.position * halfScreenDimensions

        var x = realPosition.x
        var y = realPosition.y
        if (elementProperties.xBinding == HUDElementProperties.PositionBindings.FURTHEST_POINT_AWAY) {
            if (elementProperties.position.x >= 0) {
                x -= elementSize.x
            }
        } else {
            x -= halfElementSize.x
        }

        if (elementProperties.yBinding == HUDElementProperties.PositionBindings.FURTHEST_POINT_AWAY) {
            if (elementProperties.position.y < 0) {
                y += elementSize.y
            }
        } else {
            y += halfElementSize.y
        }
        return Vec2(x, y)
    }

    private fun drawImage(start: Vec2, end: Vec2, hudMesh: HUDMesh, texture: TextureLike, matrix: Mat4, z: Int = 1, tintColor: RGBColor? = null) {
        val modelStart = matrix * Vec4(start, 1.0f, 1.0f)
        val modelEnd = matrix * Vec4(end, 1.0f, 1.0f)


        val realZ = HUD_Z_COORDINATE + HUD_Z_COORDINATE_Z_FACTOR * z


        hudMesh.addVertex(Vec3(modelStart.x, modelStart.y, realZ), Vec2(texture.uvStart.x, texture.uvStart.y), texture.texture.id, tintColor)
        hudMesh.addVertex(Vec3(modelEnd.x, modelStart.y, realZ), Vec2(texture.uvEnd.x, texture.uvStart.y), texture.texture.id, tintColor)
        hudMesh.addVertex(Vec3(modelStart.x, modelEnd.y, realZ), Vec2(texture.uvStart.x, texture.uvEnd.y), texture.texture.id, tintColor)
        hudMesh.addVertex(Vec3(modelStart.x, modelEnd.y, realZ), Vec2(texture.uvStart.x, texture.uvEnd.y), texture.texture.id, tintColor)
        hudMesh.addVertex(Vec3(modelEnd.x, modelStart.y, realZ), Vec2(texture.uvEnd.x, texture.uvStart.y), texture.texture.id, tintColor)
        hudMesh.addVertex(Vec3(modelEnd.x, modelEnd.y, realZ), Vec2(texture.uvEnd.x, texture.uvEnd.y), texture.texture.id, tintColor)
    }


    data class ElementElement(
        val start: Vec2,
        val end: Vec2,
        val textureLike: TextureLike,
        val z: Int,
        val tintColor: RGBColor?,
    )

    companion object {
        private const val HUD_Z_COORDINATE = -0.9996f
        private const val HUD_Z_COORDINATE_Z_FACTOR = -0.000001f
    }
}
