package de.bixilon.minosoft.gui.rendering.gui.mesh

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.ChildedElement
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.manager.SimpleChildrenManager
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.properties.GUIScreen
import de.bixilon.minosoft.gui.rendering.gui.test.GuiRenderTestUtil
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["gui"])
class GUIMeshCacheTest {

    fun `first render call`() {
        var called = 0
        val element = child { called++ }
        val consumer = TestCountGUIVertexConsumer()
        val rendered = element.render(Vec2.EMPTY, consumer, null)
        assertTrue(rendered)

        assertEquals(called, 1)
        assertEquals(consumer.caches, 1)
    }

    fun `basic cache`() {
        var called = 0
        val element = child { called++ }
        val consumer = TestCountGUIVertexConsumer()
        element.render(Vec2.EMPTY, consumer, null)
        assertFalse(element.render(Vec2.EMPTY, consumer, null))

        assertEquals(called, 1)
        assertEquals(consumer.caches, 2)
    }

    fun `indirect invalidation, offset changed`() {
        var called = 0
        val element = child { called++ }
        val consumer = TestCountGUIVertexConsumer()
        element.render(Vec2.EMPTY, consumer, null)
        assertTrue(element.render(Vec2(1.0f, 1.0f), consumer, null))

        assertEquals(called, 2)
        assertEquals(consumer.caches, 2)
    }

    fun `indirect invalidation, options changed`() {
        var called = 0
        val element = child { called++ }
        val consumer = TestCountGUIVertexConsumer()
        element.render(Vec2.EMPTY, consumer, GUIVertexOptions(alpha = 0.9f))
        assertTrue(element.render(Vec2.EMPTY, consumer, GUIVertexOptions(alpha = 0.8f)))

        assertEquals(called, 2)
        assertEquals(consumer.caches, 2)
    }

    fun `indirect invalidation, screen changed`() {
        var called = 0
        val element = child { called++ }
        val consumer = TestCountGUIVertexConsumer()
        element.render(Vec2.EMPTY, consumer, null)
        element.guiRenderer.screen = GUIScreen(Vec2i(100, 100), Vec2(100, 100))
        assertTrue(element.render(Vec2.EMPTY, consumer, null))

        assertEquals(called, 2)
        assertEquals(consumer.caches, 2)
    }

    fun `don't render again after indirect invalidation`() {
        var called = 0
        val element = child { called++ }
        val consumer = TestCountGUIVertexConsumer()
        element.render(Vec2.EMPTY, consumer, null)
        element.render(Vec2(1.0f), consumer, null)
        assertFalse(element.render(Vec2(1.0f), consumer, null))

        assertEquals(called, 2)
        assertEquals(consumer.caches, 3)
    }

    fun `manual cache invalidation`() {
        var called = 0
        val element = child { called++ }
        val consumer = TestCountGUIVertexConsumer()
        element.render(Vec2.EMPTY, consumer, null)
        element.cache.invalidate()
        assertTrue(element.render(Vec2.EMPTY, consumer, null))

        assertEquals(called, 2)
        assertEquals(consumer.caches, 2)
    }

    fun `initial disabled cache`() {
        var called = 0
        val element = child { called++ }
        val consumer = TestCountGUIVertexConsumer()
        element.cache.disable()
        assertTrue(element.render(Vec2.EMPTY, consumer, null))
        assertTrue(element.render(Vec2.EMPTY, consumer, null))

        assertEquals(called, 2)
        assertEquals(consumer.caches, 0)
    }

    fun `post disabled cache`() {
        var called = 0
        val element = child { called++ }
        val consumer = TestCountGUIVertexConsumer()
        assertTrue(element.render(Vec2.EMPTY, consumer, null))
        element.cache.disable()
        assertTrue(element.render(Vec2.EMPTY, consumer, null))

        assertEquals(called, 2)
        assertEquals(consumer.caches, 1)
    }

    fun `initial disabled cache and enabled initial`() {
        var called = 0
        val element = child { called++ }
        val consumer = TestCountGUIVertexConsumer()
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
        val element = child { called++ }
        val consumer = TestCountGUIVertexConsumer()
        element.cache.disable()
        assertTrue(element.render(Vec2.EMPTY, consumer, null))
        element.cache.enable()
        assertTrue(element.render(Vec2.EMPTY, consumer, null))

        assertEquals(called, 2)
        assertEquals(consumer.caches, 1)
    }

