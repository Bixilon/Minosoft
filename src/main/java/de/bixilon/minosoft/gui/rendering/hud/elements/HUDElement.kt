/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud.elements

import de.bixilon.minosoft.gui.rendering.hud.HUDElementProperties
import de.bixilon.minosoft.gui.rendering.hud.HUDMesh
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.atlas.HUDAtlasElement
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

abstract class HUDElement(protected val hudRenderer: HUDRenderer) {
    abstract val elementProperties: HUDElementProperties

    abstract fun init()
    open fun postInit() {}
    abstract fun prepare(hudMesh: HUDMesh)
    open fun update() {}
    open fun draw() {}
    open fun screenChangeResizeCallback(screenWidth: Int, screenHeight: Int) {}


    fun getRealPosition(elementSize: Vec2, elementProperties: HUDElementProperties, realType: RealTypes, innerOffset: Vec2 = Vec2()): Vec2 {
        // ToDo: Improve this code
        val halfElementSize = elementSize / 2f
        val realPosition = elementProperties.position * hudRenderer.renderWindow.screenDimensions

        fun getValue(elementSize: Float, halfSize: Float, position: Float, binding: HUDElementProperties.PositionBindings, innerOffset: Float): Float {
            val ourHalfSize = if (realType == RealTypes.START) {
                -halfSize
            } else {
                halfSize
            }
            var value = when (binding) {
                HUDElementProperties.PositionBindings.CENTER -> {
                    when {
                        position == 0f -> {
                            ourHalfSize
                        }
                        position < 0f -> {
                            position + ourHalfSize
                        }
                        else -> {
                            position - ourHalfSize
                        }
                    }
                }
                HUDElementProperties.PositionBindings.FAHRTEST_POINT_AWAY -> {
                    if (position == 0f) {
                        0f
                    } else {
                        when (realType) {
                            RealTypes.START -> {
                                if (position < 0f) {
                                    position
                                } else {
                                    position - (elementSize)
                                }
                            }
                            RealTypes.END -> {
                                if (position < 0f) {
                                    position + (elementSize)
                                } else {
                                    position
                                }
                            }
                        }
                    }
                }
            }

            value += innerOffset
            return value
        }

        return Vec2(getValue(elementSize.x, halfElementSize.x, realPosition.x, elementProperties.xBinding, innerOffset.x), getValue(elementSize.y, halfElementSize.y, realPosition.y, elementProperties.yBinding, innerOffset.y))
    }


    fun drawImage(start: Vec2, end: Vec2, hudMesh: HUDMesh, hudAtlasElement: HUDAtlasElement, z: Int = 1) {
        val textureStart = Vec2((hudAtlasElement.binding.start.x * hudAtlasElement.texture.widthFactor) / hudAtlasElement.texture.width.toFloat(), (hudAtlasElement.binding.start.y * hudAtlasElement.texture.heightFactor) / hudAtlasElement.texture.height.toFloat())
        val textureEnd = Vec2(((hudAtlasElement.binding.end.x + 1) * hudAtlasElement.texture.widthFactor) / (hudAtlasElement.texture.width + 1f), ((hudAtlasElement.binding.end.y + 1) * hudAtlasElement.texture.heightFactor) / (hudAtlasElement.texture.height + 1f))

        drawImage(start, end, hudMesh, hudAtlasElement, textureStart, textureEnd, z)
    }

    fun drawImage(start: Vec2, end: Vec2, hudMesh: HUDMesh, hudAtlasElement: HUDAtlasElement, textureStart: Vec2, textureEnd: Vec2, z: Int = 1) {
        val modelStart = hudRenderer.orthographicMatrix * Vec4(start, 1f, 1.0f)
        val modelEnd = hudRenderer.orthographicMatrix * Vec4(end, 1f, 1.0f)


        val realZ = HUD_Z_COORDINATE + HUD_Z_COORDINATE_Z_FACTOR * z


        hudMesh.addVertex(Vec3(modelStart.x, modelStart.y, realZ), Vec2(textureStart.x, textureEnd.y), hudAtlasElement.texture.id)
        hudMesh.addVertex(Vec3(modelEnd.x, modelStart.y, realZ), Vec2(textureEnd.x, textureEnd.y), hudAtlasElement.texture.id)
        hudMesh.addVertex(Vec3(modelStart.x, modelEnd.y, realZ), Vec2(textureStart.x, textureStart.y), hudAtlasElement.texture.id)
        hudMesh.addVertex(Vec3(modelStart.x, modelEnd.y, realZ), Vec2(textureStart.x, textureStart.y), hudAtlasElement.texture.id)
        hudMesh.addVertex(Vec3(modelEnd.x, modelStart.y, realZ), Vec2(textureEnd.x, textureEnd.y), hudAtlasElement.texture.id)
        hudMesh.addVertex(Vec3(modelEnd.x, modelEnd.y, realZ), Vec2(textureEnd.x, textureStart.y), hudAtlasElement.texture.id)
    }


    enum class RealTypes {
        START,
        END
    }

    companion object {
        private const val HUD_Z_COORDINATE = -0.9996f
        private const val HUD_Z_COORDINATE_Z_FACTOR = -0.000001f
    }
}
