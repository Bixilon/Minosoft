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

package de.bixilon.minosoft.gui.rendering.renderer.renderer.pipeline.world

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.font.manager.FontManager
import de.bixilon.minosoft.gui.rendering.font.types.dummy.DummyFontType
import de.bixilon.minosoft.gui.rendering.framebuffer.FramebufferManager
import de.bixilon.minosoft.gui.rendering.framebuffer.world.WorldFramebuffer
import de.bixilon.minosoft.gui.rendering.gui.atlas.textures.CodeTexturePart
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererManager
import de.bixilon.minosoft.gui.rendering.renderer.renderer.pipeline.RendererPipeline
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.LayerSettings
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.gui.rendering.system.base.layer.OpaqueLayer
import de.bixilon.minosoft.gui.rendering.system.base.layer.RenderLayer
import de.bixilon.minosoft.gui.rendering.system.base.layer.TranslucentLayer
import de.bixilon.minosoft.gui.rendering.system.base.layer.TransparentLayer
import de.bixilon.minosoft.gui.rendering.system.base.settings.RenderSettings
import de.bixilon.minosoft.gui.rendering.system.dummy.DummyRenderSystem
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTexture
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTextureManager
import de.bixilon.minosoft.test.IT
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["rendering"])
class WorldRendererPipelineTest {
    private val elements = WorldRendererPipeline::class.java.getFieldOrNull("elements")!!
    val WorldRendererPipeline.elements: Array<PipelineElement> get() = this@WorldRendererPipelineTest.elements.get(this).unsafeCast()

    private val pipeline = RendererManager::class.java.getFieldOrNull("pipeline")!!
    val RendererManager.pipeline: RendererPipeline get() = this@WorldRendererPipelineTest.pipeline.get(this).unsafeCast()


    fun construct() {
        manager()
        renderer()
    }

    fun `register single layer`() {
        val manager = manager()
        val renderer = manager.register(renderer())

        renderer.layers.register(OpaqueLayer, null, renderer::run)

        manager.pipeline.world.rebuild()

        val elements = manager.pipeline.world.elements
        assertEquals(elements.size, 1)
        assertEquals(elements[0].layer, OpaqueLayer)
    }

    fun `register 2 but same layer, check insertion order`() {
        val manager = manager()
        val renderer = manager.register(renderer())

        renderer.layers.register(OpaqueLayer, null, renderer::run)
        renderer.layers.register(layer("abc", OpaqueLayer.priority), null, renderer::run)

        manager.pipeline.world.rebuild()

        val elements = manager.pipeline.world.elements
        assertEquals(elements.size, 2)
        assertEquals(elements[0].layer, OpaqueLayer)
        assertEquals(elements[1].layer.priority, OpaqueLayer.priority)
    }

    fun `register 2 but different layers priority, inserted correct order`() {
        val manager = manager()
        val renderer = manager.register(renderer())

        renderer.layers.register(OpaqueLayer, null, renderer::run)
        renderer.layers.register(layer("abc", 10), null, renderer::run)

        manager.pipeline.world.rebuild()

        val elements = manager.pipeline.world.elements
        assertEquals(elements.size, 2)
        assertEquals(elements[0].layer, OpaqueLayer)
        assertEquals(elements[1].layer.priority, 10)
    }

    fun `register 2 but different layers priority, wrong insertion order`() {
        val manager = manager()
        val renderer = manager.register(renderer())

        renderer.layers.register(layer("abc", 10), null, renderer::run)
        renderer.layers.register(OpaqueLayer, null, renderer::run)

        manager.pipeline.world.rebuild()

        val elements = manager.pipeline.world.elements
        assertEquals(elements.size, 2)
        assertEquals(elements[0].layer, OpaqueLayer)
        assertEquals(elements[1].layer.priority, 10)
    }

