/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.data.DefaultEntityFactories
import de.bixilon.minosoft.data.entities.entities.AreaEffectCloud
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.boss.enderdragon.EndCrystal
import de.bixilon.minosoft.data.entities.entities.decoration.ArmorStand
import de.bixilon.minosoft.data.entities.entities.decoration.ItemFrame
import de.bixilon.minosoft.data.entities.entities.decoration.LeashFenceKnotEntity
import de.bixilon.minosoft.data.entities.entities.item.FallingBlock
import de.bixilon.minosoft.data.entities.entities.item.ItemEntity
import de.bixilon.minosoft.data.entities.entities.item.PrimedTNT
import de.bixilon.minosoft.data.entities.entities.projectile.*
import de.bixilon.minosoft.data.entities.entities.vehicle.Boat
import de.bixilon.minosoft.data.entities.entities.vehicle.Minecart
import de.bixilon.minosoft.data.entities.meta.EntityMetaData
import de.bixilon.minosoft.data.mappings.entities.EntityFactory
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3

@Deprecated(message = "We will use json for this soon")
enum class EntityObjects(
    val id: Int,
    val factory: EntityFactory<out Entity>,
) {
    BOAT(1, Boat),
    ITEM_STACK(2, ItemEntity),
    AREA_EFFECT_CLOUD(3, AreaEffectCloud),
    MINECART(10, Minecart),
    PRIMED_TNT(50, PrimedTNT),
    ENDER_CRYSTAL(51, EndCrystal),
    ARROW(60, Arrow),  // ToDo: Tipped Arrows
    SNOWBALL(61, ThrownSnowball),
    EGG(62, ThrownEgg),
    FIREBALL(63, LargeFireball),
    FIRE_CHARGE(64, SmallFireball),
    ENDER_PEARL(65, ThrownEnderPearl),
    WITHER_SKULL(66, WitherSkull),
    SHULKER_BULLET(67, ShulkerBullet),
    LLAMA_SPIT(67, LlamaSpit),
    FALLING_BLOCK(70, FallingBlock),
    ITEM_FRAME(71, ItemFrame),
    EYE_OF_ENDER(72, ThrownEyeOfEnder),
    THROWN_POTION(73, ThrownPotion),

    // FALLING_DRAGON_EGG(74, FallingDragonEgg.class),
    THROWN_EXP_BOTTLE(75, ThrownExperienceBottle),
    FIREWORK(76, FireworkRocketEntity),
    LEASH_KNOT(77, LeashFenceKnotEntity),
    ARMOR_STAND(78, ArmorStand),
    EVOKER_FANGS(78, EvokerFangs),
    FISHING_HOOK(90, FishingHook),
    SPECTRAL_ARROW(91, SpectralArrow),
    DRAGON_FIREBALL(93, DragonFireball),
    TRIDENT(94, ThrownTrident);


    fun build(connection: PlayConnection, position: Vec3, rotation: EntityRotation, entityMetaData: EntityMetaData?, versionId: Int): Entity? {
        return DefaultEntityFactories.buildEntity(factory, connection, position, rotation, entityMetaData, versionId)
    }

    companion object {
        private val ID_OBJECT_MAP = HashBiMap.create<Int, EntityObjects>()

        @JvmStatic
        fun byId(id: Int): EntityObjects? {
            return ID_OBJECT_MAP[id]
        }

        init {
            for (value in values()) {
                ID_OBJECT_MAP[value.id] = value
            }
        }
    }

}
