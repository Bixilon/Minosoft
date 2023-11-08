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

package de.bixilon.minosoft.gui.rendering.entities.feature.hitbox

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.gui.rendering.entities.EntityRendererTestUtil.create
import de.bixilon.minosoft.gui.rendering.entities.EntityRendererTestUtil.isInvisible
import de.bixilon.minosoft.gui.rendering.input.key.manager.InputManager
import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import de.bixilon.minosoft.util.KUtil.startInit
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["entities", "rendering"])
class HitboxFeatureTest {
    private val mesh = HitboxFeature::class.java.getDeclaredField("mesh").apply { isAccessible = true }

    val HitboxFeature.mesh: LineMesh? get() = this@HitboxFeatureTest.mesh.get(this).unsafeCast()

    private fun create(entity: EntityFactory<*>): HitboxFeature {
        val renderer = create().create(entity)
        renderer::hitbox.forceSet(null) // remove
        renderer.entity.startInit()
        renderer.entity.draw(0L)
        renderer.renderer.context::input.forceSet(InputManager(renderer.renderer.context))
        renderer.renderer.features.hitbox.init() // register listeners

        return HitboxFeature(renderer)
    }

    fun `create simple hitbox`() {
        val hitbox = create(RemotePlayerEntity)
        hitbox.update(0, 0.0f)
        assertNotNull(hitbox.mesh)
    }

    fun `unload if entity is invisible`() {
        val hitbox = create(RemotePlayerEntity)
        hitbox.update(0, 0.0f)
        hitbox.renderer.entity.isInvisible(true)
        hitbox.update(0, 0.0f)
        assertNull(hitbox.mesh)
    }

    fun `entity is invisible but invisibles are shown`() {
        val hitbox = create(RemotePlayerEntity)
        hitbox.update(0, 0.0f)
        hitbox.renderer.entity.isInvisible(true)
        hitbox.renderer.renderer.profile.features.hitbox.showInvisible = true
        hitbox.update(0, 0.0f)
        assertNotNull(hitbox.mesh)
    }

    fun `profile disabled`() {
        val hitbox = create(RemotePlayerEntity)
        hitbox.renderer.renderer.profile.features.hitbox.enabled = false
        hitbox.update(0, 0.0f)
        assertNull(hitbox.mesh)
    }

    fun `don't update hitbox if unchanged`() {
        val hitbox = create(RemotePlayerEntity)
        hitbox.update(100, 1.0f)
        val mesh = hitbox.mesh
        hitbox.update(100, 1.0f)
        assertSame(mesh, hitbox.mesh)
    }

    fun `update hitbox if entity moved`() {
        val hitbox = create(RemotePlayerEntity)
        hitbox.update(0, 0.0f)
        val mesh = hitbox.mesh
        hitbox.renderer.entity.physics.forceMove(Vec3d(0.5))
        hitbox.renderer.entity.draw(1000)
        hitbox.update(1000, 1.0f)
        assertNotSame(mesh, hitbox.mesh)
    }

    // TODO: velocity, correct size, direction, (eye height), lazy
    // TODO: visibility, interpolation
}
