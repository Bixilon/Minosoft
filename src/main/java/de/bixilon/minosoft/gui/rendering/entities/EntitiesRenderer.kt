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

package de.bixilon.minosoft.gui.rendering.entities

import de.bixilon.kutil.concurrent.queue.Queue
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.entities.draw.EntityDrawer
import de.bixilon.minosoft.gui.rendering.entities.feature.register.EntityRenderFeatures
import de.bixilon.minosoft.gui.rendering.entities.visibility.VisibilityManager
import de.bixilon.minosoft.gui.rendering.renderer.renderer.AsyncRenderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.LayerSettings
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.WorldRenderer
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class EntitiesRenderer(
    val session: PlaySession,
    override val context: RenderContext,
) : WorldRenderer, AsyncRenderer {
    override val layers = LayerSettings()
    val profile = session.profiles.entity
    val features = EntityRenderFeatures(this)
    val renderers = EntityRendererManager(this)
    val visibility = VisibilityManager(this)
    val drawer = EntityDrawer(this)
    val queue = Queue()


    private var invalid = false

    override fun registerLayers() = drawer.registerLayers()

    override fun prePrepareDraw() {
        queue.work()
    }

    override fun prepareDrawAsync() {
        this.features.update()
        val time = now()
        this.visibility.update()
        drawer.clear()

        renderers.iterate {
            try {
                if (invalid) {
                    it.invalidate()
                }

                it.updateVisibility(visibility.getVisibilityLevel(it)) // TODO: only calculate if position, world or frustum changed (but still set it)
                it.enqueueUnload()

                if (!it.isVisible()) return@iterate

                it.update(time)
                it.enqueueUnload()

                it.collect(drawer)
            } catch (error: Throwable) {
                error.printStackTrace()
                Exception("Exception while rendering entity (session=${session.id}, entity=${it.entity})", error).crash()
            }
        }
        this.invalid = false
    }

    override fun postPrepareDraw() {
        context.profiler.profile("queue") { queue.work() }
        drawer.prepare()
    }

    override fun init(latch: AbstractLatch) {
        context.camera.offset::offset.observe(this) { invalid = true }
        features.init()
        renderers.init()
        visibility.init()
    }

    override fun postInit(latch: AbstractLatch) {
        features.postInit()
    }


    companion object : RendererBuilder<EntitiesRenderer> {

        override fun build(session: PlaySession, context: RenderContext): EntitiesRenderer? {
            if (!session.profiles.entity.general.enabled) return null
            return EntitiesRenderer(session, context)
        }
    }
}
