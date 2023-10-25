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

package de.bixilon.minosoft.gui.rendering.entities.feature

import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance

class SkeletalFeature(
    renderer: EntityRenderer<*>,
    val instance: SkeletalInstance,
) : EntityRenderFeature(renderer) {
    private val manager = renderer.renderer.context.skeletal

    constructor(renderer: EntityRenderer<*>, model: BakedSkeletalModel) : this(renderer, model.createInstance(renderer.renderer.context))

    override fun update(millis: Long, delta: Float) {
        instance.transform.reset()
        instance.animation.draw(delta)
    }

    override fun draw() {
        manager.upload(instance)
        instance.model.mesh.draw()
    }
}
