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
import de.bixilon.minosoft.data.chat.signature.SignatureTestUtil
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.protocol.versions.Versions
import org.testng.annotations.Test
import org.testng.internal.junit.ArrayAsserts.assertArrayEquals
import java.time.Instant

@Test(groups = ["signature"], dependsOnGroups = ["private_key"])
class MessageSigner1Test {
    private lateinit var version: Version

    @Test(priority = -1)
    fun setup() {
        version = Versions.getById(ProtocolVersions.V_1_19)!!
    }

    private fun create(): MessageSigner1 {
        return MessageSigner1(version)
    }

    fun signing1() {
        val message = "bixilon began the development during lockdown"
        val time = Instant.ofEpochMilli(1673102470_1234)
        val signer = create()
        val signature = signer.signMessage(SignatureTestUtil.key.pair.private, message, -4886770986649219020L, TestAccount.uuid, time)


        val expected = "Wy91EhZPcvKlMweY+hZ6iegp9Ak5fGVdFVsTjYXl6MbnDzZtZ43GVZRvJDq/G8PZ9hsbS7ZfDGAs7TyMtYGRIuqiAR/3SKCmJZv6RGK6EiMc5uY4fQU+r/Bz+SjNivDlYXyv7ax3wZ90U210eyIDeVfYEmZqeZg4+WtWft1ZQaoTU9V2ZFIPWAQoRyhjtTVJeE8UyQrurT4LmVFom2/YFG/EtqvPTpC3vJS+EdgnWbGFvOTilGXWCrMg8I+yNFPTZ1Q7kG1HSyhy0anDT/rE9M1/SEtyHi1xhaIvueCQeIrQ/hoKdWCNJARXRdDaCAF+agtAPWneUVuquhGdhzsEmg==".fromBase64()
        assertArrayEquals(expected, signature)
    }

    fun signing2() {
        val message = "reversing signature code is pita"
        val time = Instant.ofEpochMilli(1673103191_5678)
        val signer = create()
        val signature = signer.signMessage(SignatureTestUtil.key.pair.private, message, 1856442944183750824L, TestAccount.uuid, time)


        val expected = "U+rXvXVgZoVJwFMpJAWkmRYxEF1YAxtWGojLJ1pqzdZOdfrrpfqs/jr5g6QQz5cJ2v96oo2I1Zpm+O2Qnigb/fJANCcV3OISVOS+BgtkhH/Z8vCvdsFlDAnJLVKtBayk2NiERnd5Ax+drYdWuxMxMVBBk62k1uvtibpEcrB18bQJVaL8vaYOPqnr51pUXZTBP/1/WtJLTsSGXv72wOETN8gumAufhmnqLtKtmT3zclieX7JvzMZogVgzapANTtO5ZuUdxxaPkrkYOffk3GdZ4zixoN4R/gObmxMcQ+IU1x+DGA8Bu7J+LJ4Q5MPv1B239FtazHy0OkDECGECGn5m8Q==".fromBase64()
        assertArrayEquals(expected, signature)
    }

    fun signing3() {
        val message = "now even with last seen messages. I don't know their purpose."
        val time = Instant.ofEpochMilli(1673103854_8402)
        val signer = create()

        val signature = signer.signMessage(SignatureTestUtil.key.pair.private, message, 6392082609L, TestAccount.uuid, time)


        val expected = "Rb41Gpj1gx00pc5mXo3YkDFim5dF0dpF6LJdpFOYfaTsyjqmxe+qudnHM8+ZJVdu30WJUYleWQk3f30eSz5FQewdP3CHRaFGYF/5ifh+j1It6RFQ1IyK3+4XITD781fnvjm8YkpljcJI7x7L9ewiwM/TKN/XnKqL/LT3ZRCghBfl2XJQLJBmOo9YeSwm6djFRAbNhQS+gy55M1dZQlRRI3tQtPK07as1/BwmhKeDfoto9lXgKVMJArS2HH08KaoBaqFhhI6SbE6CLCXpa/K3AF0ISldgbhgbLDbrgr+gAX9E+PL+hPmwR9WG5mikA+Mv0Snz2KZJKB+9G5IVWmyBFw==".fromBase64()
        assertArrayEquals(expected, signature)
    }

    fun signing4() {
        val message = "ʞ⍧\uDB16\uDFB1іⴎO\uD85E\uDF9A!㛽J˻5&뻝\uD8B9\uDE8F\uDA0C\uDC41\uDB9B\uDD0Fi읣މ猣랒糓\uD8EF\uDFCFk\uDA1D\uDCA4썿ߛ믣Ȣ\uDA51\uDF5B4" // random utf8 chars
        val time = Instant.ofEpochMilli(1739579479_6302)
        val signer = create()

        val signature = signer.signMessage(SignatureTestUtil.key.pair.private, message, 6392082609L, TestAccount.uuid, time)


        val expected = "mS4E4dFd10fiVzYbO7WIQw3k6dCMJKPQDx8SBZvVbaHbGhL4I3F2nCapTJRux1N/XBB8uS7/haqODK2TsnALYJgv9KVsgYSxOUQC6s+Dx7ziydOvkGy2w2nIvJAtI5MOaETOQE2PMXZgTg3g+IhULYMtOlB8eYcgjIJROEpe8AiTbjXofy8foIgRuVAiSuEJ/4tQfqs0v4C5QMBjOJfB/d3tz2JoIkEKop41k2cMh93xNO7UP+AUCsDI+nBnfqiDPORHo1fncyhIhucGeFcIkhDhDMZ6lytprXyUdmYWDqNtUXkS6yn5P33rCvWEPcpGAVhetOluQZAWsEOQ/9Tl3w==".fromBase64()
        assertArrayEquals(expected, signature)
    }
    // TODO: Test 22w17a (sha1 signature)
}
