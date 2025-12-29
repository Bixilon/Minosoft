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

package de.bixilon.minosoft.data.registries.blocks.types.portal

import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.light.TransparentProperty
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties.register
import de.bixilon.minosoft.data.registries.blocks.properties.EnumProperty
import de.bixilon.minosoft.data.registries.blocks.properties.list.MapPropertyList
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.CollisionContext
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EntityCollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.legacy.FlatteningRenamedModel
import de.bixilon.minosoft.data.registries.blocks.types.properties.hardness.UnbreakableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.rendering.RandomDisplayTickable
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.block.climbing.ClimbingItems
import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.PortalParticle
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.versions.Version
import java.util.*

open class NetherPortalBlock(identifier: ResourceLocation = NetherPortalBlock.identifier, settings: BlockSettings) : Block(identifier, settings), UnbreakableBlock, FlatteningRenamedModel, OutlinedBlock, RandomDisplayTickable, BlockWithItem<Item> {
    override val item: Item = this::item.inject(this.identifier)
    val particle: ParticleType = this::particle.inject(PortalParticle)

    override val lightProperties get() = TransparentProperty
    override val legacyModelName get() = LEGACY_MODEL

    override fun registerProperties(version: Version, list: MapPropertyList) {
        super.registerProperties(version, list)
        list += AXIS
    }

    override fun buildState(version: Version, settings: BlockStateBuilder): BlockState {
        val axis = settings.properties[AXIS]
        val shape = when (axis) {
            Axes.X -> SHAPE_X
            Axes.Z -> SHAPE_Z
            else -> Broken("impossible")
        }
        return settings.build(this.unsafeCast(), outlineShape = shape)
    }

    override fun randomDisplayTick(session: PlaySession, state: BlockState, position: BlockPosition, random: Random) {
        val particle = session.world.particle ?: return
        if (this.particle == null) return

        for (i in 0 until 4) {
            val particlePosition = MVec3d(
                position.x + random.nextDouble(),
                position.y + random.nextDouble(),
                position.z + random.nextDouble(),
            )
            val velocity = MVec3d((random.nextDouble() - 0.5) * 0.5, (random.nextDouble() - 0.5) * 0.5, (random.nextDouble() - 0.5) * 0.5)

            val factor = (random.nextInt(2) * 2 + 1).toDouble()

            if (session.world[position + Directions.WEST]?.block != this && session.world[position + Directions.EAST]?.block != this) {
                particlePosition.x = position.x + 0.5 + 0.25 * factor
                velocity.x = random.nextDouble() * 2.0 * factor
            } else {
                particlePosition.z = position.z + 0.5 + 0.25 * factor
                velocity.z = random.nextDouble() * 2.0 * factor
            }
            particle += PortalParticle(
                session,
                particlePosition.unsafe,
                velocity,
                this.particle.default(),
            )
        }
    }

    companion object : BlockFactory<NetherPortalBlock> {
        override val identifier = minecraft("nether_portal")
        val LEGACY_MODEL = minecraft("portal")

        val AXIS = EnumProperty("axis", Axes, Axes.set(Axes.X, Axes.Z))


        private val SHAPE_X = AABB(0.0, 0.0, 0.375, 1.0, 1.0, 0.625)
        private val SHAPE_Z = AABB(0.375, 0.0, 0.0, 0.625, 1.0, 1.0)

        override fun build(registries: Registries, settings: BlockSettings) = NetherPortalBlock(settings = settings)
    }
}
