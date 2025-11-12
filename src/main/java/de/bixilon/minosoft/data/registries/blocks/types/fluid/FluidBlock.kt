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

package de.bixilon.minosoft.data.registries.blocks.types.fluid

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.registries.blocks.light.CustomLightProperties
import de.bixilon.minosoft.data.registries.blocks.properties.list.MapPropertyList
import de.bixilon.minosoft.data.registries.blocks.properties.primitives.IntProperty
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.LightedBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.rendering.RandomDisplayTickable
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.models.loader.legacy.CustomModel
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.gui.rendering.tint.TintedBlock
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.versions.Version
import java.util.*

abstract class FluidBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), FluidHolder, OutlinedBlock, LightedBlock, RandomDisplayTickable, CustomModel, TintedBlock {
    override val hardness: Float get() = Broken("Fluid is kind of unbreakable?")
    override val modelName: ResourceLocation? get() = null

    override var tintProvider: TintProvider? = null

    override fun initTint(manager: TintManager) {
        val tinted = fluid.nullCast<TintedBlock>() ?: return
        tinted.initTint(manager)
        this.tintProvider = tinted.tintProvider
    }

    override fun getOutlineShape(session: PlaySession, position: BlockPosition, state: BlockState): AABB {
        return AABB(Vec3d.EMPTY, Vec3d(1.0f, fluid.getHeight(state), 1.0f))
    }

    override val lightProperties get() = LIGHT_PROPERTIES

    override fun randomDisplayTick(session: PlaySession, state: BlockState, position: BlockPosition, random: Random) {
        fluid.randomTick(session, state, position, random)
    }

    override fun registerProperties(version: Version, list: MapPropertyList) {
        super.registerProperties(version, list)
        list += LEVEL
    }

    companion object {
        val LIGHT_PROPERTIES = CustomLightProperties(true, false, true)
        val LEVEL = IntProperty("level", 0..15)
    }
}
