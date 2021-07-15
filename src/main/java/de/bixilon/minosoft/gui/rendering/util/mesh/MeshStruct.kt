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

package de.bixilon.minosoft.gui.rendering.util.mesh

import de.bixilon.minosoft.data.text.RGBColor
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

abstract class MeshStruct(val struct: KClass<*>) {
    val BYTES_PER_VERTEX: Int = calculateBytesPerVertex(struct)
    val FLOATS_PER_VERTEX: Int = BYTES_PER_VERTEX / Float.SIZE_BYTES
    val attributes: List<MeshAttribute>

    init {
        val attributes: MutableList<MeshAttribute> = mutableListOf()
        var stride = 0L

        for ((index, parameter) in struct.primaryConstructor!!.parameters.withIndex()) {
            val bytes = parameter.BYTES
            attributes += MeshAttribute(index, bytes / Float.SIZE_BYTES, stride)
            stride += bytes
        }

        this.attributes = attributes.toList()
    }

    companion object {

        fun calculateBytesPerVertex(clazz: KClass<*>): Int {
            var bytes = 0

            for (type in clazz.primaryConstructor!!.parameters) {
                bytes += type.BYTES
            }
            return bytes
        }

        val KParameter.BYTES: Int
            get() = (this.type.classifier as KClass<*>).BYTES

        val KClass<*>.BYTES: Int
            get() {
                return when (this) {
                    Mat4::class -> 4 * 4 * Float.SIZE_BYTES
                    Vec3d::class -> 3 * Double.SIZE_BYTES
                    Vec3::class -> 3 * Float.SIZE_BYTES
                    Vec3i::class -> 2 * Int.SIZE_BYTES
                    Vec2d::class -> 2 * Double.SIZE_BYTES
                    Vec2::class -> 2 * Float.SIZE_BYTES
                    Vec2i::class -> 2 * Int.SIZE_BYTES
                    Float::class -> Float.SIZE_BYTES
                    Int::class -> Int.SIZE_BYTES
                    RGBColor::class -> Int.SIZE_BYTES
                    else -> TODO("Can not find $this")
                }
            }
    }
}
