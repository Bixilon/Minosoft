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

package de.bixilon.minosoft.gui.rendering.system.base.texture

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.player.LocalPlayerEntity
import de.bixilon.minosoft.data.player.properties.PlayerProperties
import de.bixilon.minosoft.data.player.properties.textures.PlayerTexture.Companion.isSteve
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.gui.atlas.TextureLikeTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureUtil.readTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

abstract class TextureManager {
    abstract val staticTextures: StaticTextureArray
    abstract val dynamicTextures: DynamicTextureArray

    lateinit var debugTexture: AbstractTexture
        private set
    lateinit var whiteTexture: TextureLikeTexture
        private set
    lateinit var steveTexture: DynamicTexture
        private set
    lateinit var alexTexture: DynamicTexture
        private set
    lateinit var skin: DynamicTexture
        private set

    fun loadDefaultTextures() {
        if (this::debugTexture.isInitialized) {
            throw IllegalStateException("Already initialized!")
        }
        debugTexture = staticTextures.createTexture(RenderConstants.DEBUG_TEXTURE_RESOURCE_LOCATION)
        // steveTexture = staticTextures.createTexture("minecraft:entity/steve".toResourceLocation().texture())
        whiteTexture = TextureLikeTexture(texture = staticTextures.createTexture(ResourceLocation("minosoft:textures/white.png")), uvStart = Vec2(0.0f, 0.0f), uvEnd = Vec2(0.001f, 0.001f), size = Vec2i(16, 16))
    }

    fun loadDefaultSkins(connection: PlayConnection) {
        steveTexture = dynamicTextures.pushBuffer(UUID(0L, 0L)) { connection.assetsManager["minecraft:entity/steve".toResourceLocation().texture()].readTexture().second }.apply { usages.incrementAndGet() }
        alexTexture = dynamicTextures.pushBuffer(UUID(1L, 0L)) { connection.assetsManager["minecraft:entity/alex".toResourceLocation().texture()].readTexture().second }.apply { usages.incrementAndGet() }
        skin = getSkin(connection.account.supportsSkins, connection.account.uuid, connection.account.properties).apply { usages.incrementAndGet() }
    }


    fun getSkin(fetchSkin: Boolean, uuid: UUID, properties: PlayerProperties?): DynamicTexture {
        var properties = properties
        if (properties?.textures == null) {
            for (account in AccountProfileManager.selected.entries.values) {
                if (account.uuid == uuid) {
                    properties = account.properties
                }
            }
            if (properties?.textures == null && fetchSkin) {
                try {
                    properties = PlayerProperties.fetch(uuid) // ToDo: async
                } catch (ignored: Throwable) {
                }
            }
        }
        properties?.textures?.skin?.let { return dynamicTextures.pushRawArray(uuid) { it.read() } }
        if (uuid.isSteve()) {
            return steveTexture
        }
        return alexTexture
    }

    fun getSkin(player: PlayerEntity): DynamicTexture {
        if (player is LocalPlayerEntity) {
            return skin
        }
        val uuid = player.uuid
        if (uuid == null) {
            Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Player uuid is null: $player" }
            return steveTexture
        }
        return getSkin(true, uuid, player.tabListItem.properties)
    }
}
