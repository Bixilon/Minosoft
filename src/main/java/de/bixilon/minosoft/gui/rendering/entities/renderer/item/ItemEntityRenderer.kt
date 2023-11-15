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

package de.bixilon.minosoft.gui.rendering.entities.renderer.item

import de.bixilon.kutil.math.MathConstants.PIf
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.entities.entities.item.ItemEntity
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.factory.RegisteredEntityModelFactory
import de.bixilon.minosoft.gui.rendering.entities.feature.item.ItemFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.models.raw.display.DisplayPositions
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.translateYAssign
import kotlin.math.sin

class ItemEntityRenderer(renderer: EntitiesRenderer, entity: ItemEntity) : EntityRenderer<ItemEntity>(renderer, entity) {
    val item = ItemFeature(this, null, DisplayPositions.GROUND).register()
    private var floating = 0.0f
    private var rotation = 0.0f

    init {
        entity::stack.observe(this, true) { item.stack = it }
    }

    override fun update(millis: Long, delta: Float) {
        updateFloatingRotation(delta)
        super.update(millis, delta)
    }

    private fun updateFloatingRotation(delta: Float) {
        floating += delta / 3.0f
        if (floating > 1.0f) floating %= 1.0f

        rotation += delta / CIRCLE
        if (rotation > 1.0f) rotation %= 1.0f
    }

    override fun updateMatrix(delta: Float) {
        super.updateMatrix(delta)

        this.matrix
            .translateYAssign(sin(floating * CIRCLE) * 0.1f + 0.1f)
            .rotateYassign(rotation * CIRCLE)
    }


    companion object : RegisteredEntityModelFactory<ItemEntity>, Identified {
        const val CIRCLE = PIf * 2.0f
        override val identifier get() = ItemEntity.identifier

        override fun create(renderer: EntitiesRenderer, entity: ItemEntity) = ItemEntityRenderer(renderer, entity)
    }
}
