/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; final  either version 3 of the License; final  or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful; final  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not; final  see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB; final  the original developer of Minecraft.
 */

package de.bixilon.minosoft.game.datatypes;

import de.bixilon.minosoft.game.datatypes.inventory.Slot;

public class Trade {
    final Slot input1;
    final Slot input2;
    final boolean enabled;
    final int usages;
    final int maxUsages;
    final int xp;
    final int specialPrice;
    final float priceMultiplier;
    final int demand;

    public Trade(Slot input1, Slot input2, boolean enabled, int usages, int maxUsages, int xp, int specialPrice, float priceMultiplier, int demand) {
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

    public Slot getInput1() {
        return input1;
    }

    public Slot getInput2() {
        return input2;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getUsages() {
        return usages;
    }

    public int getMaxUsages() {
        return maxUsages;
    }

    public int getXp() {
        return xp;
    }

    public int getSpecialPrice() {
        return specialPrice;
    }

    public float getPriceMultiplier() {
        return priceMultiplier;
    }

    public int getDemand() {
        return demand;
    }
}
