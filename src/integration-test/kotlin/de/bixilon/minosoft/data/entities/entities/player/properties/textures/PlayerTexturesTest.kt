/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.entities.player.properties.textures

import org.testng.Assert.assertEquals
import org.testng.Assert.assertNotNull
import org.testng.annotations.Test

@Test(groups = ["skin"], dependsOnGroups = ["yggdrasil"])
class PlayerTexturesTest {


    fun `deserialize and check signature`() {
        val encoded = "ewogICJ0aW1lc3RhbXAiIDogMTY5OTcxMDcwMzI3MiwKICAicHJvZmlsZUlkIiA6ICIyNGYwZDRhMjE3ODc0NzYxYWVlZjM5YzkwODI0ZTc0NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGV3YXRpbmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDcxYzNhZGVmMmVjMGNiNTBlYmVhODg0MDMxOTQ1ZmU2MDRhMDdkNzI5ZjI4ZjE0MWU5MWUzZDZmZGI3NzE1ZiIKICAgIH0KICB9Cn0="
        val signature = "evh4Zmc2Pp+n3ZBmmKb+Bdg4psOfhW5qqFuY3vyRxUKDxIE31AnNvwEUBnAYH3NnocOxD15MsxFYKplVKP7AOf4HuC7ELkux1MhsQRHU6P5/ky/wuQFr3sC/KKpIihkjUQgozJqaTmGSXcrlp75CTUzAK49cwXt7Mi9xgpsrQTtOgMgIAErXc4in8pJJ8j5QhqleCxmH6dWYDeFBRt9bGPo6rceHhBuKlV6O5sVu8CqE5Rz7p4/MSgzRDTKclqUZghYZ3v9pGjz1tp6KY9RkMbXjSjpBxkWdGER2Z3C8euPqPSB1r37QUZrcVLkuRQ0jPzthMHSvDKJ6ugX0JJgXC0rzNtbopkXn43XqwfutcceWF5D3RjPgbEBmHQeO9jNS9XOpMHecU2MZY304c1Lhhn1lIBmpP4ckReJiTXl9axyOtQY2MnuHdxLQD/UKg5Y8ILjAzxnRr26FSHWrnS/nevL/D98W2XR3YfDX7TjpjBK0kihGIEA2/FBh2vu6ta9TwhOUH9JgVEaSkdv0jMBUgkJOrlxImpWJLkzD2c13m/Z6Tn7pOZK73QZXKLJWazT0bUUOl42G1VXixBBMjoCUwyrbFUXMG1fl97MR897gep5+RjqMCvlf1lwl/JYS8ITs+eZbPzZsy01k4kUjdhskyaNyflDY8fz7qcVBVA6EFP0="

        val textures = PlayerTextures.of(encoded, signature)

        assertEquals(textures.name, "thewating")
        assertNotNull(textures.skin)
    }
}
