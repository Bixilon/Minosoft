package de.bixilon.minosoft.data.entities.entities.decoration

import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.entities.EntityFactory
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3d

class Marker(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : LivingEntity(connection, entityType, position, rotation) {
    companion object : EntityFactory<ItemFrame> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("marker")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): ItemFrame {
            return ItemFrame(connection, entityType, position, rotation)
        }
    }
}
