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

package de.bixilon.minosoft.data.chat.signature

import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.minosoft.util.account.minecraft.MinecraftPrivateKey
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.yggdrasil.YggdrasilUtil
import org.testng.Assert
import org.testng.SkipException
import org.testng.annotations.Test
import java.time.Instant

@Test(groups = ["private_key"])
class SignatureSIT {
    // The private key is a private environment variable in gitlab. Yes, that makes it hard to run tests outside my devices, but you have to understand that I am not publishing my private key, no matter if it is already expired

    fun loadYggdrasil() {
        YggdrasilUtil.load()
    }

    fun loadPrivateKey() {
        val string = System.getenv("CHAT_SIGNATURE_PRIVATE_KEY") ?: throw SkipException("Can not find private key! Skipping chat signature tests")
        SignatureTestUtil.key = Jackson.MAPPER.readValue(string, MinecraftPrivateKey::class.java)
    }

    @Test(dependsOnMethods = ["loadPrivateKey"])
    fun testKeyUUID() {
        SignatureTestUtil.key.requireSignature("9e6ce7c5-40d3-483e-8e5a-b6350987d65f".toUUID()) // yep, that is really Bixilon's private key
    }

    @Test(dependsOnMethods = ["loadPrivateKey"])
    fun testRequireSignature() {
        Assert.assertThrows { SignatureTestUtil.key.requireSignature("b876ec32-e396-476b-a115-8438d83c67d4".toUUID()) } // sadly that is not possible anymore
    }

    @Test(dependsOnMethods = ["loadPrivateKey"])
    fun testExpiresAt() {
        Assert.assertEquals(SignatureTestUtil.key.expiresAt, Instant.ofEpochSecond(1668977246L, 93031788L)) // verify that we load the correct key and not just some key
    }
}

