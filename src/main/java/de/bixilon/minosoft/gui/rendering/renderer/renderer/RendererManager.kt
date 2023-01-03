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
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalTask
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.phases.PostDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.PreDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.RenderPhases
import de.bixilon.minosoft.gui.rendering.system.base.phases.SkipAll
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class RendererManager(
    private val context: RenderContext,
) {
    private val renderers: MutableMap<ResourceLocation, Renderer> = synchronizedMapOf()
    private val connection = context.connection
    private val renderSystem = context.renderSystem
    private val framebufferManager = context.framebufferManager


    fun <T : Renderer> register(builder: RendererBuilder<T>): T? {
        val resourceLocation = builder.identifier
        if (resourceLocation in RunConfiguration.SKIP_RENDERERS) {
            return null
        }
        val renderer = builder.build(connection, context) ?: return null
        val previous = renderers.put(resourceLocation, renderer)
        if (previous != null) {
            Log.log(LogMessageType.RENDERING_LOADING, LogLevels.WARN) { "Renderer $previous(${builder.identifier}) got replaced by $renderer!" }
        }
        return renderer
    }

    operator fun plusAssign(builder: RendererBuilder<*>) {
        register(builder)
    }

    operator fun <T : Renderer> get(renderer: RendererBuilder<T>): T? {
        return this[renderer.identifier].unsafeCast()
    }

    operator fun get(resourceLocation: ResourceLocation): Renderer? {
        return renderers[resourceLocation]
    }

    fun init(latch: CountUpAndDownLatch) {
        val inner = CountUpAndDownLatch(0, latch)
        var worker = UnconditionalWorker()
        for (renderer in renderers.values) {
            worker += { renderer.preAsyncInit(inner) }
        }
        worker.work(inner)

        for (renderer in renderers.values) {
            renderer.init(inner)
        }

        worker = UnconditionalWorker()
        for (renderer in renderers.values) {
            worker += { renderer.asyncInit(inner) }
        }
        worker.work(inner)
    }

    fun postInit(latch: CountUpAndDownLatch) {
        for (renderer in renderers.values) {
            renderer.postInit(latch)
        }
        val inner = CountUpAndDownLatch(0, latch)
        val worker = UnconditionalWorker()
        for (renderer in renderers.values) {
            worker += { renderer.postAsyncInit(inner) }
        }
        worker.work(inner)
    }

    private fun renderNormal(rendererList: Collection<Renderer>) {
        for (phase in RenderPhases.VALUES) {
            for (renderer in rendererList) {
                if (renderer is SkipAll && renderer.skipAll) {
                    continue
                }
                if (!phase.type.java.isAssignableFrom(renderer::class.java)) {
                    continue
                }
                if (phase.invokeSkip(renderer)) {
                    continue
                }
                renderSystem.framebuffer = renderer.framebuffer
                renderSystem.polygonMode = renderer.polygonMode
                phase.invokeSetup(renderer)
                phase.invokeDraw(renderer)
            }
        }
    }

    private fun prepareDraw(rendererList: Collection<Renderer>) {
        for (renderer in rendererList) {
            renderer.prePrepareDraw()
        }

        val latch = CountUpAndDownLatch(0)
        val worker = UnconditionalWorker()
        for (renderer in rendererList) {
            if (renderer !is AsyncRenderer) {
                continue
            }
            worker += UnconditionalTask(priority = ThreadPool.HIGHER) { renderer.prepareDrawAsync() }
        }
        worker.work(latch)

        for (renderer in rendererList) {
            renderer.postPrepareDraw()
        }
    }

    fun render() {
        val renderers = renderers.values

        prepareDraw(renderers)
        renderNormal(renderers)

        renderSystem.framebuffer = null
        renderPre(renderers)
        framebufferManager.draw()
        renderPost(renderers)
    }

    private fun renderPre(renderers: Collection<Renderer>) {
        for (renderer in renderers) {
            if (renderer is SkipAll && renderer.skipAll) {
                continue
            }
            if (renderer !is PreDrawable) {
                continue
            }
            if (renderer.skipPre) {
                continue
            }
            renderSystem.polygonMode = renderer.polygonMode
            renderer.drawPre()
        }
    }

    private fun renderPost(renderers: Collection<Renderer>) {
        for (renderer in renderers) {
            if (renderer is SkipAll && renderer.skipAll) {
                continue
            }
            if (renderer !is PostDrawable) {
                continue
            }
            if (renderer.skipPost) {
                continue
            }
            renderSystem.polygonMode = renderer.polygonMode
            renderer.drawPost()
        }
    }
}
