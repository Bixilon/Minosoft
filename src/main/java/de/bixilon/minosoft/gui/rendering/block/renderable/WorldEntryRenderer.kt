package de.bixilon.minosoft.gui.rendering.block.renderable

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.block.models.FaceSize
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture

interface WorldEntryRenderer {
    val faceBorderSizes: Array<Array<FaceSize>?> // direction indexed
    val transparentFaces: BooleanArray

    fun render(context: BlockLikeRenderContext)

    fun resolveTextures(textureManager: TextureManager)

    fun postInit() {}

    companion object {
        fun resolveTexture(textureManager: TextureManager, textureResourceLocation: ResourceLocation): AbstractTexture {
            return textureManager.staticTextures.createTexture(textureResourceLocation)
        }
    }
}
