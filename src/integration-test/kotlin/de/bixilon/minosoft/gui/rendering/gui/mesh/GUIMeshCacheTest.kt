package de.bixilon.minosoft.gui.rendering.gui.mesh

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.test.GuiRenderTestUtil
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["gui"])
class GUIMeshCacheTest {

    fun `first render call`() {
        var called = 0
        val element = element { called++ }
        val consumer = DummyGUIVertexConsumer()
        val rendered = element.render(Vec2.EMPTY, consumer, null)
        assertTrue(rendered)

        assertEquals(called, 1)
        assertEquals(consumer.caches, 1)
    }

    fun `basic cache`() {
        var called = 0
        val element = element { called++ }
        val consumer = DummyGUIVertexConsumer()
        element.render(Vec2.EMPTY, consumer, null)
        assertFalse(element.render(Vec2.EMPTY, consumer, null))

        assertEquals(called, 1)
        assertEquals(consumer.caches, 2)
    }

    fun `indirect invalidation, offset changed`() {
        var called = 0
        val element = element { called++ }
        val consumer = DummyGUIVertexConsumer()
        element.render(Vec2.EMPTY, consumer, null)
        assertTrue(element.render(Vec2(1.0f, 1.0f), consumer, null))

        assertEquals(called, 2)
        assertEquals(consumer.caches, 2)
    }

    fun `indirect invalidation, options changed`() {
        var called = 0
        val element = element { called++ }
        val consumer = DummyGUIVertexConsumer()
        element.render(Vec2.EMPTY, consumer, GUIVertexOptions(alpha = 0.9f))
        assertTrue(element.render(Vec2.EMPTY, consumer, GUIVertexOptions(alpha = 0.8f)))

        assertEquals(called, 2)
        assertEquals(consumer.caches, 2)
    }

    fun `don't render again after indirect invalidation`() {
        var called = 0
        val element = element { called++ }
        val consumer = DummyGUIVertexConsumer()
        element.render(Vec2.EMPTY, consumer, null)
        element.render(Vec2(1.0f), consumer, null)
        assertFalse(element.render(Vec2(1.0f), consumer, null))

        assertEquals(called, 2)
        assertEquals(consumer.caches, 3)
    }

    fun `manual cache invalidation`() {
        var called = 0
        val element = element { called++ }
        val consumer = DummyGUIVertexConsumer()
        element.render(Vec2.EMPTY, consumer, null)
        element.cache.invalidate()
        assertTrue(element.render(Vec2.EMPTY, consumer, null))

        assertEquals(called, 2)
        assertEquals(consumer.caches, 2)
    }

    fun `initial disabled cache`() {
        var called = 0
        val element = element { called++ }
        val consumer = DummyGUIVertexConsumer()
        element.cache.disable()
        assertTrue(element.render(Vec2.EMPTY, consumer, null))
        assertTrue(element.render(Vec2.EMPTY, consumer, null))

        assertEquals(called, 2)
        assertEquals(consumer.caches, 0)
    }

    fun `post disabled cache`() {
        var called = 0
        val element = element { called++ }
        val consumer = DummyGUIVertexConsumer()
        assertTrue(element.render(Vec2.EMPTY, consumer, null))
        element.cache.disable()
        assertTrue(element.render(Vec2.EMPTY, consumer, null))

        assertEquals(called, 2)
        assertEquals(consumer.caches, 1)
    }

    fun `initial disabled cache and enabled initial`() {
        var called = 0
        val element = element { called++ }
        val consumer = DummyGUIVertexConsumer()
        element.cache.disable()
        assertFalse(element.cache.enabled())
        element.cache.enable()
        assertTrue(element.cache.enabled())

        assertTrue(element.render(Vec2.EMPTY, consumer, null))
        assertFalse(element.render(Vec2.EMPTY, consumer, null))

        assertEquals(called, 1)
        assertEquals(consumer.caches, 2)
    }

    fun `initial disabled cache and post enabled`() {
        var called = 0
        val element = element { called++ }
        val consumer = DummyGUIVertexConsumer()
        element.cache.disable()
        assertTrue(element.render(Vec2.EMPTY, consumer, null))
        element.cache.enable()
        assertTrue(element.render(Vec2.EMPTY, consumer, null))

        assertEquals(called, 2)
        assertEquals(consumer.caches, 1)
    }

    // TODO: children, parent calls, screen change


    private fun element(callback: () -> Unit): Element {
        return object : Element(GuiRenderTestUtil.create()) {
            override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
                callback()
            }

            override fun forceSilentApply() {
                TODO("Not yet implemented")
            }

        }
    }
}
