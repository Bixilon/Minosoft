/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.hud.elements

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.gui.AbstractGUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.Initializable
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.renderer.Drawable
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList

class LayoutedHUDElement<T : LayoutedElement>(
    val layout: T,
) : HUDElement, Drawable {
    val elementLayout = layout.unsafeCast<Element>()
    override val guiRenderer: AbstractGUIRenderer = elementLayout.guiRenderer
    override val renderWindow: RenderWindow = guiRenderer.renderWindow
    override var enabled = true
    var mesh: GUIMesh = GUIMesh(renderWindow, guiRenderer.matrix, DirectArrayFloatList(1000))
    private var lastRevision = 0L
    override val skipDraw: Boolean
        get() = if (layout is Drawable) layout.skipDraw else false

    override fun tick() {
        elementLayout.tick()
    }

    override fun init() {
        if (layout is Initializable) {
            layout.init()
        }
    }

    override fun postInit() {
        if (layout is Initializable) {
            layout.postInit()
        }
    }

    private fun createNewMesh() {
        val mesh = this.mesh
        if (mesh.state == Mesh.MeshStates.LOADED) {
            mesh.unload()
        }
        this.mesh = GUIMesh(renderWindow, guiRenderer.matrix, mesh.data)
    }

    fun prepare(z: Int): Int {
        val layoutOffset = layout.layoutOffset
        val usedZ = elementLayout.render(layoutOffset, z, mesh, null)

        val revision = elementLayout.cache.revision
        if (revision != lastRevision) {
            createNewMesh()
            this.mesh.load()
            this.lastRevision = revision
        }

        return usedZ
    }

    override fun draw() {
        if (layout is Drawable) {
            layout.draw()
        }
    }


    fun initMesh() {
        elementLayout.cache.data = mesh.data
        mesh.load()
    }
}
