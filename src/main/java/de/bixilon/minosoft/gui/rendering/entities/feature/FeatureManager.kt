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

import de.bixilon.minosoft.gui.rendering.entities.feature.properties.InvisibleFeature.Companion.isInvisible
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer

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

    fun update(millis: Long, delta: Float) {
        for (feature in features) {
            if (feature.isInvisible()) continue
            feature.update(millis, delta)
        }
    }

    fun prepare() {
        for (feature in features) {
            if (feature.isInvisible()) continue
            feature.prepare()
        }
    }

    fun unload() {
        for (feature in features) {
            feature.unload()
        }
    }

    fun reset() {
        for (feature in features) {
            feature.reset()
        }
    }

    fun updateVisibility(occluded: Boolean) {
        for (feature in features) {
            feature.updateVisibility(occluded)
        }
    }

    fun clear() {
        features.clear()
    }


    override fun iterator(): Iterator<EntityRenderFeature> {
        return features.iterator()
    }
}
