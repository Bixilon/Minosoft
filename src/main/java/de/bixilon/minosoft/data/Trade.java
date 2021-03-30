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

package de.bixilon.minosoft.data;

import de.bixilon.minosoft.data.inventory.ItemStack;
import org.checkerframework.common.value.qual.IntRange;

public class Trade {
    private final ItemStack input1;
    private final ItemStack input2;
    private final boolean enabled;
    private final int usages;
    private final int maxUsages;
    private final int xp;
    private final int specialPrice;
    private final float priceMultiplier;
    private final int demand;

    public Trade(ItemStack input1, ItemStack input2, boolean enabled, int usages, int maxUsages, int xp, int specialPrice, float priceMultiplier, int demand) {
        this.input1 = input1;
        this.input2 = input2;
        this.enabled = enabled;
        this.usages = usages;
        this.maxUsages = maxUsages;
        this.xp = xp;
        this.specialPrice = specialPrice;
        this.priceMultiplier = priceMultiplier;
        this.demand = demand;
    }

    public ItemStack getInput1() {
        return this.input1;
    }

    public ItemStack getInput2() {
        return this.input2;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    @IntRange(from = 0)
    public int getUsages() {
        return this.usages;
    }

    public int getMaxUsages() {
        return this.maxUsages;
    }

    public int getXp() {
        return this.xp;
    }

    public int getSpecialPrice() {
        return this.specialPrice;
    }

    public float getPriceMultiplier() {
        return this.priceMultiplier;
    }

    public int getDemand() {
        return this.demand;
    }
}
