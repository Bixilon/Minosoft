package de.bixilon.minosoft.gui.rendering.chunk.models.renderable

import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.world.BlockPosition
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.light.LightAccessor
import de.bixilon.minosoft.gui.rendering.chunk.SectionArrayMesh
import de.bixilon.minosoft.gui.rendering.textures.Texture

interface BlockRenderInterface {
    val fullFaceDirections: Array<Directions?>
    val transparentFaces: Array<Directions?>

    fun render(blockState: BlockState, lightAccessor: LightAccessor, tintColor: RGBColor?, position: BlockPosition, mesh: SectionArrayMesh, neighbourBlocks: Array<BlockState?>, world: World)

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
