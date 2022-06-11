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
package de.bixilon.minosoft.data.registries.items

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.Rarities
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.language.Translatable
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.items.armor.*
import de.bixilon.minosoft.data.registries.items.arrow.ArrowItem
import de.bixilon.minosoft.data.registries.items.arrow.SpectralArrowItem
import de.bixilon.minosoft.data.registries.items.arrow.TippedArrowItem
import de.bixilon.minosoft.data.registries.items.block.*
import de.bixilon.minosoft.data.registries.items.book.BookItem
import de.bixilon.minosoft.data.registries.items.book.EnchantedBookItem
import de.bixilon.minosoft.data.registries.items.book.WritableBookItem
import de.bixilon.minosoft.data.registries.items.book.WrittenBookItem
import de.bixilon.minosoft.data.registries.items.bucket.BucketItem
import de.bixilon.minosoft.data.registries.items.bucket.EntityBucketItem
import de.bixilon.minosoft.data.registries.items.bucket.MilkBucketItem
import de.bixilon.minosoft.data.registries.items.map.EmptyMapItem
import de.bixilon.minosoft.data.registries.items.map.FilledMapItem
import de.bixilon.minosoft.data.registries.items.throwable.*
import de.bixilon.minosoft.data.registries.items.throwable.potion.LingeringPotionItem
import de.bixilon.minosoft.data.registries.items.throwable.potion.PotionItem
import de.bixilon.minosoft.data.registries.items.tools.*
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registries.registry.codec.ResourceLocationCodec
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.gui.rendering.camera.target.targets.EntityTarget
import de.bixilon.minosoft.gui.rendering.input.interaction.InteractionResults
import de.bixilon.minosoft.gui.rendering.models.baked.item.BakedItemModel
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation

