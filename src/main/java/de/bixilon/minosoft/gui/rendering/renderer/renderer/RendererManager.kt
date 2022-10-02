/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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
import de.bixilon.kutil.concurrent.worker.TaskWorker
import de.bixilon.kutil.concurrent.worker.tasks.Task
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.config.profile.ConnectionProfiles
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.entity.EntityRenderer
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.system.base.phases.PostDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.PreDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.RenderPhases
import de.bixilon.minosoft.gui.rendering.system.base.phases.SkipAll
import de.bixilon.minosoft.gui.rendering.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.world.border.WorldBorderRenderer
import de.bixilon.minosoft.gui.rendering.world.chunk.ChunkBorderRenderer
import de.bixilon.minosoft.gui.rendering.world.outline.BlockOutlineRenderer
import de.bixilon.minosoft.terminal.RunConfiguration

class RendererManager(
    private val renderWindow: RenderWindow,
) {
    private val renderers: MutableMap<ResourceLocation, Renderer> = synchronizedMapOf()
    private val connection = renderWindow.connection
    private val renderSystem = renderWindow.renderSystem
    private val framebufferManager = renderWindow.framebufferManager


    fun register(builder: RendererBuilder<*>) {
        val resourceLocation = builder.RESOURCE_LOCATION
        if (resourceLocation in RunConfiguration.SKIP_RENDERERS) {
            return
        }
        renderers[resourceLocation] = builder.build(connection, renderWindow)
    }

    operator fun plusAssign(builder: RendererBuilder<*>) = register(builder)

    operator fun <T : Renderer> get(renderer: RendererBuilder<T>): T? {
        return this[renderer.RESOURCE_LOCATION].unsafeCast()
    }

    operator fun get(resourceLocation: ResourceLocation): Renderer? {
        return renderers[resourceLocation]
    }

    fun init(latch: CountUpAndDownLatch) {
        val inner = CountUpAndDownLatch(0, latch)
        var worker = TaskWorker()
        for (renderer in renderers.values) {
            worker += { renderer.preAsyncInit(inner) }
        }
        worker.work(inner)

        for (renderer in renderers.values) {
            renderer.init(inner)
        }

        worker = TaskWorker()
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
        val worker = TaskWorker()
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
        val worker = TaskWorker()
        for (renderer in rendererList) {
            if (renderer !is AsyncRenderer) {
                continue
            }
            worker += Task(priority = ThreadPool.HIGHER) { renderer.prepareDrawAsync() }
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

    companion object {

        fun RendererManager.registerDefault(profiles: ConnectionProfiles) {
            // order dependent (from back to front)
            register(SkyRenderer)
            register(WorldRenderer)
            register(BlockOutlineRenderer)
            if (!profiles.particle.skipLoading) {
                register(ParticleRenderer)
            }
            register(EntityRenderer)
            register(ChunkBorderRenderer)
            register(WorldBorderRenderer)
            register(GUIRenderer)
        }
    }
}
