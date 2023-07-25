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

package de.bixilon.minosoft.gui.rendering.camera

import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.RenderUtil.runAsync
import de.bixilon.minosoft.gui.rendering.camera.view.ViewManager
import de.bixilon.minosoft.gui.rendering.chunk.view.WorldVisibilityGraph

class Camera(
    val context: RenderContext,
) {
    val fogManager = FogManager(context)
    val matrixHandler = MatrixHandler(context, this)
    val visibilityGraph = WorldVisibilityGraph(context, this)

    val view = ViewManager(this)

    val offset = WorldOffset(this)

    fun init() {
        matrixHandler.init()
        view.init()
        context.connection.camera::entity.observe(this) { context.connection.camera.entity = it }
    }

    fun draw() {
        val entity = context.connection.camera.entity
        (entity.attachment.getRootVehicle() ?: entity).tryTick() // TODO
        if (entity is LocalPlayerEntity) {
            entity._draw(millis())
        }
        view.draw()
        matrixHandler.draw()
        val latch = SimpleLatch(2)
        context.runAsync { visibilityGraph.draw(); latch.dec() }
        context.runAsync { context.connection.camera.target.update(); latch.dec() }
        fogManager.draw()
        latch.await()
    }
}
