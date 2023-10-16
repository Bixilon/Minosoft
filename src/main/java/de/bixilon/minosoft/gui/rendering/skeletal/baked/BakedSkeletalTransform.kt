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

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.skeletal.instance.TransformInstance

data class BakedSkeletalTransform(
    val id: Int,
    val pivot: Vec3,
    val children: Map<String, BakedSkeletalTransform>,
) {

    fun instance(): TransformInstance {
        val children: MutableMap<String, TransformInstance> = mutableMapOf()

        for ((name, child) in this.children) {
            children[name] = child.instance()
        }

        return TransformInstance(id, pivot, children)
    }
}
