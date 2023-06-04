package de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedFace
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.SideSize
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.MemoryTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class FaceCullingTest {

    private fun createFace(size: SideSize.FaceSize? = SideSize.FaceSize(Vec2(0), Vec2(1)), transparency: TextureTransparencies = TextureTransparencies.OPAQUE): BakedFace {
        return BakedFace(floatArrayOf(), floatArrayOf(), 1.0f, -1, null, MemoryTexture(minosoft("test"), Vec2i.EMPTY), size)
    }

    private fun createNeighbour(size: SideSize? = SideSize(arrayOf(SideSize.FaceSize(Vec2(0), Vec2(1)))), transparency: TextureTransparencies = TextureTransparencies.OPAQUE): BlockState {
        TODO()
    }

    @Test
    fun noNeighbour() {
        val face = createFace()
        assertFalse(FaceCulling.canCull(face, null))
    }

    @Test
    fun selfNotTouching() {
        val face = createFace(null)
        val neighbour = createNeighbour()
        assertFalse(FaceCulling.canCull(face, neighbour))
    }

    @Test
    fun neighbourNotTouching() {
        val face = createFace()
        val neighbour = createNeighbour(null)
        assertFalse(FaceCulling.canCull(face, neighbour))
    }

    // TODO: force no cull (e.g. leaves), mix of transparency, mix of side sizes
}
