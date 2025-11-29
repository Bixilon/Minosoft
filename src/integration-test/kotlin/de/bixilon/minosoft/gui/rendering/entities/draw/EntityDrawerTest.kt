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

package de.bixilon.minosoft.gui.rendering.entities.draw

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.animal.Pig
import de.bixilon.minosoft.gui.rendering.entities.EntityRendererTestUtil
import de.bixilon.minosoft.gui.rendering.entities.feature.FeatureDrawable
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entities.visibility.EntityLayer
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["entities", "rendering"])
class EntityDrawerTest {
    private val LAYERS = EntityDrawer::class.java.getFieldOrNull("layers")!!.field

    private fun create(): EntityDrawer {
        val renderer = EntityRendererTestUtil.create()
        return EntityDrawer(renderer)
    }

    private fun EntityDrawer.entity(): Entity {
        val entity = Pig(renderer.session, EntityRendererTestUtil.PIG, EntityData(renderer.session, Int2ObjectOpenHashMap()), Vec3d(), EntityRotation.EMPTY)
        entity.init()

        return entity
    }

    private fun EntityDrawer.feature(layer: EntityLayer, priority: Int = 0, distance: Double = 0.0) = object : EntityRenderer<Entity>(this.renderer, entity()) {

        val drawable = object : FeatureDrawable {
            override val layer = layer
            override val priority = priority
            override val sort get() = 0
            override val distance2 = distance

            override fun draw() = Unit
        }

        init {
            this.distance2 = distance
            collect(this@feature)
        }

        override fun collect(drawer: EntityDrawer) {
            drawer += drawable
        }

        override fun toString() = "feature"
    }

    fun `no features`() {
        val drawer = create()
        drawer.prepare()
        assertEquals(drawer[EntityLayer.Opaque].size, 0)
        assertEquals(drawer[EntityLayer.Translucent].size, 0)
    }

    fun `single opaque features`() {
        val drawer = create()
        val feature = drawer.feature(EntityLayer.Opaque)
        drawer.prepare()
        assertEquals(drawer[EntityLayer.Opaque], listOf(feature.drawable))
        assertEquals(drawer[EntityLayer.Translucent].size, 0)
    }

    fun `single translucent features`() {
        val drawer = create()
        val feature = drawer.feature(EntityLayer.Translucent)
        drawer.prepare()
        assertEquals(drawer[EntityLayer.Opaque].size, 0)
        assertEquals(drawer[EntityLayer.Translucent], listOf(feature.drawable))
    }

    fun `two opaque features, sorted by distance`() {
        val drawer = create()
        val a = drawer.feature(EntityLayer.Opaque, priority = 0, 0.0)
        val b = drawer.feature(EntityLayer.Opaque, priority = 0, 1.0)
        drawer.prepare()
        assertEquals(drawer[EntityLayer.Opaque], listOf(a.drawable, b.drawable))
    }

    fun `two translucent features, sorted by distance`() {
        val drawer = create()
        val a = drawer.feature(EntityLayer.Translucent, priority = 0, 0.0)
        val b = drawer.feature(EntityLayer.Translucent, priority = 0, 1.0)
        drawer.prepare()
        assertEquals(drawer[EntityLayer.Translucent], listOf(b.drawable, a.drawable))
    }

    fun `two translucent features, sorted by type`() {
        val drawer = create()
        val a = drawer.feature(EntityLayer.Translucent, priority = 1, 0.0)
        val b = drawer.feature(EntityLayer.Translucent, priority = 0, 0.0)
        drawer.prepare()
        assertEquals(drawer[EntityLayer.Translucent], listOf(b.drawable, a.drawable))
    }

    fun `sorted by type and distance`() {
        val drawer = create()
        val a = drawer.feature(EntityLayer.Opaque, priority = 1, 0.0)
        val b = drawer.feature(EntityLayer.Opaque, priority = 0, 0.0)
        val c = drawer.feature(EntityLayer.Opaque, priority = 0, 5.0)
        val d = drawer.feature(EntityLayer.Opaque, priority = 0, 2.0)
        val e = drawer.feature(EntityLayer.Opaque, priority = 1, 2.0)
        drawer.prepare()
        assertEquals(drawer[EntityLayer.Opaque], listOf(b.drawable, d.drawable, c.drawable, a.drawable, e.drawable))
    }

    // TODO: test if type is respected

    private operator fun EntityDrawer.get(layer: EntityLayer) = LAYERS.get<Map<EntityLayer, ArrayList<FeatureDrawable>>>(this)[layer] ?: emptyList()
}
