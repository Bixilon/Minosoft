/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.modding;

import de.bixilon.minosoft.modding.loading.ModInfo;
import de.bixilon.minosoft.modding.loading.ModPhases;

@Deprecated
public abstract class MinosoftMod {
    protected boolean enabled = true;
    private ModInfo info;

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ModInfo getInfo() {
        return this.info;
    }

    public void setInfo(ModInfo info) {
        if (this.info != null) {
            throw new RuntimeException(String.format("Mod info already set %s vs %s", this.info, info));
        }
        this.info = info;
    }

    /**
     * @param phase The current loading phase
     * @return If the loading was successful. If not, the mod is getting disabled.
     */
    public abstract boolean start(ModPhases phase);
}
