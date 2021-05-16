/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.block

import de.bixilon.minosoft.data.entities.block.container.*
import de.bixilon.minosoft.data.entities.block.container.storage.*
import de.bixilon.minosoft.data.entities.block.piston.PistonBlockEntity
import de.bixilon.minosoft.data.entities.block.piston.StickyPistonBlockEntity
import de.bixilon.minosoft.data.mappings.MultiResourceLocationAble
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.PlayConnection

object DefaultBlockEntityMetaDataFactory {
    private val BLOCK_ENTITY_FACTORY_MAP: Map<ResourceLocation, BlockEntityFactory<out BlockEntity>>


    init {
        val entityFactories: List<BlockEntityFactory<out BlockEntity>> = listOf(
            BedBlockEntity,
            HopperBlockEntity,
            SignBlockEntity,
            BlastFurnaceBlockEntity,
            FurnaceBlockEntity,
            CampfireBlockEntity,
            JigsawBlockEntity,
            LecternBlockEntity,
            BellBlockEntity,
            SmokerBlockEntity,
            ConduitBlockEntity,
            BarrelBlockEntity,
            ShulkerBoxBlockEntity,
            StructureBlockBlockEntity,
            CommandBlockBlockEntity,
            ComparatorBlockEntity,
            BannerBlockEntity,
            DaylightDetectorBlockEntity,
            BeaconBlockEntity,
            SkullBlockEntity,
            EnchantingTableBlockEntity,
            BrewingStandBlockEntity,
            MobSpawnerBlockEntity,
            DispenserBlockEntity,
            DropperBlockEntity,
            EnderChestBlockEntity,
            JukeboxBlockEntity,
            ChestBlockEntity,
            TrappedChestBlockEntity,
            BeehiveBlockEntity,
            NoteblockBlockEntity,
            EndGatewayBlockEntity,
            PistonBlockEntity,
            StickyPistonBlockEntity,
        )

        val ret: MutableMap<ResourceLocation, BlockEntityFactory<out BlockEntity>> = mutableMapOf()


        for (entityFactory in entityFactories) {
            ret[entityFactory.RESOURCE_LOCATION] = entityFactory
            if (entityFactory is MultiResourceLocationAble) {
                for (resourceLocation in entityFactory.ALIASES) {
                    ret[resourceLocation] = entityFactory
                }
            }
        }

        BLOCK_ENTITY_FACTORY_MAP = ret.toMap()
    }


    fun getEntityFactory(resourceLocation: ResourceLocation): BlockEntityFactory<out BlockEntity>? {
        return BLOCK_ENTITY_FACTORY_MAP[resourceLocation]
    }

    fun buildBlockEntity(factory: BlockEntityFactory<out BlockEntity>, connection: PlayConnection): BlockEntity {
        return factory.build(connection)
    }
}
