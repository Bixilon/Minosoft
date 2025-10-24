/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.vec.vec2.d.Vec2d
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec3.i.Vec3i
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.PackedUV
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.UnpackedUV
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

abstract class MeshStruct(struct: KClass<*>) {
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

        this.attributes = attributes
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
            get() = when (this) {
                Mat4f::class -> Mat4f.LENGTH * Float.SIZE_BYTES

                Vec3d::class -> Vec3d.LENGTH * Double.SIZE_BYTES
                Vec3f::class -> Vec3f.LENGTH * Float.SIZE_BYTES
                Vec3i::class -> Vec3i.LENGTH * Int.SIZE_BYTES

                Vec2d::class -> Vec2d.LENGTH * Double.SIZE_BYTES
                Vec2f::class -> Vec2f.LENGTH * Float.SIZE_BYTES
                Vec2i::class -> Vec2i.LENGTH * Int.SIZE_BYTES

                Float::class -> Float.SIZE_BYTES
                Int::class -> Int.SIZE_BYTES

                UnpackedUV::class -> 2 * Float.SIZE_BYTES
                PackedUV::class -> Float.SIZE_BYTES

                RGBColor::class -> Int.SIZE_BYTES
                else -> TODO("Can not find $this")
            }
    }
}
