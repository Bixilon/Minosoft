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

package de.bixilon.minosoft.gui.rendering.util.mesh.builder

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.primitive.floats.FloatList
import de.bixilon.kutil.collections.primitive.ints.IntList
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.VertexBuffer
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct
import de.bixilon.minosoft.util.collections.MemoryOptions
import de.bixilon.minosoft.util.collections.floats.FloatListUtil
import de.bixilon.minosoft.util.collections.ints.IntListUtil
import java.nio.FloatBuffer
import java.nio.IntBuffer

abstract class MeshBuilder(
    val context: RenderContext,
    val struct: MeshStruct,
    val primitive: PrimitiveTypes,
    var estimate: Int = 512,
    data: FloatList? = null,
    index: IntList? = null,
) : VertexConsumer {
    var _data = data
    val data: FloatList
        get() {
            if (_data == null) {
                _data = FloatListUtil.direct(estimate * primitive.vertices * struct.floats)
            }
            return _data.unsafeCast()
        }

    var _index = index
    val index: IntList
        get() {
            if (_index == null) {
                _index = IntListUtil.direct(estimate * primitive.vertices)
            }
            return _index.unsafeCast()
        }

    init {
        assert(primitive in context.system.primitives) { "Primitive type not supported by render system: $primitive" }
    }


    protected open val reused: Boolean = false

    private fun createNativeData(): FloatBuffer {
        val data = this._data

        val native = data?.toUnsafeNativeBuffer()
        val buffer = native ?: data?.toBuffer { MemoryOptions.allocateFloat(it) } ?: MemoryOptions.allocateFloat(0)

        buffer.limit(data?.size ?: 0); buffer.position(0)

        dropData(native == null)

        return buffer
    }

    private fun createIndexNativeData(): IntBuffer? {
        val index = this._index ?: return null
        if (index.isEmpty) return null

        val native = index.toUnsafeNativeBuffer()
        val buffer = native ?: index.toBuffer { MemoryOptions.allocateInt(it) }

        buffer.limit(index.size); buffer.position(0)

        dropIndex(native == null)

        return buffer
    }

    protected fun create(): VertexBuffer {
        val data = createNativeData()
        val index = createIndexNativeData()

        return context.system.createVertexBuffer(struct, data, primitive, index, reused)
    }

    open fun bake() = Mesh(create())

    protected fun dropIndex(free: Boolean) {
        if (free && !reused) {
            _index?.free()
        }
        this._index = null
    }

    protected fun dropData(free: Boolean) {
        if (free && !reused) {
            _data?.free()
        }
        this._data = null
    }

    open fun drop(free: Boolean = true) {
        dropIndex(free)
        dropData(free)
    }

    override fun ensureSize(primitives: Int) {
        data.ensureSize(primitives * primitive.vertices * struct.floats)
    }
}
