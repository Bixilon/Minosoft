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

package de.bixilon.minosoft.gui.rendering.skeletal.baked

import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.skeletal.SkeletalMesh
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.SkeletalAnimation

data class BakedSkeletalModel(
    val mesh: SkeletalMesh,
    val transform: BakedSkeletalTransform,
    val transformCount: Int,
    val animations: Map<String, SkeletalAnimation>,
) {
    private var state = SkeletalModelStates.DECLARED


    fun load() {
        if (state != SkeletalModelStates.DECLARED) throw IllegalStateException("Can not load model!")
        mesh.load()
        state = SkeletalModelStates.LOADED
    }

    fun unload() {
        if (state != SkeletalModelStates.LOADED) throw IllegalStateException("Can not unload model!")
        mesh.unload()
        state = SkeletalModelStates.UNLOADED
    }


    fun createInstance(context: RenderContext): SkeletalInstance {
        val transforms = this.transform.instance()

        return SkeletalInstance(context, this, transforms)
    }
}
