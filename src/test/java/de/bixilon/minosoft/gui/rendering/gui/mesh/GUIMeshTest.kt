package de.bixilon.minosoft.gui.rendering.gui.mesh

import de.bixilon.kotlinglm.vec2.Vec2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GUIMeshTest {

    @Test
    fun transforming1() {
        val position = Vec2(0, 0)
        val halfSize = Vec2(1000, 1000) / 2

        assertEquals(Vec2(-1.0, 1.0), GUIMesh.transformPosition(position, halfSize))
    }

    @Test
    fun transforming2() {
        val position = Vec2(400, 600)
        val halfSize = Vec2(1000, 1000) / 2

        assertEquals(Vec2(-0.19999999, -0.20000005), GUIMesh.transformPosition(position, halfSize))
    }

    @Test
    fun transforming3() {
        val position = Vec2(1000, 1000)
        val halfSize = Vec2(1000, 1000) / 2

        assertEquals(Vec2(1.0f, -1.0f), GUIMesh.transformPosition(position, halfSize))
    }
}