open class Item(
    override val resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : RegistryItem(), Translatable {
    val rarity: Rarities = data["rarity"]?.toInt()?.let { Rarities[it] } ?: Rarities.COMMON
    val maxStackSize: Int = data["max_stack_size"]?.toInt() ?: 64
    val maxDurability: Int = data["max_damage"]?.toInt() ?: 1
    val isFireResistant: Boolean = data["is_fire_resistant"]?.toBoolean() ?: false
    override val translationKey: ResourceLocation? = data["translation_key"]?.toResourceLocation()

    open var model: BakedItemModel? = null
    var tintProvider: TintProvider? = null

    override fun toString(): String {
        return resourceLocation.toString()
    }

    open fun getMiningSpeedMultiplier(connection: PlayConnection, blockState: BlockState, stack: ItemStack): Float {
        return 1.0f
    }

    open fun interactBlock(connection: PlayConnection, target: BlockTarget, hand: Hands, stack: ItemStack): InteractionResults {
        return InteractionResults.PASS
    }

    open fun interactEntity(connection: PlayConnection, target: EntityTarget, hand: Hands, stack: ItemStack): InteractionResults {
        return InteractionResults.PASS
    }

    open fun interactEntityAt(connection: PlayConnection, target: EntityTarget, hand: Hands, stack: ItemStack): InteractionResults {
        return InteractionResults.PASS
    }

    open fun interactItem(connection: PlayConnection, hand: Hands, stack: ItemStack): InteractionResults {
        return InteractionResults.PASS
    }

    companion object : ResourceLocationCodec<Item> {
        const val INFINITE_MINING_SPEED_MULTIPLIER = -1.0f

        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): Item {
            check(registries != null) { "Registries is null!" }
            if (data["food_properties"] != null) {
                return FoodItem(resourceLocation, registries, data)
            }
            return when (val `class` = data["class"].unsafeCast<String>()) {
                "BlockItem" -> BlockItem(resourceLocation, registries, data)
                "AliasedBlockItem" -> AliasedBlockItem(resourceLocation, registries, data)
                "Item", "AirBlockItem" -> Item(resourceLocation, registries, data)
                "ArmorItem" -> ArmorItem(resourceLocation, registries, data)
                "SwordItem" -> SwordItem(resourceLocation, registries, data)
                "ToolItem" -> ToolItem(resourceLocation, registries, data)
                "AxeItem" -> AxeItem(resourceLocation, registries, data)
                "BucketItem" -> BucketItem(resourceLocation, registries, data)
                "DyeItem" -> DyeItem(resourceLocation, registries, data)
                "HorseArmorItem" -> HorseArmorItem(resourceLocation, registries, data)
                "SpawnEggItem" -> SpawnEggItem(resourceLocation, registries, data)
                "MusicDiscItem" -> MusicDiscItem(resourceLocation, registries, data)
                "ShovelItem" -> ShovelItem(resourceLocation, registries, data)
                "PickaxeItem" -> PickaxeItem(resourceLocation, registries, data)
                "HoeItem" -> HoeItem(resourceLocation, registries, data)
                "DyeableArmorItem" -> DyeableArmorItem(resourceLocation, registries, data)
                "TallBlockItem" -> TallBlockItem(resourceLocation, registries, data)
                "WallStandingBlockItem" -> WallStandingBlockItem(resourceLocation, registries, data)
                "LilyPadItem" -> LilyPadItem(resourceLocation, registries, data)
                "CommandBlockItem" -> CommandBlockItem(resourceLocation, registries, data)
                "ScaffoldingItem" -> ScaffoldingItem(resourceLocation, registries, data)
                "SaddleItem" -> SaddleItem(resourceLocation, registries, data)
                "MinecartItem" -> MinecartItem(resourceLocation, registries, data)
                "OnAStickItem" -> OnAStickItem(resourceLocation, registries, data)
                "ElytraItem" -> ElytraItem(resourceLocation, registries, data)
                "BoatItem" -> BoatItem(resourceLocation, registries, data)
                "FlintAndSteelItem" -> FlintAndSteelItem(resourceLocation, registries, data)
                "BowItem" -> BowItem(resourceLocation, registries, data)
                "ArrowItem" -> ArrowItem(resourceLocation, registries, data)
                "MushroomStewItem" -> MushroomStewItem(resourceLocation, registries, data)
                "DecorationItem" -> DecorationItem(resourceLocation, registries, data)
                "EnchantedGoldenAppleItem" -> EnchantedGoldenAppleItem(resourceLocation, registries, data)
                "SignItem" -> SignItem(resourceLocation, registries, data)
                "PowderSnowBucketItem" -> PowderSnowBucketItem(resourceLocation, registries, data)
                "SnowballItem" -> SnowballItem(resourceLocation, registries, data)
                "MilkBucketItem" -> MilkBucketItem(resourceLocation, registries, data)
                "FishBucketItem", "EntityBucketItem" -> EntityBucketItem(resourceLocation, registries, data)
                "BookItem" -> BookItem(resourceLocation, registries, data)
                "EggItem" -> EggItem(resourceLocation, registries, data)
                "CompassItem" -> CompassItem(resourceLocation, registries, data)
                "BundleItem" -> BundleItem(resourceLocation, registries, data)
                "FishingRodItem" -> FishingRodItem(resourceLocation, registries, data)
                "SpyglassItem" -> SpyglassItem(resourceLocation, registries, data)
                "BoneMealItem" -> BoneMealItem(resourceLocation, registries, data)
                "BedItem" -> BedItem(resourceLocation, registries, data)
                "FilledMapItem" -> FilledMapItem(resourceLocation, registries, data)
                "ShearsItem" -> ShearsItem(resourceLocation, registries, data)
                "EnderPearlItem" -> EnderPearlItem(resourceLocation, registries, data)
                "PotionItem" -> PotionItem(resourceLocation, registries, data)
                "GlassBottleItem" -> GlassBottleItem(resourceLocation, registries, data)
                "EnderEyeItem" -> EnderEyeItem(resourceLocation, registries, data)
                "ExperienceBottleItem" -> ExperienceBottleItem(resourceLocation, registries, data)
                "FireChargeItem" -> FireChargeItem(resourceLocation, registries, data)
                "WritableBookItem" -> WritableBookItem(resourceLocation, registries, data)
                "WrittenBookItem" -> WrittenBookItem(resourceLocation, registries, data)
                "ItemFrameItem" -> ItemFrameItem(resourceLocation, registries, data)
                "EmptyMapItem" -> EmptyMapItem(resourceLocation, registries, data)
                "SkullItem" -> SkullItem(resourceLocation, registries, data)
                "NetherStarItem" -> NetherStarItem(resourceLocation, registries, data)
                "FireworkItem" -> FireworkItem(resourceLocation, registries, data)
                "FireworkChargeItem", "FireworkRocketItem" -> FireworkChargeItem(resourceLocation, registries, data)
                "EnchantedBookItem" -> EnchantedBookItem(resourceLocation, registries, data)
                "ArmorStandItem" -> ArmorStandItem(resourceLocation, registries, data)
                "DyeableHorseArmorItem" -> DyeableHorseArmorItem(resourceLocation, registries, data)
                "LeadItem" -> LeadItem(resourceLocation, registries, data)
                "NameTagItem" -> NameTagItem(resourceLocation, registries, data)
                "BannerItem" -> BannerItem(resourceLocation, registries, data)
                "EndCrystalItem" -> EndCrystalItem(resourceLocation, registries, data)
                "ChorusFruitItem" -> ChorusFruitItem(resourceLocation, registries, data)
                "SplashPotionItem" -> SplashPotionItem(resourceLocation, registries, data)
                "SpectralArrowItem" -> SpectralArrowItem(resourceLocation, registries, data)
                "TippedArrowItem" -> TippedArrowItem(resourceLocation, registries, data)
                "LingeringPotionItem" -> LingeringPotionItem(resourceLocation, registries, data)
                "ShieldItem" -> ShieldItem(resourceLocation, registries, data)
                "KnowledgeBookItem" -> KnowledgeBookItem(resourceLocation, registries, data)
                "DebugStickItem" -> DebugStickItem(resourceLocation, registries, data)
                "TridentItem" -> TridentItem(resourceLocation, registries, data)
                "CrossbowItem" -> CrossbowItem(resourceLocation, registries, data)
                "SuspiciousStewItem" -> SuspiciousStewItem(resourceLocation, registries, data)
                "BannerPatternItem" -> BannerPatternItem(resourceLocation, registries, data)
                "HoneycombItem" -> HoneycombItem(resourceLocation, registries, data)
                "HoneyBottleItem" -> HoneyBottleItem(resourceLocation, registries, data)
//                else -> TODO("Can not find item class (resourceLocation=$resourceLocation, $`class`)")
                else -> Item(resourceLocation, registries, data)
            }
        }
    }
}
