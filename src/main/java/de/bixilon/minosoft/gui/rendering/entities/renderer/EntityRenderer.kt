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
import de.bixilon.minosoft.gui.rendering.entities.visibility.EntityVisibility

abstract class EntityRenderer<E : Entity>(
    val renderer: EntitiesRenderer,
    val entity: Entity,
) {
    val features = FeatureManager(this)
    val visibility = EntityVisibility(this)
    val info = entity.renderInfo

    val hitbox = HitboxFeature(this).register()

    fun <T : EntityRenderFeature> T.register(): T {
        features += this
        return this
    }

    open fun update(millis: Long) {
        entity.draw(millis)
        features.update(millis)
    }

    open fun unload() {
        features.unload()
    }

    open fun reset() {
        features.reset()
    }
}
