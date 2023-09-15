package de.bixilon.minosoft.data.world.positions

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.world.positions.BlockPositionUtil.positionHash
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BlockPositionUtilTest {

    @Test
    fun hash1() {
        assertEquals(0L, Vec3i(0, 0, 0).positionHash)
    }

    @Test
    fun hash2() {
        assertEquals(-88257927667816, Vec3i(123, 456, 789).positionHash)
    }

    @Test
    fun hash3() {
        assertEquals(-88257927667816, Vec3i(-123, 456, -789).positionHash)
    }

    @Test
    fun hash4() {
        assertEquals(10888876138951, Vec3i(123, -456, 789).positionHash)
    }

    @Test
    fun hash5() {
        assertEquals(65198192324831, Vec3i(5473628, 123123, 1234737534).positionHash)
    }
}
