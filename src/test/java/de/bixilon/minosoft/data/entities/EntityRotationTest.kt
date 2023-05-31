package de.bixilon.minosoft.data.entities

import de.bixilon.minosoft.data.entities.EntityRotation.Companion.interpolateYaw
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EntityRotationTest {

    @Test
    fun interpolation1() {
        assertEquals(50.0f, interpolateYaw(0.5f, 0.0f, 100.0f))
    }

    @Test
    fun interpolation2() {
        assertEquals(0.0f, interpolateYaw(-1.0f, 0.0f, 100.0f))
    }

    @Test
    fun interpolation3() {
        assertEquals(100.0f, interpolateYaw(2.0f, 0.0f, 100.0f))
    }

    @Test
    fun interpolation4() {
        assertEquals(-180.0f, interpolateYaw(0.1f, 180.0f, -180.0f))
    }

    @Test
    fun interpolation5() {
        assertEquals(-180.0f, interpolateYaw(0.9f, 180.0f, -180.0f))
    }

    @Test
    fun interpolation6() {
        assertEquals(-170.0f, interpolateYaw(0.5f, 180.0f, -160.0f))
    }

    @Test
    fun interpolation7() {
        assertEquals(150.0f, interpolateYaw(0.5f, 110.0f, -170.0f))
    }
}
