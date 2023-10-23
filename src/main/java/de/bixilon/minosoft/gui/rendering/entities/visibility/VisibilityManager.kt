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

package de.bixilon.minosoft.gui.rendering.entities.visibility

import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.feature.EntityRenderFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import java.util.concurrent.atomic.AtomicInteger

class VisibilityManager(val renderer: EntitiesRenderer) : Iterable<EntityRenderFeature> {
    private var update = false
    var size: Int = 0
        private set

    private val count = AtomicInteger()

    fun init() {
        renderer.connection.events.listen<VisibilityGraphChangeEvent> { update = true }
    }

    fun reset() {
        count.set(0)
    }

    fun update(renderer: EntityRenderer<*>) {
        renderer.visibility.update(this.update)
        if (renderer.visibility.visible) {
            count.incrementAndGet()
        }
    }

    fun finish() {
        this.update = false
        size = count.get()
    }

    override fun iterator(): Iterator<EntityRenderFeature> {
        TODO()
    }
}
