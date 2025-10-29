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

package de.bixilon.minosoft.gui.rendering.entities.feature.properties

import de.bixilon.minosoft.gui.rendering.entities.feature.DrawableEntityRenderFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStates
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

abstract class MeshedFeature<M : Mesh>(
    renderer: EntityRenderer<*>,
) : DrawableEntityRenderFeature(renderer) {
    protected open var mesh: M? = null
    override var enabled: Boolean
        get() = super.enabled && mesh != null
        set(value) {
            super.enabled = value
        }

    override val sort = this::class.java.hashCode()

    override fun update(time: ValueTimeMark, delta: Float) {
        if (!super.enabled) return unload()
    }

    override fun invalidate() {
        unload()
    }

    override fun draw() {
        val mesh = this.mesh ?: return
        if (mesh.state != MeshStates.LOADED) mesh.load()
        draw(mesh)
    }

    protected open fun draw(mesh: M) {
        mesh.draw()
    }

    override fun unload() {
        val mesh = this.mesh ?: return
        this.mesh = null
        renderer.renderer.queue += { mesh.unload() }
    }
}
