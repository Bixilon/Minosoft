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

package de.bixilon.minosoft.gui.rendering.entities.renderer

import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.feature.EntityRenderFeature
import de.bixilon.minosoft.gui.rendering.entities.feature.FeatureManager
import de.bixilon.minosoft.gui.rendering.entities.hitbox.HitboxFeature

abstract class EntityRenderer<E : Entity>(
    val renderer: EntitiesRenderer,
    val entity: E,
) {
    private var update = 0L
    val features = FeatureManager(this)
    val info = entity.renderInfo

    val hitbox = HitboxFeature(this).register()


    var visible = true
        private set

    fun <T : EntityRenderFeature> T.register(): T {
        features += this
        return this
    }

    open fun update(millis: Long) {
        val delta = if (this.update <= 0L) 0.0f else ((millis - update) / 1000.0f)
        entity.draw(millis)
        features.update(millis, delta)
        this.update = millis
    }

    open fun unload() {
        features.unload()
    }

    open fun reset() {
        features.reset()
    }

    open fun updateVisibility(occluded: Boolean, visible: Boolean) {
        this.visible = visible
        features.updateVisibility(occluded)
    }
}
