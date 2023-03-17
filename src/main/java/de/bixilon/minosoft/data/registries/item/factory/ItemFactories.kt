/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.item.factory

import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.factory.DefaultFactory
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.integrated.IntegratedRegistry
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.armor.extra.ElytraItem
import de.bixilon.minosoft.data.registries.item.items.armor.extra.TurtleHelmet
import de.bixilon.minosoft.data.registries.item.items.armor.materials.*
import de.bixilon.minosoft.data.registries.item.items.block.climbing.ClimbingItems
import de.bixilon.minosoft.data.registries.item.items.bucket.BucketItem
import de.bixilon.minosoft.data.registries.item.items.bucket.FilledBucketItem
import de.bixilon.minosoft.data.registries.item.items.end.EnderEyeItem
import de.bixilon.minosoft.data.registries.item.items.end.EnderPealItem
import de.bixilon.minosoft.data.registries.item.items.entities.chicken.EggItem
import de.bixilon.minosoft.data.registries.item.items.fire.FireChargeItem
import de.bixilon.minosoft.data.registries.item.items.fire.FlintAndSteelItem
import de.bixilon.minosoft.data.registries.item.items.fishing.rod.OnAStickItem
import de.bixilon.minosoft.data.registries.item.items.food.AppleItem
import de.bixilon.minosoft.data.registries.item.items.potion.DrinkingPotionItem
import de.bixilon.minosoft.data.registries.item.items.potion.LingeringPotionItem
import de.bixilon.minosoft.data.registries.item.items.potion.SplashPotionItem
import de.bixilon.minosoft.data.registries.item.items.snow.SnowballItem
import de.bixilon.minosoft.data.registries.item.items.tool.materials.*
import de.bixilon.minosoft.data.registries.item.items.tool.shears.ShearsItem
import de.bixilon.minosoft.data.registries.item.items.weapon.attack.range.pullable.BowItem
import de.bixilon.minosoft.data.registries.item.items.weapon.defend.ShieldItem
import de.bixilon.minosoft.data.registries.registries.Registries

object ItemFactories : DefaultFactory<ItemFactory<*>>(
    AppleItem,
    AppleItem.GoldenAppleItem,
    AppleItem.EnchantedGoldenAppleItem,

    BucketItem.EmptyBucketItem,
    FilledBucketItem.LavaBucketItem,
    FilledBucketItem.WaterBucketItem,

    LeatherArmor.LeatherBoots,
    LeatherArmor.LeatherChestplate,
    LeatherArmor.LeatherLeggings,
    LeatherArmor.LeatherHelmet,

    ChainmailArmor.ChainmailBoots,
    ChainmailArmor.ChainmailChestplate,
    ChainmailArmor.ChainmailLeggings,
    ChainmailArmor.ChainmailHelmet,

    IronArmor.IronBoots,
    IronArmor.IronChestplate,
    IronArmor.IronLeggings,
    IronArmor.IronHelmet,

    GoldArmor.GoldBoots,
    GoldArmor.GoldChestplate,
    GoldArmor.GoldLeggings,
    GoldArmor.GoldHelmet,

    DiamondArmor.DiamondBoots,
    DiamondArmor.DiamondChestplate,
    DiamondArmor.DiamondLeggings,
    DiamondArmor.DiamondHelmet,

    NetheriteArmor.NetheriteBoots,
    NetheriteArmor.NetheriteChestplate,
    NetheriteArmor.NetheriteLeggings,
    NetheriteArmor.NetheriteHelmet,

    TurtleHelmet,
    ElytraItem,

    BowItem,
    ShieldItem,

    EnderEyeItem,
    EnderPealItem,

    SnowballItem,

    EggItem,

    DrinkingPotionItem,
    LingeringPotionItem,
    SplashPotionItem,

    OnAStickItem.CarrotOnAStickItem,
    OnAStickItem.WarpedFungusOnAStickItem,

    WoodenTool.WoodenSword, WoodenTool.WoodenShovel, WoodenTool.WoodenPickaxe, WoodenTool.WoodenAxe, WoodenTool.WoodenHoe,
    StoneTool.StoneSword, StoneTool.StoneShovel, StoneTool.StonePickaxe, StoneTool.StoneAxe, StoneTool.StoneHoe,

    GoldenTool.GoldenSword, GoldenTool.GoldenShovel, GoldenTool.GoldenPickaxe, GoldenTool.GoldenAxe, GoldenTool.GoldenHoe,

    IronTool.IronSword, IronTool.IronShovel, IronTool.IronPickaxe, IronTool.IronAxe, IronTool.IronHoe,

    DiamondTool.DiamondSword, DiamondTool.DiamondShovel, DiamondTool.DiamondPickaxe, DiamondTool.DiamondAxe, DiamondTool.DiamondHoe,

    NetheriteTool.NetheriteSword, NetheriteTool.NetheriteShovel, NetheriteTool.NetheritePickaxe, NetheriteTool.NetheriteAxe, NetheriteTool.NetheriteHoe,

    ShearsItem,

    FireChargeItem, FlintAndSteelItem,


    ClimbingItems.ScaffoldingItem,
), IntegratedRegistry<Item> {

    override fun build(name: ResourceLocation, registries: Registries, data: JsonObject): Item? {
        return this[name]?.build(registries, data)
    }
}
