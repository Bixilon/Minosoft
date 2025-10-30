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

package de.bixilon.minosoft.gui.rendering.entities.feature

import de.bixilon.minosoft.gui.rendering.entities.draw.EntityDrawer
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entities.visibility.EntityVisibilityLevels
import kotlin.time.Duration

class FeatureManager(val renderer: EntityRenderer<*>) : Iterable<EntityRenderFeature> {
    private val features: ArrayList<EntityRenderFeature> = ArrayList(10)


    operator fun plusAssign(feature: EntityRenderFeature) = register(feature)
    fun register(feature: EntityRenderFeature) {
        this.features += feature
    }

    operator fun minusAssign(feature: EntityRenderFeature) = remove(feature)
    fun remove(feature: EntityRenderFeature) {
        this.features -= feature
    }

    fun update(delta: Duration) {
        for (feature in features) {
            if (!feature.isVisible()) continue
            feature.update(delta)
        }
    }

    fun unload() {
        features.forEach { it.unload() }
        features.clear()
    }

    fun enqueueUnload() {
        features.forEach { it.enqueueUnload() }
    }

    @Deprecated("What, why and how?")
    fun invalidate() = features.forEach { it.invalidate() }
    fun updateVisibility(level: EntityVisibilityLevels) = features.forEach { it.updateVisibility(level) }
    fun collect(drawer: EntityDrawer) {
        for (feature in features) {
            if (!feature.isVisible()) continue
            feature.collect(drawer)
        }
    }

    fun clear() {
        features.clear()
    }

    override fun iterator(): Iterator<EntityRenderFeature> {
        return features.iterator()
    }
}
