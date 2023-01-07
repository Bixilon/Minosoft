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

package de.bixilon.minosoft.data.chat.signature.signer

import de.bixilon.kutil.base64.Base64Util.fromBase64
import de.bixilon.minosoft.data.accounts.types.test.TestAccount
import de.bixilon.minosoft.data.chat.signature.LastSeenMessageList
import de.bixilon.minosoft.data.chat.signature.SignatureTestUtil
import de.bixilon.minosoft.data.chat.signature.lastSeen.LastSeenMessage
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.protocol.versions.Versions
import org.testng.annotations.Test
import org.testng.internal.junit.ArrayAsserts.assertArrayEquals
import java.time.Instant
import java.util.*

@Test(groups = ["signature"], dependsOnGroups = ["private_key"])
class MessageSigner2Test {
    private lateinit var version: Version

    @Test(priority = -1)
    fun setup() {
        version = Versions.getById(ProtocolVersions.V_1_19_2)!!
    }

    private fun create(): MessageSigner2 {
        return MessageSigner2(version)
    }

    fun signing1() {
        val message = "bixilon began the development during lockdown"
        val time = Instant.ofEpochMilli(1673102470_1234)
        val signer = create()
        val signature = signer.signMessage(SignatureTestUtil.key.pair.private, message, null, -4886770986649219020L, TestAccount.uuid, time, LastSeenMessageList(emptyArray()))


        val expected = "".fromBase64() // TODO
        assertArrayEquals(expected, signature)
    }

    fun signing2() {
        val message = "reversing signature code is pita"
        val time = Instant.ofEpochMilli(1673103191_5678)
        val signer = create()
        val signature = signer.signMessage(SignatureTestUtil.key.pair.private, message, null, 1856442944183750824L, TestAccount.uuid, time, LastSeenMessageList(emptyArray()))


        val expected = "".fromBase64() // TODO
        assertArrayEquals(expected, signature)
    }

    fun signing3() {
        val message = "now even with last seen messages. I don't know their purpose."
        val time = Instant.ofEpochMilli(1673103854_8402)
        val signer = create()

        val lastSeen = LastSeenMessageList(arrayOf(LastSeenMessage(UUID(0L, 0L), "/OmblHW9CwdAawdE7WtVurRs7umsa/my8EeTT/NY36lqg3HmtAsqyRQ4fXHelw6pOA4o8UroAlmx+inwiEFHXkDICEIVM69vHKapQvoAaeEVrQ4pn9vURTY3GcUVOgr12V3d00odEpwviXaF6kchG1b/pZsLsfpacMiiVHkxZoqolvUNifHQKXVS48Gu1AKkwRw6bkcOKYZpcZgffe6U273rEQQefwzIdT/8F1P04WhiH7SREexVOolkuoKo6gYxXELf5M0BUf0ssG3SS1k8Wr3ys9nzB6hSoEd/ftKqGVxoqeq7pd1GgKfaRWpka8ZNyDpdm8JqvrmlN/phpS5X4Q==".fromBase64()))) // uuid does not matter, bytes are random
        val signature = signer.signMessage(SignatureTestUtil.key.pair.private, message, null, 6392082609L, TestAccount.uuid, time, lastSeen)


        val expected = "".fromBase64() // TODO
        assertArrayEquals(expected, signature)
    }

    fun signing4() {
        val message = "ʞ⍧\uDB16\uDFB1іⴎO\uD85E\uDF9A!㛽J˻5&뻝\uD8B9\uDE8F\uDA0C\uDC41\uDB9B\uDD0Fi읣މ猣랒糓\uD8EF\uDFCFk\uDA1D\uDCA4썿ߛ믣Ȣ\uDA51\uDF5B4" // random utf8 chars
        val time = Instant.ofEpochMilli(1739579479_6302)
        val signer = create()

        val signature = signer.signMessage(SignatureTestUtil.key.pair.private, message, null, 6392082609L, TestAccount.uuid, time, LastSeenMessageList(emptyArray()))


        val expected = "".fromBase64() // TODO
        assertArrayEquals(expected, signature)
    }

    // TODO: Test 1.19.1 (changes to string encoding)
    // TODO: Test preview
    // TODO: Test previous signature (singing 2x)
}
