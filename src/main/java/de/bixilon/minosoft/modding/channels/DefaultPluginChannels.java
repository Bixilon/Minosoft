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

package de.bixilon.minosoft.modding.channels;

import de.bixilon.minosoft.data.mappings.ResourceLocation;

public class DefaultPluginChannels {
    public static final ResourceLocation REGISTER = new ResourceLocation("minecraft:register");
    public static final ResourceLocation UNREGISTER = new ResourceLocation("minecraft:unregister");
    public static final ResourceLocation BRAND = new ResourceLocation("minecraft:brand");
    public static final ResourceLocation STOP_SOUND = new ResourceLocation("minecraft:stop_sound");
}
