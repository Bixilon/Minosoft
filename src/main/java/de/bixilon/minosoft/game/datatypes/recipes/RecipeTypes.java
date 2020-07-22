/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.game.datatypes.recipes;

public enum RecipeTypes {
    SHAPELESS("crafting_shapeless"),
    SHAPED("crafting_shaped"),
    SPECIAL_ARMOR_DYE("crafting_special_armordye"),
    SPECIAL_BOOK_CLONING("crafting_special_bookcloning"),
    SPECIAL_MAP_CLONING("crafting_special_mapcloning"),
    SPECIAL_MAP_EXTENDING("crafting_special_mapextending"),
    SPECIAL_FIREWORK_ROCKET("crafting_special_firework_rocket"),
    SPECIAL_FIREWORK_STAR("crafting_special_firework_star"),
    SPECIAL_FIREWORK_STAR_FADE("crafting_special_firework_star_fade"),
    SPECIAL_REPAIR_ITEM("crafting_special_repairitem"),
    SPECIAL_TIPPED_ARROW("crafting_special_tippedarrow"),
    SPECIAL_BANNER_DUPLICATE("crafting_special_bannerduplicate"),
    SPECIAL_BANNER_ADD_PATTERN("crafting_special_banneraddpattern"),
    SPECIAL_SHIELD_DECORATION("crafting_special_shielddecoration"),
    SPECIAL_SHULKER_BOX_COLORING("crafting_special_shulkerboxcoloring"),
    SPECIAL_SUSPICIOUS_STEW("crafting_special_suspiciousstew"),
    SMELTING("smelting"),
    BLASTING("blasting"),
    SMOKING("smoking"),
    CAMPFIRE("campfire_cooking"),
    STONE_CUTTING("stonecutting");

    final String name;

    RecipeTypes(String name) {
        this.name = name;
    }

    public static RecipeTypes byName(String name) {
        if (name.contains(":")) {
            name = name.split(":", 2)[1];
        }
        for (RecipeTypes recipeProperty : values()) {
            if (recipeProperty.getName().equals(name)) {
                return recipeProperty;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