    fun `simple children cache`() {
        var parent = 0
        var child = 0
        val childE = child { child++ }
        val parentE = parent { parent++ }
        childE.parent = parentE

        val consumer = TestCountGUIVertexConsumer()

        assertTrue(parentE.render(Vec2.EMPTY, consumer, null))
        assertEquals(parent, 1)
        assertEquals(child, 1)
        assertEquals(consumer.caches, 1)

        assertFalse(parentE.render(Vec2.EMPTY, consumer, null))
        assertEquals(parent, 1)
        assertEquals(child, 1)
        assertEquals(consumer.caches, 2)
    }

    fun `invalidate child children cache`() {
        var parent = 0
        var child = 0
        val childE = child { child++ }
        val parentE = parent { parent++ }
        childE.parent = parentE

        val consumer = TestCountGUIVertexConsumer()

        assertTrue(parentE.render(Vec2.EMPTY, consumer, null))
        assertEquals(parent, 1)
        assertEquals(child, 1)
        assertEquals(consumer.caches, 1)

        childE.cache.invalidate()

        assertTrue(parentE.render(Vec2.EMPTY, consumer, null))
        assertEquals(parent, 2)
        assertEquals(child, 2)
        assertEquals(consumer.caches, 2)
    }

    fun `initial disabled child children cache`() {
        val child = child { }
        child.cache.disable()
        val parent = parent { }
        child.parent = parent

        assertFalse(parent.cache.enabled())
    }

    fun `disabled child children cache`() {
        val child = child { }
        val parent = parent { }
        child.parent = parent

        child.cache.disable()
        assertFalse(parent.cache.enabled())
    }

    fun `initial disable and enable child children cache`() {
        val child = child { }
        child.cache.disable()
        val parent = parent { }
        child.parent = parent

        child.cache.enable()

        assertTrue(parent.cache.enabled())
    }

    fun `disable and enable child children cache`() {
        val child = child { }
        val parent = parent { }
        child.parent = parent

        child.cache.disable()
        child.cache.enable()

        assertTrue(parent.cache.enabled())
    }


    fun `one child enabled other cache disabled`() {
        val child1 = child { }
        val child2 = child { }
        val parent = parent { }
        child1.parent = parent
        child2.parent = parent

        child1.cache.disable()

        assertFalse(child1.cache.enabled())
        assertTrue(child2.cache.enabled())

        assertFalse(parent.cache.enabled())
    }

    fun `both children disabled`() {
        val child1 = child { }
        val child2 = child { }
        val parent = parent { }
        child1.parent = parent
        child2.parent = parent

        child1.cache.disable()
        child2.cache.disable()

        assertFalse(child1.cache.enabled())
        assertFalse(child2.cache.enabled())

        assertFalse(parent.cache.enabled())
    }

    fun `both children disabled, enable one`() {
        val child1 = child { }
        val child2 = child { }
        val parent = parent { }
        child1.parent = parent
        child2.parent = parent

        child1.cache.disable()
        child2.cache.disable()
        child1.cache.enable()

        assertTrue(child1.cache.enabled())
        assertFalse(child2.cache.enabled())

        assertFalse(parent.cache.enabled())
    }

    fun `both children disabled, enable both`() {
        val child1 = child { }
        val child2 = child { }
        val parent = parent { }
        child1.parent = parent
        child2.parent = parent

        child1.cache.disable()
        child2.cache.disable()
        child1.cache.enable()
        child2.cache.enable()

        assertTrue(child1.cache.enabled())
        assertTrue(child2.cache.enabled())

        assertTrue(parent.cache.enabled())
    }

    private fun child(callback: () -> Unit): Element {
        return object : Element(GuiRenderTestUtil.create()) {
            override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
                callback()
            }

            override fun forceSilentApply() {
                TODO("Not yet implemented")
            }

        }
    }

    private fun parent(callback: () -> Unit): Element {
        return object : Element(GuiRenderTestUtil.create()), ChildedElement {
            override val children = SimpleChildrenManager(this)

            override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
                callback()
                for (child in children) {
                    child.render(offset, consumer, options)
                }
            }

            override fun forceSilentApply() {
                TODO("Not yet implemented")
            }

            override fun update(child: Element) {
                TODO("Not yet implemented")
            }
        }
    }
}
