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

package de.bixilon.minosoft.gui.rendering.entities.visibility

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.animal.Pig
import de.bixilon.minosoft.gui.rendering.entities.EntityRendererTestUtil
import de.bixilon.minosoft.gui.rendering.entities.feature.EntityRenderFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["entities", "rendering"])
class VisibilityManagerTest {

    private fun create(): VisibilityManager {
        val renderer = EntityRendererTestUtil.create()
        return VisibilityManager(renderer)
    }

    private fun VisibilityManager.entity(): Entity {
        return Pig(renderer.connection, EntityRendererTestUtil.PIG, EntityData(renderer.connection, Int2ObjectOpenHashMap()), Vec3d(), EntityRotation.EMPTY)
    }

    private fun VisibilityManager.feature(layer: EntityLayer, priority: Int = 0, distance: Double = 0.0) = object : EntityRenderer<Entity>(this.renderer, entity()) {

        val f = object : EntityRenderFeature(this) {
            override val layer = layer
            override val priority = priority

            override fun draw() = Unit
        }

        init {
            features.clear()
            features += f
            this.distance = distance
        }

        override fun toString(): String {
            return "feature"
        }
    }

    fun `no features`() {
        val visibility = create()
        visibility.finish()
        assertEquals(visibility.opaque.size, 0)
        assertEquals(visibility.translucent.size, 0)
    }

    fun `single opaque features`() {
        val visibility = create()
        val feature = visibility.feature(EntityLayer.Opaque)
        visibility.collect(feature)
        visibility.finish()
        assertEquals(visibility.opaque, listOf(feature.f))
        assertEquals(visibility.translucent.size, 0)
    }

    fun `single translucent features`() {
        val visibility = create()
        val feature = visibility.feature(EntityLayer.Translucent)
        visibility.collect(feature)
        visibility.finish()
        assertEquals(visibility.opaque.size, 0)
        assertEquals(visibility.translucent, listOf(feature.f))
    }

    fun `two opaque features, sorted by distance`() {
        val visibility = create()
        val a = visibility.feature(EntityLayer.Opaque, priority = 0, 0.0)
        val b = visibility.feature(EntityLayer.Opaque, priority = 0, 1.0)
        visibility.collect(a); visibility.collect(b)
        visibility.finish()
        assertEquals(visibility.opaque, listOf(a.f, b.f))
    }

    fun `two translucent features, sorted by distance`() {
        val visibility = create()
        val a = visibility.feature(EntityLayer.Translucent, priority = 0, 0.0)
        val b = visibility.feature(EntityLayer.Translucent, priority = 0, 1.0)
        visibility.collect(a); visibility.collect(b)
        visibility.finish()
        assertEquals(visibility.translucent, listOf(b.f, a.f))
    }

    fun `two translucent features, sorted by type`() {
        val visibility = create()
        val a = visibility.feature(EntityLayer.Translucent, priority = 1, 0.0)
        val b = visibility.feature(EntityLayer.Translucent, priority = 0, 0.0)
        visibility.collect(a); visibility.collect(b)
        visibility.finish()
        assertEquals(visibility.translucent, listOf(b.f, a.f))
    }

    fun `sorted by type and distance`() {
        val visibility = create()
        val a = visibility.feature(EntityLayer.Opaque, priority = 1, 0.0)
        val b = visibility.feature(EntityLayer.Opaque, priority = 0, 0.0)
        val c = visibility.feature(EntityLayer.Opaque, priority = 0, 5.0)
        val d = visibility.feature(EntityLayer.Opaque, priority = 0, 2.0)
        val e = visibility.feature(EntityLayer.Opaque, priority = 1, 2.0)
        visibility.collect(a); visibility.collect(b); visibility.collect(c); visibility.collect(d); visibility.collect(e)
        visibility.finish()
        assertEquals(visibility.opaque, listOf(b.f, d.f, c.f, a.f, e.f))
    }
}
