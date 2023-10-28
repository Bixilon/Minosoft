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

package de.bixilon.minosoft.gui.rendering.system.base.texture.skin

import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.properties.PlayerProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.skin.vanilla.DefaultSkinProvider
import java.util.*

class SkinManager(private val textureManager: TextureManager) {
    lateinit var default: DefaultSkinProvider
        private set
    private var skin: PlayerSkin? = null

    fun initialize(account: Account, assets: AssetsManager) {
        default = DefaultSkinProvider(this.textureManager.dynamicTextures, assets)
        default.initialize()
        skin = getSkin(account.uuid, account.properties, fetch = true, async = false)
    }

    private fun getAccountProperties(uuid: UUID): PlayerProperties? {
        for (account in AccountProfileManager.selected.entries.values) {
            if (account.uuid != uuid) {
                continue
            }
            return account.properties
        }
        return null
    }

    private fun getProperties(player: PlayerEntity, uuid: UUID, fetch: Boolean): PlayerProperties? {
        return player.additional.properties ?: getAccountProperties(uuid) ?: if (fetch) catchAll { PlayerProperties.fetch(uuid) } else null
    }

    private fun getSkin(uuid: UUID, properties: PlayerProperties?, async: Boolean = true): PlayerSkin? {
        val texture = properties?.textures?.skin ?: return default[uuid]
        return PlayerSkin(textureManager.dynamicTextures.pushRaw(texture.getHash(), async) { texture.read() }, default[uuid]?.texture, texture.metadata.model)
    }

    fun getSkin(player: PlayerEntity, properties: PlayerProperties? = null, fetch: Boolean = true, async: Boolean = true): PlayerSkin? {
        if (player is LocalPlayerEntity) {
            return skin
        }
        val uuid = player.uuid ?: return default[player]
        return getSkin(uuid, properties ?: getProperties(player, uuid, fetch), async)
    }

    fun getSkin(uuid: UUID?, properties: PlayerProperties? = null, fetch: Boolean = true, async: Boolean = true): PlayerSkin? {
        if (uuid == null) return default[null]

        return getSkin(uuid, properties ?: if (fetch) catchAll { PlayerProperties.fetch(uuid) } else null, async)
    }
}
