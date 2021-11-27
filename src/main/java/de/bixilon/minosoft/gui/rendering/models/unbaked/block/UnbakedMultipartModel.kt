package de.bixilon.minosoft.gui.rendering.models.unbaked.block

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.baked.MultipartBakedModel
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockModel
import de.bixilon.minosoft.gui.rendering.models.properties.AbstractFaceProperties
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.util.KUtil.unsafeCast
import glm_.vec3.Vec3i
import java.util.*

class UnbakedMultipartModel(
    val models: Set<UnbakedBlockStateModel>,
) : UnbakedModel {

    override fun bake(renderWindow: RenderWindow): BakedModel {
        val baked: Array<BakedBlockModel?> = arrayOfNulls(this.models.size)
        val sizes: Array<MutableList<AbstractFaceProperties>> = Array(Directions.SIZE) { mutableListOf() }
        var particleTexture: AbstractTexture? = null

        for ((index, model) in this.models.withIndex()) {
            val bakedModel = model.bake(renderWindow)
            if (particleTexture == null) {
                val modelParticleTexture = bakedModel.getParticleTexture(RANDOM, Vec3i.EMPTY)
                if (modelParticleTexture != null) {
                    particleTexture = modelParticleTexture
                }
            }
            for (direction in Directions.VALUES) {
                sizes[direction.ordinal] += bakedModel.getTouchingFaceProperties(RANDOM, direction) // There is no random here!
            }
            baked[index] = bakedModel
        }
        val finalFaces: Array<Array<AbstractFaceProperties>?> = arrayOfNulls(Directions.SIZE)
        for (index in 0 until Directions.SIZE) {
            finalFaces[index] = sizes[index].toTypedArray()
        }


        return MultipartBakedModel(baked.unsafeCast(), finalFaces.unsafeCast(), particleTexture)
    }

    private companion object {
        private val RANDOM = Random(0L)
    }
}
