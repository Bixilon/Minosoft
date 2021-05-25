package de.bixilon.minosoft.data.mappings.blocks.types

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.blocks.BlockUsages
import de.bixilon.minosoft.data.mappings.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.mappings.versions.Registries
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.gui.rendering.input.camera.RaycastHit
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3i

open class ComparatorBlock(resourceLocation: ResourceLocation, mappings: Registries, data: JsonObject) : AbstractRedstoneGateBlock(resourceLocation, mappings, data) {

    override fun getPlacementState(connection: PlayConnection, raycastHit: RaycastHit): BlockState? {
        TODO()
    }

    override fun use(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, raycastHit: RaycastHit, hands: Hands, itemStack: ItemStack?): BlockUsages {
        connection.world[blockPosition] = blockState.cycle(BlockProperties.STRUCTURE_BLOCK_MODE)

        return BlockUsages.SUCCESS
    }
}
