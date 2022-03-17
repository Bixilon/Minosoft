/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.registries.entities

import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.EvokerFangs
import de.bixilon.minosoft.data.entities.entities.*
import de.bixilon.minosoft.data.entities.entities.ambient.Bat
import de.bixilon.minosoft.data.entities.entities.animal.*
import de.bixilon.minosoft.data.entities.entities.animal.hoglin.Hoglin
import de.bixilon.minosoft.data.entities.entities.animal.horse.*
import de.bixilon.minosoft.data.entities.entities.animal.water.*
import de.bixilon.minosoft.data.entities.entities.boss.enderdragon.EndCrystal
import de.bixilon.minosoft.data.entities.entities.boss.enderdragon.EnderDragon
import de.bixilon.minosoft.data.entities.entities.boss.wither.WitherBoss
import de.bixilon.minosoft.data.entities.entities.decoration.*
import de.bixilon.minosoft.data.entities.entities.item.FallingBlockEntity
import de.bixilon.minosoft.data.entities.entities.item.ItemEntity
import de.bixilon.minosoft.data.entities.entities.item.PrimedTNT
import de.bixilon.minosoft.data.entities.entities.monster.*
import de.bixilon.minosoft.data.entities.entities.monster.piglin.Piglin
import de.bixilon.minosoft.data.entities.entities.monster.piglin.PiglinBrute
import de.bixilon.minosoft.data.entities.entities.monster.raid.*
import de.bixilon.minosoft.data.entities.entities.npc.villager.Villager
import de.bixilon.minosoft.data.entities.entities.npc.villager.WanderingTrader
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.data.entities.entities.projectile.*
import de.bixilon.minosoft.data.entities.entities.vehicle.*
import de.bixilon.minosoft.data.entities.meta.EntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.factory.DefaultFactory
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.vec3.Vec3d

@SuppressWarnings("deprecation")
object DefaultEntityFactories : DefaultFactory<EntityFactory<*>>(
    AreaEffectCloud,
    ArmorStand,
    Arrow,
    Axolotl,
    Bat,
    Bee,
    Blaze,
    Boat,
    Cat,
    CaveSpider,
    Chicken,
    Cod,
    Cow,
    Creeper,
    Dolphin,
    Donkey,
    DragonFireball,
    Drowned,
    ElderGuardian,
    EndCrystal,
    EnderDragon,
    Enderman,
    Endermite,
    Evoker,
    EvokerFangs,
    ExperienceOrb,
    ThrownEyeOfEnder,
    FallingBlockEntity,
    FireworkRocketEntity,
    Fox,
    Frog,
    Goat,
    Ghast,
    Giant,
    Guardian,
    Hoglin,
    Horse,
    Husk,
    Illusioner,
    IronGolem,
    ItemEntity,
    GlowItemFrame,
    ItemFrame,
    LargeFireball,
    LeashFenceKnotEntity,
    LightningBolt,
    Llama,
    LlamaSpit,
    MagmaCube,
    Marker,
    Minecart,
    ChestMinecart,
    CommandBlockMinecart,
    FurnaceMinecart,
    HopperMinecart,
    SpawnerMinecart,
    TNTMinecart,
    Mule,
    Mooshroom,
    Ocelot,
    Painting,
    Panda,
    Parrot,
    Phantom,
    Pig,
    Piglin,
    PiglinBrute,
    Pillager,
    PolarBear,
    PrimedTNT,
    PufferFish,
    Rabbit,
    Ravager,
    Salmon,
    Sheep,
    Shulker,
    ShulkerBullet,
    Silverfish,
    Skeleton,
    SkeletonHorse,
    Slime,
    SmallFireball,
    SnowGolem,
    ThrownSnowball,
    SpectralArrow,
    Spider,
    Squid,
    Stray,
    Strider,
    Tadpole,
    ThrownEgg,
    ThrownEnderPearl,
    ThrownExperienceBottle,
    ThrownPotion,
    ThrownTrident,
    TraderLlama,
    TropicalFish,
    Turtle,
    Vex,
    Villager,
    Vindicator,
    WanderingTrader,
    Witch,
    WitherBoss,
    WitherSkeleton,
    WitherSkull,
    Wolf,
    Zoglin,
    Zombie,
    ZombieHorse,
    ZombieVillager,
    ZombiePigman,
    ZombifiedPiglin,
    RemotePlayerEntity,
    FishingBobber,
    GlowSquid,
    EvokerFangs,
) {
    fun buildEntity(resourceLocation: ResourceLocation, connection: PlayConnection, position: Vec3d, rotation: EntityRotation, entityData: EntityData?, versionId: Int): Entity? {
        val factory = this[resourceLocation] ?: throw UnknownEntityException("Can not find entity type: $resourceLocation")
        return buildEntity(factory, connection, position, rotation, entityData, versionId)
    }

    fun buildEntity(factory: EntityFactory<out Entity>, connection: PlayConnection, position: Vec3d, rotation: EntityRotation, entityData: EntityData?, versionId: Int): Entity? {
        val tweakedResourceLocation = factory.tweak(connection, entityData, versionId)

        val tweakedFactory = this[tweakedResourceLocation] ?: throw UnknownEntityException("Can not find tweaked entity type: $tweakedResourceLocation for $factory")

        val tweakedEntityType = connection.registries.entityTypeRegistry[tweakedResourceLocation] ?: throw UnknownEntityException("Can not find tweaked entity type data in ${connection.version}: $tweakedResourceLocation for $factory")
        return tweakedFactory.build(connection, tweakedEntityType, position, rotation)
    }
}
