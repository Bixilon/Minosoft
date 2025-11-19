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

package de.bixilon.minosoft.data.registries.blocks

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.entities.block.TestBlockEntities
import de.bixilon.minosoft.data.registries.blocks.light.LightProperties
import de.bixilon.minosoft.data.registries.blocks.light.OpaqueProperty
import de.bixilon.minosoft.data.registries.blocks.light.TransparentProperty
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.state.manager.SingleStateManager
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.entity.BlockWithEntity
import de.bixilon.minosoft.data.registries.blocks.types.properties.FullBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.TestBlockModels
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.versions.TestVersions
import de.bixilon.minosoft.protocol.versions.Version

object TestBlocks {
    val TEST1 = TestBlock(minecraft("test1"))
    val TEST2 = TestBlock(minecraft("test2"))
    val TEST3 = TestBlock(minecraft("test3"))

    val MODEL1 = TestBlock(minecraft("model1")).apply { states.default.model = TestBlockModels.TEST1 }
    val MODEL2 = TestBlock(minecraft("model2")).apply { states.default.model = TestBlockModels.TEST2 }
    val MODEL3 = TestBlock(minecraft("model3")).apply { states.default.model = TestBlockModels.TEST3 }


    val OPAQUE1: TestBlock = object : TestBlock(minecraft("opaque1")), FullBlock {
        override val lightProperties get() = OpaqueProperty
    }
    val OPAQUE2: TestBlock = object : TestBlock(minecraft("opaque2")), FullBlock {
        override val lightProperties get() = OpaqueProperty
    }
    val OPAQUE3: TestBlock = object : TestBlock(minecraft("opaque3")), FullBlock {
        override val lightProperties get() = OpaqueProperty
    }


    val ENTITY1: TestBlock = object : TestBlock(minecraft("entity1")), BlockWithEntity<TestBlockEntities.TestBlockEntity> {
        override fun createBlockEntity(session: PlaySession, position: BlockPosition, state: BlockState) = TestBlockEntities.TestBlockEntity(session, position, state)
    }
    val ENTITY2: TestBlock = object : TestBlock(minecraft("entity2")), BlockWithEntity<TestBlockEntities.TestBlockEntity> {
        override fun createBlockEntity(session: PlaySession, position: BlockPosition, state: BlockState) = TestBlockEntities.TestBlockEntity(session, position, state)
    }


    val TORCH14: TestBlock = object : TestBlock(minecraft("light14")) {
        override val lightProperties get() = TransparentProperty

        override fun buildState(version: Version, settings: BlockStateBuilder) = settings.build(this, luminance = 14)
    }

    open class TestBlock(identifier: ResourceLocation) : Block(identifier, BlockSettings(TestVersions.TEST)) {
        override val hardness get() = 1.0f

        override val lightProperties: LightProperties? get() = TransparentProperty

        init {
            this::states.forceSet(SingleStateManager(buildState(TestVersions.TEST, BlockStateBuilder(emptyMap(), 0, null, null, lightProperties!!, flags))))
        }
    }
}
