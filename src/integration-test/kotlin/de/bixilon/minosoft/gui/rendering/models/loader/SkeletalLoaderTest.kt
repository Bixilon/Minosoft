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

package de.bixilon.minosoft.gui.rendering.models.loader

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.assets.MemoryAssetsManager
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.loader.SkeletalLoader.Companion.sModel
import de.bixilon.minosoft.gui.rendering.skeletal.model.SkeletalModel
import de.bixilon.minosoft.gui.rendering.system.dummy.DummyRenderSystem
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTexture
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTextureManager
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil
import de.bixilon.minosoft.test.IT
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["rendering", "skeletal"])
class SkeletalLoaderTest {
    private val dummyModel = minosoft("model/dummy").sModel()


    private fun createContext(): RenderContext {
        val context = IT.OBJENESIS.newInstance(RenderContext::class.java)
        context::connection.forceSet(ConnectionTestUtil.createConnection())
        context::system.forceSet(DummyRenderSystem(context))
        context::textures.forceSet(DummyTextureManager(context))

        val manager = MemoryAssetsManager()
        manager.push(dummyModel, SkeletalLoaderTest::class.java.getResourceAsStream("/model/skeletal/dummy.smodel")!!.readAllBytes())
        context.connection.assetsManager.add(manager)


        return context
    }

    private fun createLoader(): SkeletalLoader {
        val context = createContext()
        val loader = ModelLoader(context)

        return loader.skeletal
    }

    private fun SkeletalLoader.loadDummyModel() {
        register(dummyModel, override = mapOf(minosoft("dummy_texture") to DummyTexture(), minosoft("second_texture") to DummyTexture()))
        load(SimpleLatch(0))
    }

    private fun SkeletalLoader.getRegisteredModel(name: ResourceLocation): SkeletalModel {
        val map = SkeletalLoader::class.java.getDeclaredField("registered").apply { isAccessible = true }.get(this).unsafeCast<Map<ResourceLocation, Any>>()
        val registered = map[name] ?: throw IllegalArgumentException("Can not find model!")
        return registered::class.java.getDeclaredField("model").apply { isAccessible = true }.get(registered).unsafeCast()
    }

    fun `load dummy model from file`() {
        val loader = createLoader()
        loader.loadDummyModel()
        val raw = loader.getRegisteredModel(dummyModel)

        assertEquals(raw.elements.size, 1)
        assertEquals(raw.elements["body"]!!.children["head1"]!!.transform, "head")
    }
}
