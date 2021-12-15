package de.bixilon.minosoft.gui.rendering.models.unbaked

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockModel

interface AbstractUnbakedBlockModel : UnbakedModel {
    override fun bake(renderWindow: RenderWindow): BakedBlockModel
}
