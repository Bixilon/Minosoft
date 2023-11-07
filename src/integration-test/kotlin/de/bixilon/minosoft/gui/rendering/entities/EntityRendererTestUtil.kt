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

package de.bixilon.minosoft.gui.rendering.entities

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.concurrent.queue.Queue
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.config.profile.profiles.entity.EntityProfile
import de.bixilon.minosoft.data.accounts.types.test.TestAccount
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.animal.Pig
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.additional.PlayerAdditional
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.local.SignatureKeyManagement
import de.bixilon.minosoft.data.entities.entities.player.tab.TabList
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.scoreboard.ScoreboardManager
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.entities.factory.DefaultEntityModels
import de.bixilon.minosoft.gui.rendering.entities.feature.register.EntityRenderFeatures
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.light.RenderLight
import de.bixilon.minosoft.gui.rendering.shader.ShaderManager
import de.bixilon.minosoft.gui.rendering.skeletal.SkeletalManager
import de.bixilon.minosoft.gui.rendering.system.dummy.DummyRenderSystem
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil
import de.bixilon.minosoft.test.IT
import java.util.*

object EntityRendererTestUtil {
    val PIG = EntityType(Pig.identifier, minosoft("key"), 1.0f, 1.0f, mapOf(), Pig, null)

    fun createContext(): RenderContext {
        val connection = ConnectionTestUtil.createConnection()
        connection::scoreboard.forceSet(ScoreboardManager(connection))
        connection::tabList.forceSet(TabList())
        val context = IT.OBJENESIS.newInstance(RenderContext::class.java)
        context::system.forceSet(DummyRenderSystem(context))
        context::textures.forceSet(context.system.createTextureManager())
        context::shaders.forceSet(ShaderManager(context))
        context::connection.forceSet(connection)
        context.connection::player.forceSet(LocalPlayerEntity(TestAccount, connection, SignatureKeyManagement(connection, connection.account)))
        context::camera.forceSet(Camera(context))
        context::light.forceSet(RenderLight(context))
        context::skeletal.forceSet(SkeletalManager(context))


        return context
    }

    fun create(): EntitiesRenderer {
        val context = createContext()
        val renderer = IT.OBJENESIS.newInstance(EntitiesRenderer::class.java)
        renderer::context.forceSet(context)
        renderer::queue.forceSet(Queue())
        renderer::connection.forceSet(context.connection)
        renderer::profile.forceSet(EntityProfile())
        renderer::features.forceSet(EntityRenderFeatures(renderer))
        renderer::renderers.forceSet(EntityRendererManager(renderer))
        return renderer
    }

    fun <E : Entity> EntitiesRenderer.createEntity(factory: EntityFactory<E>): E {
        val uuid = UUID(1L, 1L)
        if (factory == RemotePlayerEntity) {
            connection.tabList.uuid[uuid] = PlayerAdditional("John")
        }
        val renderer = DefaultEntityModels[factory.identifier]
        val type = PIG.copy(identifier = factory.identifier, modelFactory = renderer)
        return factory.build(connection, type, EntityData(connection), Vec3d(1, 1, 1), EntityRotation.EMPTY, uuid)!!
    }

    fun <E : Entity> EntitiesRenderer.create(factory: EntityFactory<E>): EntityRenderer<E> {
        val entity = createEntity(factory)
        this.renderers.add(entity)
        this.queue.work()
        return entity.renderer!!.unsafeCast()
    }
}
