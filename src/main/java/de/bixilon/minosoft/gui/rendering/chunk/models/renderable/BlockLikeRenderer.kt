package de.bixilon.minosoft.gui.rendering.chunk.models.renderable

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.chunk.models.FaceSize
import de.bixilon.minosoft.gui.rendering.textures.Texture

interface BlockLikeRenderer {
    val faceBorderSizes: Array<Array<FaceSize>?> // direction indexed
    val transparentFaces: BooleanArray

    fun render(context: BlockLikeRenderContext)

    fun resolveTextures(textures: MutableMap<ResourceLocation, Texture>)

    fun postInit() {}

    companion object {
        fun resolveTexture(textures: MutableMap<ResourceLocation, Texture>, textureResourceLocation: ResourceLocation): Texture {
            return textures.getOrPut(textureResourceLocation) { Texture(textureResourceLocation) }
        }
    }
}
