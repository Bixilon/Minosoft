package de.bixilon.minosoft.gui.rendering.chunk.models.renderable

import de.bixilon.minosoft.gui.rendering.chunk.models.FaceSize
import de.bixilon.minosoft.gui.rendering.textures.Texture

interface BlockLikeRenderer {
    val faceBorderSizes: Array<Array<FaceSize>?> // direction indexed
    val transparentFaces: BooleanArray

    fun render(context: BlockLikeRenderContext)

    fun resolveTextures(indexed: MutableList<Texture>, textureMap: MutableMap<String, Texture>)

    fun postInit() {}

    companion object {
        fun resolveTexture(indexed: MutableList<Texture>, textureMap: MutableMap<String, Texture>, textureName: String): Texture? {
            var texture: Texture? = null
            val index: Int? = textureMap[textureName]?.let {
                texture = it
                indexed.indexOf(it)
            }
            if (index == null) {
                texture = Texture(Texture.getResourceTextureIdentifier(textureName = textureName))
                textureMap[textureName] = texture!!
                indexed.add(texture!!)
            }
            return texture
        }
    }
}