    fun `register 4 but different layers`() {
        val manager = manager()
        val renderer = manager.register(renderer())

        renderer.layers.register(layer("a", 10), null, renderer::run)
        renderer.layers.register(OpaqueLayer, null, renderer::run)
        renderer.layers.register(layer("b", -10), null, renderer::run)
        renderer.layers.register(layer("c", -10), null, renderer::run)

        manager.pipeline.world.rebuild()

        val elements = manager.pipeline.world.elements
        assertEquals(elements.size, 4)
        assertEquals(elements[0].layer.unsafeCast<Identified>().identifier.path, "b")
        assertEquals(elements[1].layer.unsafeCast<Identified>().identifier.path, "c")
        assertEquals(elements[2].layer, OpaqueLayer)
        assertEquals(elements[3].layer.unsafeCast<Identified>().identifier.path, "a")
    }

    fun `opaque transparent translucent`() {
        val manager = manager()
        val renderer = manager.register(renderer())

        renderer.layers.register(TransparentLayer, null, renderer::run)
        renderer.layers.register(TranslucentLayer, null, renderer::run)
        renderer.layers.register(OpaqueLayer, null, renderer::run)

        manager.pipeline.world.rebuild()

        val elements = manager.pipeline.world.elements
        assertEquals(elements.size, 3)
        assertEquals(elements[0].layer, OpaqueLayer)
        assertEquals(elements[1].layer, TransparentLayer)
        assertEquals(elements[2].layer, TranslucentLayer)
    }

    fun `different renderer`() {
        val manager = manager()

        val first = manager.register(renderer())
        val second = manager.register(renderer())

        first.layers.register(OpaqueLayer, null, first::run)
        second.layers.register(OpaqueLayer, null, second::run)
        second.layers.register(TranslucentLayer, null, second::run)

        manager.pipeline.world.rebuild()

        val elements = manager.pipeline.world.elements
        assertEquals(elements.size, 3)
        assertEquals(elements[0].layer, OpaqueLayer)
        assertEquals(elements[1].layer, OpaqueLayer)
        assertEquals(elements[2].layer, TranslucentLayer)
    }

    fun `correct render draw order`() {
        val manager = manager()

        val list: MutableList<String> = mutableListOf()

        val first = manager.register(renderer())
        val second = manager.register(renderer())

        first.layers.register(OpaqueLayer, null, { list += "a" })
        second.layers.register(OpaqueLayer, null, { list += "b" })
        second.layers.register(TranslucentLayer, null, { list += "c" })

        manager.pipeline.world.rebuild()

        manager.pipeline.world.draw()

        assertEquals(list, listOf("a", "b", "c"))
    }

    private fun context(): RenderContext {
        val context = IT.OBJENESIS.newInstance(RenderContext::class.java)
        context.font = FontManager(DummyFontType)
        context::system.forceSet(DummyRenderSystem(context))
        context::textures.forceSet(DummyTextureManager(context))

        val framebuffer = IT.OBJENESIS.newInstance(FramebufferManager::class.java)
        framebuffer::world.forceSet(IT.OBJENESIS.newInstance(WorldFramebuffer::class.java).apply { this::polygonMode.forceSet(PolygonModes.FILL) })
        context::framebuffer.forceSet(framebuffer)

        context.textures::whiteTexture.forceSet(CodeTexturePart(DummyTexture(), size = Vec2i(16, 16)))

        return context
    }


    private fun manager(): RendererManager {
        val context = context()
        return RendererManager(context)
    }

    private fun renderer() = object : WorldRenderer {
        override val context get() = Broken()
        override val framebuffer get() = null
        override val renderSystem get() = Broken()

        override val layers = LayerSettings()
        override fun registerLayers() = Unit


        fun run(): Unit = TODO()
    }

    private fun layer(name: String, priority: Int) = object : RenderLayer, Identified {
        override val identifier = minosoft(name)
        override val settings = RenderSettings.DEFAULT
        override val priority = priority
    }
}
