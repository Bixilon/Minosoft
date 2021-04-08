/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data

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
import de.bixilon.minosoft.data.entities.entities.item.FallingBlock
import de.bixilon.minosoft.data.entities.entities.item.ItemEntity
import de.bixilon.minosoft.data.entities.entities.item.PrimedTNT
import de.bixilon.minosoft.data.entities.entities.monster.*
import de.bixilon.minosoft.data.entities.entities.monster.piglin.Piglin
import de.bixilon.minosoft.data.entities.entities.monster.piglin.PiglinBrute
import de.bixilon.minosoft.data.entities.entities.monster.raid.*
import de.bixilon.minosoft.data.entities.entities.npc.villager.Villager
import de.bixilon.minosoft.data.entities.entities.npc.villager.WanderingTrader
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.projectile.*
import de.bixilon.minosoft.data.entities.entities.vehicle.*
import de.bixilon.minosoft.data.entities.meta.EntityMetaData
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.entities.EntityFactory
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3

object DefaultEntityFactories {
    @SuppressWarnings("deprecation")
    private val ENTITY_FACTORY_MAP: Map<ResourceLocation, EntityFactory<out Entity>>


    init {
        val entityFactories: List<EntityFactory<out Entity>> = listOf(
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
            FallingBlock,
            FireworkRocketEntity,
            Fox,
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
            PlayerEntity,
            FishingHook,
            GlowSquid,
            EvokerFangs
        )

        val ret: MutableMap<ResourceLocation, EntityFactory<out Entity>> = mutableMapOf()


        for (entityFactory in entityFactories) {
            ret[entityFactory.RESOURCE_LOCATION] = entityFactory
        }

        ENTITY_FACTORY_MAP = ret.toMap()
    }

    fun getEntityFactory(resourceLocation: ResourceLocation): EntityFactory<out Entity>? {
        return ENTITY_FACTORY_MAP[resourceLocation]
    }

    fun buildEntity(resourceLocation: ResourceLocation, connection: PlayConnection, position: Vec3, rotation: EntityRotation, entityMetaData: EntityMetaData?, versionId: Int): Entity? {
        val factory = getEntityFactory(resourceLocation) ?: throw UnknownEntityException("Can not find entity type: $resourceLocation")
        return buildEntity(factory, connection, position, rotation, entityMetaData, versionId)
    }

    fun buildEntity(factory: EntityFactory<out Entity>, connection: PlayConnection, position: Vec3, rotation: EntityRotation, entityMetaData: EntityMetaData?, versionId: Int): Entity? {
        val tweakedResourceLocation = factory.tweak(connection, entityMetaData, versionId)

        val tweakedFactory = ENTITY_FACTORY_MAP[tweakedResourceLocation] ?: throw UnknownEntityException("Can not find tweaked entity type: $tweakedResourceLocation for $factory")

        val tweakedEntityType = connection.mapping.entityRegistry.get(tweakedResourceLocation) ?: throw UnknownEntityException("Can not find tweaked entity type data in ${connection.version}: $tweakedResourceLocation for $factory")
        return tweakedFactory.build(connection, tweakedEntityType, position, rotation)
    }

}
