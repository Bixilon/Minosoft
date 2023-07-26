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

package de.bixilon.minosoft.gui.rendering.renderer.renderer

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalTask
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.latch.ParentLatch
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.gui.rendering.renderer.renderer.pipeline.RendererPipeline
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.WorldRenderer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class RendererManager(
    val context: RenderContext,
) : Drawable, Iterable<Renderer> {
    private val list: MutableList<Renderer> = mutableListOf()
    private val renderers: MutableMap<RendererBuilder<*>, Renderer> = linkedMapOf()
    private val pipeline = RendererPipeline(this)
    private val connection = context.connection


    fun <T : Renderer> register(renderer: T): T {
        this.list += renderer

        return renderer
    }

    fun <T : Renderer> register(builder: RendererBuilder<T>): T? {
        val renderer = builder.build(connection, context) ?: return null
        val previous = renderers.put(builder, renderer)
        if (previous != null) {
            Log.log(LogMessageType.RENDERING, LogLevels.WARN) { "Renderer $previous ($builder) got replaced by $renderer!" }
        }
        list += renderer
        pipeline += renderer
        return renderer
    }

    operator fun plusAssign(builder: RendererBuilder<*>) {
        register(builder)
    }

    operator fun <T : Renderer> get(builder: RendererBuilder<T>): T? {
        return renderers[builder].unsafeCast()
    }

    private fun runAsync(latch: AbstractLatch?, runnable: (Renderer, AbstractLatch) -> Unit) {
        val inner = if (latch == null) SimpleLatch(0) else ParentLatch(0, latch)

        val worker = UnconditionalWorker()
        for (renderer in list) {
            worker += { runnable.invoke(renderer, inner) }
        }
        worker.work(inner)
    }

    fun init(latch: AbstractLatch) {
        for (renderer in list) {
            if (renderer !is WorldRenderer) continue
            renderer.registerLayers()
        }
        pipeline.world.rebuild()

        runAsync(latch, Renderer::preAsyncInit)

        for (renderer in list) {
            renderer.init(latch)
        }
        runAsync(latch, Renderer::asyncInit)
    }

    fun postInit(latch: AbstractLatch) {
        for (renderer in list) {
            renderer.postInit(latch)
        }

        runAsync(latch, Renderer::postAsyncInit)
    }

    private fun prepare() {
        for (renderer in list) {
            renderer.prePrepareDraw()
        }

        val latch = SimpleLatch(0)
        val worker = UnconditionalWorker()
        for (renderer in list) {
            if (renderer !is AsyncRenderer) continue
            worker += UnconditionalTask(priority = ThreadPool.HIGHER) { renderer.prepareDrawAsync() }
        }
        worker.work(latch)

        for (renderer in list) {
            renderer.postPrepareDraw()
        }
    }

    override fun draw() {
        prepare()
        pipeline.draw()
    }

    override fun iterator(): Iterator<Renderer> {
        return list.iterator()
    }
}
