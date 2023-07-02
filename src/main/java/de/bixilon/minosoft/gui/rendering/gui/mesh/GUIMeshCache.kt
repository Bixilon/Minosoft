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

package de.bixilon.minosoft.gui.rendering.gui.mesh

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.collections.primitive.floats.AbstractFloatList
import de.bixilon.kutil.collections.primitive.floats.HeapArrayFloatList
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.ChildedElement
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.ParentedElement
import de.bixilon.minosoft.gui.rendering.gui.properties.GUIScreen
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderIdentifiable
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.util.collections.DirectList

class GUIMeshCache(
    private val element: ParentedElement,
    private var screen: GUIScreen,
    override val order: Array<Pair<Int, Int>>,
    val context: RenderContext,
    initialCacheSize: Int = 1000,
    var data: AbstractFloatList = HeapArrayFloatList(initialCacheSize),
) : GUIVertexConsumer {
    private val whiteTexture = context.textures.whiteTexture

    private var offset = Vec2.EMPTY
    private var options: GUIVertexOptions? = null

    private var enabled = true // direct enable/disable call
    private var childEnabled = true // indirect call, from children
    private var invalid = true


    private inline fun checkMutable() {
        if (!CHECK_MUTABILITY) return
        if (!invalid) throw IllegalStateException("Cache is currently not mutable because it was not properly invalidated!")
    }

    override fun addVertex(position: Vec2, texture: ShaderIdentifiable?, uv: Vec2, tint: RGBColor, options: GUIVertexOptions?) {
        checkMutable()
        GUIMesh.addVertex(data, screen.half, position, texture ?: whiteTexture.texture, uv, tint, options)
    }

    override fun addCache(cache: GUIMeshCache) {
        checkMutable()
        data.add(cache.data)
    }

    private fun clear() {
        if (data.finished) {
            data = HeapArrayFloatList(initialSize = data.size)
        } else {
            data.clear()
        }
    }

    override fun ensureSize(size: Int) {
        checkMutable()
        data.ensureSize(size)
    }


    private fun canChildrenCache(): Boolean {
        if (element !is ChildedElement) return true
        for (child in element.children) {
            val cache = child.cache
            if (!cache.enabled) return false
            if (!cache.canChildrenCache()) return false
        }

        return true
    }

    private fun updateParentState(cache: Boolean) {
        if (!cache) {
            childEnabled = false
            element.parent?.cache?.updateParentState(false)
            return
        }
        val can = canChildrenCache()
        if (can == childEnabled) return
        childEnabled = can
        element.parent?.cache?.updateParentState(can)
    }


    fun enable() {
        enabled = true
        element.parent?.cache?.updateParentState(true)
    }

    fun disable() {
        enabled = false
        element.parent?.cache?.updateParentState(false)
    }

    fun invalidate() {
        invalid = true
        element.parent?.cache?.invalidate()
    }

    fun isValid(screen: GUIScreen, offset: Vec2, options: GUIVertexOptions?): Boolean {
        if (!this.enabled || !this.childEnabled) return false
        if (this.invalid) return false
        if (this.offset != offset) return false
        if (this.options != options) return false
        if (this.screen != screen) return false


        return true
    }

    fun start(screen: GUIScreen, offset: Vec2, options: GUIVertexOptions?) {
        clear()
        this.invalid = true
        this.screen = screen
        this.offset = offset
        this.options = options
    }

    fun end() {
        if (data !is DirectList) {
            // not raw mesh data
            data.finish()
        }
        invalid = false
    }


    fun onChildrenChange() {
        updateParentState(this.enabled)
    }

    fun onParentChange() {
        element.parent?.cache?.updateParentState(this.enabled)
    }

    fun enabled() = this.enabled && this.childEnabled


    companion object {
        const val CHECK_MUTABILITY = true
    }
}
