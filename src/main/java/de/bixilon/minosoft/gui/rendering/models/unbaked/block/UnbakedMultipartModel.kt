package de.bixilon.minosoft.gui.rendering.models.unbaked.block

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.baked.MultipartBakedModel
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedModel
import de.bixilon.minosoft.util.KUtil.unsafeCast

class UnbakedMultipartModel(
    val models: Set<UnbakedBlockStateModel>,
) : UnbakedModel {

    override fun bake(renderWindow: RenderWindow): BakedModel {
        val baked: Array<BakedBlockModel?> = arrayOfNulls(this.models.size)

        var index = 0
        for (model in this.models) {
            baked[index++] = model.bake(renderWindow)
        }

        return MultipartBakedModel(baked.unsafeCast())
    }
}
