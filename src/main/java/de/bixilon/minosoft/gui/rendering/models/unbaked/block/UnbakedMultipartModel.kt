package de.bixilon.minosoft.gui.rendering.models.unbaked.block

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.FaceSize
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.baked.MultipartBakedModel
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedModel
import de.bixilon.minosoft.util.KUtil.unsafeCast
import java.util.*

class UnbakedMultipartModel(
    val models: Set<UnbakedBlockStateModel>,
) : UnbakedModel {

    override fun bake(renderWindow: RenderWindow): BakedModel {
        val baked: Array<BakedBlockModel?> = arrayOfNulls(this.models.size)
        val sizes: Array<MutableList<FaceSize>> = Array(Directions.SIZE) { mutableListOf() }

        for ((index, model) in this.models.withIndex()) {
            val bakedModel = model.bake(renderWindow)
            for (direction in Directions.VALUES) {
                sizes[direction.ordinal] += bakedModel.getSize(RANDOM, direction) // There is no random here!
            }
            baked[index] = bakedModel
        }
        val finalFaces: Array<Array<FaceSize>?> = arrayOfNulls(Directions.SIZE)
        for (index in 0 until Directions.SIZE) {
            finalFaces[index] = sizes[index].toTypedArray()
        }


        return MultipartBakedModel(baked.unsafeCast(), finalFaces.unsafeCast())
    }

    private companion object {
        private val RANDOM = Random(0L)
    }
}
