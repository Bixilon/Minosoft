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
import de.bixilon.minosoft.gui.rendering.entities.feature.EntityRenderFeature
import de.bixilon.minosoft.gui.rendering.entities.feature.register.EntityRenderFeatures
import de.bixilon.minosoft.gui.rendering.entities.visibility.EntityLayer
import de.bixilon.minosoft.gui.rendering.entities.visibility.VisibilityManager
import de.bixilon.minosoft.gui.rendering.renderer.renderer.AsyncRenderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.LayerSettings
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class EntitiesRenderer(
    val session: PlaySession,
    override val context: RenderContext,
) : WorldRenderer, AsyncRenderer {
    override val layers = LayerSettings()
    override val renderSystem: RenderSystem = context.system
    val profile = session.profiles.entity
    val features = EntityRenderFeatures(this)
    val renderers = EntityRendererManager(this)
    val visibility = VisibilityManager(this)
    val queue = Queue()


    private var reset = false

    override fun registerLayers() {
        layers.register(EntityLayer.Opaque, null, { visibility.opaque.draw() }) { visibility.opaque.isEmpty() }
        layers.register(EntityLayer.Translucent, null, { visibility.translucent.draw() }) { visibility.translucent.isEmpty() }
    }

    override fun prePrepareDraw() {
        queue.work()
    }

    override fun prepareDrawAsync() {
        this.features.update()
        val time = now()
        this.visibility.reset()
        renderers.iterate {
            try {
                if (reset) it.reset()
                visibility.update(it, time)
                if (!it.visible) return@iterate
                it.update(time)
                it.prepare()
                visibility.collect(it)
            } catch (error: Throwable) {
                error.printStackTrace()
                Exception("Exception while rendering entities: ${session.id}", error).crash()
            }
        }
        this.reset = false
        this.visibility.finish()
    }

    override fun postPrepareDraw() {
        queue.work()
    }

    override fun init(latch: AbstractLatch) {
        context.camera.offset::offset.observe(this) { reset = true }
        features.init()
        renderers.init()
        visibility.init()
    }

    override fun postInit(latch: AbstractLatch) {
        features.postInit()
    }

    private fun ArrayList<EntityRenderFeature>.draw() {
        for (feature in this) {
            feature.draw()
        }
    }

    companion object : RendererBuilder<EntitiesRenderer> {

        override fun build(session: PlaySession, context: RenderContext): EntitiesRenderer? {
            if (!session.profiles.entity.general.enabled) return null
            return EntitiesRenderer(session, context)
        }
    }
}
