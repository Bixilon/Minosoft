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

package de.bixilon.minosoft.gui.rendering.renderer

import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh

interface MeshSwapper : Renderer {
    var mesh: LineMesh?
    var nextMesh: LineMesh?
    var unload: Boolean


    override fun postPrepareDraw() {
        if (unload) {
            this.mesh?.unload()
            this.mesh = null
            unload = false
        }
        val nextMesh = this.nextMesh ?: return
        nextMesh.load()
        if (this.mesh?.state == Mesh.MeshStates.LOADED) {
            this.mesh?.unload()
        }
        this.mesh = nextMesh
        this.nextMesh = null
    }
}
