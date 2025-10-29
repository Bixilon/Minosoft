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

package de.bixilon.minosoft.gui.rendering.entities.draw

import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.feature.FeatureDrawable
import de.bixilon.minosoft.gui.rendering.entities.visibility.EntityLayer

class EntityDrawer(
    val renderer: EntitiesRenderer,
) {
    private val lock = Lock.lock()
    private val layers = HashMap<EntityLayer, ArrayList<FeatureDrawable>>()

    var size = 0
        private set

    fun registerLayers() {
        for (layer in EntityLayer.LAYERS) {
            renderer.layers.register(layer, null, { layers[layer]?.draw() })
        }
    }

    private fun ArrayList<FeatureDrawable>.sort(order: EntityLayer.EntitySortOrders) {
        this.sortWith { a, b ->
            var sort = a.compareTo(b)
            if (sort != 0) return@sortWith sort

            // TODO: compare by distance (in respect with order)

            return@sortWith 0

        }
    }

    private fun ArrayList<FeatureDrawable>.draw() = forEach { it.draw() }

    fun prepare() {
        var size = 0
        for ((layer, features) in this.layers) {
            for (feature in features) {
                feature.prepare()
                size++
            }
            features.sort(layer.sort)
        }
        this.size = size
    }


    fun clear() {
        // don't remove array lists (reduce useless allocations)
        for ((_, features) in this.layers) {
            features.clear()
        }
    }

    operator fun plusAssign(drawable: FeatureDrawable) = lock.locked {
        val layer = drawable.layer
        this.layers.getOrPut(layer) { ArrayList(100) } += drawable
    }
}
