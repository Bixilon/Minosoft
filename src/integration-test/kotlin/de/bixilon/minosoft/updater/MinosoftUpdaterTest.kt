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

package de.bixilon.minosoft.updater

import org.testng.Assert.assertThrows
import org.testng.annotations.Test


@Test(groups = ["updater"])
class MinosoftUpdaterTest {

    private fun verify(signature: String, data: String) {
        MinosoftUpdater.parse(signature + "\n" + data)
    }

    fun `verify broken signature`() {
        assertThrows { verify("NOT_VALID", """{"id":"dummy","name":"Dummy version","date":-1,"stable":true,"page":null,"download":{"url":"https://bixilon.de/secret-update.jar","size":123,"sha512":"b8244d028981d693af7b456af8efa4cad63d282e19ff14942c246e50d9351d22704a802a71c3580b6370de4ceb293c324a8423342557d4e5c38438f0e36910ee"},"release_notes":":)"}""") }
    }

    fun `verify correct signature`() {
        assertThrows {
            verify(
                "Mv7979ky1AlMCcOLmX+Zdmo2Y7YOGiMthTBeNP2jKUPRkMtX1GCBYMrsKV+si9rR9Kg+1Ns82Hw1iYAI+ZOkTzVmoIeaWkqL4PN4sCCVllm2ZmZhTap7wdNAVEjW197Cf+V2YJW0TsG+j2s6OK86gdtVmyJ96X/ENhXTJRp8pW50lcCyy95ipQ1Qe8v4mAFykpU9XC/yU2Mhil/KznxvxKgd7N4+/VNpubOHetWfdiz9jqAB6uaYVi0H9E+EoZodkG3Iy5uagr1OrWNiwjk3LQUEk+J+5cYRPqBHrqLM9VNQFa5BqysSJoW7cIo/QUQA47EBxO8Rmg/juFA1l9bXtSUA+1j12n9ImhE/L3cYseYiIN8GFRpbhSaROgfZW9u3lVHM4g45q67zvvdf+Eo7lqfipYio89rQ984U58o5AvLhV+WqhDVRBTTtO+oI/FjdiHIruoiY/adEz7gJEAlrMlgoAAQkVnKma9uufObIemL+QGpDjLvdluIgts/cT34r4I5Xaij1vGAjzZ+Fe+Tn5tuW48pjtjWCzAwVTEu/zf/VKSJPoCVGx5YCvFE3CKXkVWuJ86gj+rO/SXWkjv672EetaVwv2Uc/RkCfru84m6bQWAHzb3P46Hfkw3kIyaIudxgizy1xlxLEEU3LwUU/vFxTd2Q6lAhGGMn6Imy9Z6I=",
                """{"id":"dummy","name":"Dummy version","date":-1,"stable":true,"page":null,"download":{"url":"https://bixilon.de/secret-update.jar","size":123,"sha512":"b8244d028981d693af7b456af8efa4cad63d282e19ff14942c246e50d9351d22704a802a71c3580b6370de4ceb293c324a8423342557d4e5c38438f0e36910ee"},"release_notes":":)"}""")
        }
    }
}
