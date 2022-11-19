/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.chat.signature

import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.minosoft.util.account.minecraft.MinecraftPrivateKey
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.yggdrasil.YggdrasilUtil
import org.testng.Assert
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["private_key"])
class SignatureSIT {

    fun loadYggdrasil() {
        YggdrasilUtil.load()
    }

    fun loadPrivateKey() {
        val string = System.getenv("CHAT_SIGNATURE_PRIVATE_KEY") ?: throw SkipException("Can not find private key! Skipping chat signature tests")
        SignatureTestUtil.key = Jackson.MAPPER.readValue(string, MinecraftPrivateKey::class.java)
    }

    fun testKeyUUID() {
        SignatureTestUtil.key.requireSignature("9e6ce7c5-40d3-483e-8e5a-b6350987d65f".toUUID()) // yep, that is really my private key
    }

    fun testRequireSignature() {
        Assert.assertThrows { SignatureTestUtil.key.requireSignature("b876ec32-e396-476b-a115-8438d83c67d4".toUUID()) }
    }
}

