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

package de.bixilon.minosoft.gui.rendering.system.base.texture.skin.vanilla

import de.bixilon.kotlinglm.GLM.abs
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.properties.textures.metadata.SkinModel
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.skin.PlayerSkin
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.readTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import java.util.*

class DefaultSkinProvider(
    private val array: DynamicTextureArray,
    private val assets: AssetsManager,
) {
    private var defaultId = 0
    private val slim: MutableMap<ResourceLocation, DynamicTexture> = mutableMapOf()
    private val wide: MutableMap<ResourceLocation, DynamicTexture> = mutableMapOf()
    private var fallback: PlayerSkin? = null

    fun initialize() {
        for (skin in DefaultSkins) {
            load(skin)
        }
    }


    private fun load(skin: DefaultSkin) {
        var loaded = 0
        load(skin.name.skin("slim").texture())?.let { slim[skin.name] = it; loaded++ }
        load(skin.name.skin("wide").texture())?.let { wide[skin.name] = it; loaded++ }

        if (loaded > 0) {
            return
        }
        if (skin is DefaultLegacySkin) {
            loadLegacy(skin)
        }
    }

    private fun loadLegacy(skin: DefaultLegacySkin) {
        val path = ResourceLocation(skin.name.namespace, "entity/${skin.name.path}").texture()
        val texture = load(path) ?: return
        this[skin.model][skin.name] = texture

        if (skin.fallback) {
            this.fallback = PlayerSkin(texture, skin.model)
        }
    }

    private fun load(path: ResourceLocation): DynamicTexture? {
        val data = assets.getOrNull(path)?.readTexture() ?: return null
        val texture = array.pushBuffer(UUID(0L, defaultId++.toLong()), true) { data }
        texture.usages.incrementAndGet()
        return texture
    }

    private fun ResourceLocation.skin(prefix: String): ResourceLocation {
        return ResourceLocation(namespace, "entity/player/$prefix/$path")
    }

    operator fun get(name: ResourceLocation, slim: Boolean): DynamicTexture? {
        val map = if (slim) this.slim else this.wide
        return map[name]
    }

    private operator fun get(model: SkinModel): MutableMap<ResourceLocation, DynamicTexture> {
        return when (model) {
            SkinModel.SLIM -> this.slim
            SkinModel.WIDE -> this.wide
        }
    }

    private fun UUID.isSteve(): Boolean {
        return hashCode() % 2 == 0
    }

    operator fun get(skin: DefaultSkin, model: SkinModel): PlayerSkin? {
        return this[model][skin.name]?.let { PlayerSkin(it, model) }
    }

    operator fun get(uuid: UUID?): PlayerSkin? {
        if (uuid == null) {
            return fallback
        }
        if (this.slim.size <= 1) {
            return getLegacy(uuid) ?: fallback
        }
        // TODO: verify with vanilla
        val count = (DefaultSkins.SKINS.size * 2)
        val hash = abs(uuid.hashCode()) % count
        val model = if (hash > count / 2) SkinModel.WIDE else SkinModel.SLIM

        return this[DefaultSkins.SKINS[hash / 2], model] ?: getLegacy(uuid)
    }

    fun getLegacy(uuid: UUID): PlayerSkin? {
        val skin = if (uuid.isSteve()) DefaultSkins.STEVE else DefaultSkins.ALEX
        if (skin.fallback) {
            return this.fallback
        }
        return this[skin, skin.model]
    }

    operator fun get(player: PlayerEntity): PlayerSkin? {
        return this[player.uuid] ?: return fallback
    }
}
