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

package de.bixilon.minosoft.gui.rendering.system.base.texture.skin.vanilla

import de.bixilon.minosoft.data.entities.entities.player.properties.textures.metadata.SkinModel
import de.bixilon.minosoft.util.KUtil.minecraft

object DefaultSkins : Iterable<DefaultSkin> {
    val SKINS: MutableList<DefaultSkin> = mutableListOf()

    val ALEX = DefaultLegacySkin(minecraft("alex"), SkinModel.SLIM, fallback = false).register()
    val ARI = DefaultSkin(minecraft("ari")).register()
    val EFE = DefaultSkin(minecraft("efe")).register()
    val KAI = DefaultSkin(minecraft("kai")).register()
    val MAKENA = DefaultSkin(minecraft("makena")).register()
    val NOOR = DefaultSkin(minecraft("noor")).register()
    val STEVE = DefaultLegacySkin(minecraft("steve"), SkinModel.WIDE, fallback = true).register()
    val SUNNY = DefaultSkin(minecraft("sunny")).register()
    val ZURI = DefaultSkin(minecraft("zuri")).register()


    private fun <T : DefaultSkin> T.register(): T {
        SKINS += this

        return this
    }

    override fun iterator(): Iterator<DefaultSkin> {
        return SKINS.iterator()
    }
}
