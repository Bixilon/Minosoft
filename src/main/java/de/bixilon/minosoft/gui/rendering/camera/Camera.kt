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

package de.bixilon.minosoft.gui.rendering.camera

import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.RenderUtil.runAsync
import de.bixilon.minosoft.gui.rendering.camera.fog.FogManager
import de.bixilon.minosoft.gui.rendering.camera.frustum.Frustum
import de.bixilon.minosoft.gui.rendering.camera.occlusion.WorldOcclusionManager
import de.bixilon.minosoft.gui.rendering.camera.view.ViewManager
import de.bixilon.minosoft.gui.rendering.camera.visibility.WorldVisibility

class Camera(
    val context: RenderContext,
) {
    val fog = FogManager(context)
    val matrix = MatrixHandler(context, this)

    val frustum = Frustum(this, matrix, context.session.world)
    val occlusion = WorldOcclusionManager(context, this)
    val visibility = WorldVisibility(this)

    val view = ViewManager(this)

    val offset = WorldOffset(this)

    fun init() {
        matrix.init()
        view.init()
        context.session.camera::entity.observe(this) { context.session.camera.entity = it }
    }

    fun draw() {
        val entity = context.session.camera.entity
        context.profiler.profile("tick camera") { (entity.attachment.getRootVehicle() ?: entity).tryTick() }// TODO
        if (entity is LocalPlayerEntity) {
            entity._draw(now()) // TODO: force draw if entity is camera entity?
        }
        view.draw()
        matrix.draw()
        context.profiler.profile("camera") {
            val latch = SimpleLatch(2)
            context.runAsync("occlusion") { occlusion.draw(); latch.dec() }
            context.runAsync("target") { context.session.camera.target.update(); latch.dec() }
            fog.draw()
            latch.await()
        }
    }
}
