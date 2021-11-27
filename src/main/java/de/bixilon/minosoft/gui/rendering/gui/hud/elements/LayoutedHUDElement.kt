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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList
import glm_.vec2.Vec2i

abstract class LayoutedHUDElement<T : Element>(final override val hudRenderer: HUDRenderer) : HUDElement {
    final override val renderWindow: RenderWindow = hudRenderer.renderWindow
    override var enabled = true
    var mesh: GUIMesh = GUIMesh(renderWindow, hudRenderer.matrix, DirectArrayFloatList(1000))
    private var lastRevision = 0L


    abstract val layout: T
    abstract val layoutOffset: Vec2i

    override fun tick() {
        layout.tick()
    }

    private fun createNewMesh() {
        val mesh = this.mesh
        if (mesh.state == Mesh.MeshStates.LOADED) {
            mesh.unload()
        }
        this.mesh = GUIMesh(renderWindow, hudRenderer.matrix, mesh.data)
    }

    fun prepare(z: Int): Int {
        val layoutOffset = layoutOffset
        val usedZ = layout.render(layoutOffset, z, mesh, null)

        val revision = layout.cache.revision
        if (revision != lastRevision) {
            createNewMesh()
            this.mesh.load()
            this.lastRevision = revision
        }

        return usedZ
    }


    fun initMesh() {
        layout.cache.data = mesh.data
        mesh.load()
    }
}
