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
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
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
class MessageSigner3Test {
    private lateinit var version: Version

    @Test(priority = -1)
    fun setup() {
        version = Versions.getById(ProtocolVersions.V_1_19_3)!!
    }

    private fun create(sessionId: UUID): MessageSigner3 {
        return MessageSigner3(version, sessionId)
    }

    fun signing1() {
        val message = "bixilon began the development during lockdown"
        val time = Instant.ofEpochMilli(1673102470_1234)
        val signer = create("a26b7114-36c4-4eea-acac-108f516ef72d".toUUID())
        val signature = signer.signMessage(SignatureTestUtil.key.pair.private, message, -4886770986649219020L, TestAccount.uuid, time, LastSeenMessageList(emptyArray()))


        val expected = "UKzEfea2lAHcBTqv1G8dq2VYwXoUr9MNKUZyiL2YjB4DnX/harx2bQebkTelFoEc4EXe5n9TLowcfHFuj1LeGfjlduooST6Y88pFZjZNza2HwtvF+caczh5mcrYScYs5bXCD5Td42H9Ri7JKR0uRzK7OGXiHA/4b6RvF68SwcsvUTfiS5YTSnWBCdd8dQWkaRQS+H+gGXL5H3ofvGHlIIglHsZL8rkkuqknsg8sPSnh8xPzUUztV82Gbe7ZCXTR0yrO0zL4EI1URM4KawCothG168uXMNAju6Ad15Iit86PPTbAudzfZ4iBYsrENP+ntDsD9C0frFiAhr/OmXwGpHg==".fromBase64()
        assertArrayEquals(expected, signature)
    }

    fun signing2() {
        val message = "reversing signature code is pita"
        val time = Instant.ofEpochMilli(1673103191_5678)
        val signer = create("79f3da83-a6a1-4d55-9aca-39d5faf4ad8b".toUUID())
        val signature = signer.signMessage(SignatureTestUtil.key.pair.private, message, 1856442944183750824L, TestAccount.uuid, time, LastSeenMessageList(emptyArray()))


        val expected = "UJCsmqR5rUjeEAuvxqVCDj3bprQ5ObbngoPJdJTmvq5KLXHTUM2bQydzjstxd29+ykGwDC1e8gLXI8mR4O+4rC+36A8jgbz7ipeqHPqC5WZw/tngEqNRr9vf5PaiM0PIrB1L+kn13UBHYKx2FbtBspdXcB7SX2phJKPNDZ3RqdhUJ+n7MdZwRcdgoWZ5ItRVVFbaAJaAqSupTTCY1WGrjxSNqmhZcGiMC/z9/4LAzXLFcwPMnRuLCqYiz5Rn055H1+l4ZMZGCTKTMjDjnGlVnS09m5lXlji1XnNtI1ecnlYya8gI8+eQX0Y+tDxvTuR3Ra8tKfMQlXh8LGnA5u0djQ==".fromBase64()
        assertArrayEquals(expected, signature)
    }

    fun signing3() {
        val message = "now even with last seen messages. I don't know their purpose."
        val time = Instant.ofEpochMilli(1673103854_8402)
        val signer = create("5c31705b-963c-4832-b8a5-1ee617a4b362".toUUID())

        val lastSeen = LastSeenMessageList(arrayOf(LastSeenMessage(UUID(0L, 0L), "/OmblHW9CwdAawdE7WtVurRs7umsa/my8EeTT/NY36lqg3HmtAsqyRQ4fXHelw6pOA4o8UroAlmx+inwiEFHXkDICEIVM69vHKapQvoAaeEVrQ4pn9vURTY3GcUVOgr12V3d00odEpwviXaF6kchG1b/pZsLsfpacMiiVHkxZoqolvUNifHQKXVS48Gu1AKkwRw6bkcOKYZpcZgffe6U273rEQQefwzIdT/8F1P04WhiH7SREexVOolkuoKo6gYxXELf5M0BUf0ssG3SS1k8Wr3ys9nzB6hSoEd/ftKqGVxoqeq7pd1GgKfaRWpka8ZNyDpdm8JqvrmlN/phpS5X4Q==".fromBase64()))) // uuid does not matter, bytes are random
        val signature = signer.signMessage(SignatureTestUtil.key.pair.private, message, 6392082609L, TestAccount.uuid, time, lastSeen)


        val expected = "lx2uggzeCgjn3KOP3iymTMy/RKhmpNocYiqsNEhCFp+fq0rMzp7lPQAeuc8hy5xuSPFdiOInTanPSMu3FVrSZSPOQye+4w5cQwjygN5WSFp/glRFkw/6GL9RP7Zi7A8VEbGzRcTguDgryEQFytiBKX3m+5pMpsT7jQZAnxsl0aCyhLXfS6Frl4hqnSjuC05l79QziorZZpKa0F4sdDMIsnlRPl4NEB4jjwId41VwtEArMeHgDQ3aPZi2nfPEGXJCKdxdFMUFKDlaBW8QNWHePdaaivtuSsSeZJzbN4sqtMHSYJJJNEc/QY7LFpBZTlnW1EKy4tWBnWCY4Gra/FE/9Q==".fromBase64()
        assertArrayEquals(expected, signature)
    }

    fun signing4() {
        val message = "ʞ⍧\uDB16\uDFB1іⴎO\uD85E\uDF9A!㛽J˻5&뻝\uD8B9\uDE8F\uDA0C\uDC41\uDB9B\uDD0Fi읣މ猣랒糓\uD8EF\uDFCFk\uDA1D\uDCA4썿ߛ믣Ȣ\uDA51\uDF5B4" // random utf8 chars
        val time = Instant.ofEpochMilli(1739579479_6302)
        val signer = create("232170f7-ef39-4959-bf4a-354145b9483a".toUUID())

        val signature = signer.signMessage(SignatureTestUtil.key.pair.private, message, 6392082609L, TestAccount.uuid, time, LastSeenMessageList(emptyArray()))


        val expected = "n9stopralm3Ns3H4QgESZHp8gSyKtQ71bSJqSHPPGo1OwXCXYpFrYURquMW56PN8eZuvz4ppoGIrrw6QN+Z3wxDs6I5ex9uy3nyzGHaJ5iL6N7QZCqE4N/dnkVc6i4wp/lpp6AMO6tBzR8HE44UIBLKY+42J2AZitCX8N305Qq4kwB3TpqUWMr3/zW6HvmDa3mP43hhlBDqnFcKtTQy1iIkHZHEcqAj1fodP0UPJJJvmgtyI6dFyvc8JaxQV2BqRVoj8sl0cxYlEKx93//HByBNMHo37t8VfY8A6YT/DsxJD0Tz/Zv+ild+GLGXrde0fl0YNs5LXmMRN3OHkLlBRfw==".fromBase64()
        assertArrayEquals(expected, signature)
    }

    fun signing5() {
        val messages = listOf(
            Pair(
                "this is the first message :)",
                Instant.ofEpochMilli(1673104584_7491),
            ),
            Pair(
                "this is the second message :)",
                Instant.ofEpochMilli(1673104585_7492),
            ),
            Pair(
                "this is the third and last message :)",
                Instant.ofEpochMilli(1673104586_7493),
            )
        )

        val signer = create("de2f95c2-8730-47ba-a5aa-8db09fcec48e".toUUID())

        val signatures: MutableList<ByteArray> = mutableListOf()
        for ((message, time) in messages) {
            signatures += signer.signMessage(SignatureTestUtil.key.pair.private, message, 6392082609L, TestAccount.uuid, time, LastSeenMessageList(emptyArray()))
        }


        val expected = listOf(
            "aCJ/YjIz0rYLgx2dZwzaRCy5vbKKLna49haTJw4Ygq0N7uGlbyiQJpptnt3toSP00xyF2QZ3lldEYIklK5t6234aGBiTfq0WHfCGY2bDTpJIkqjII/EnIhhaSpGGw0f5GpiWDUxdwlZVeo3wO3P2LT3TzlJqZzdG9NypYKf3VLVyGd+UQbr6ivMSltavqhnInY+0ymVkk/AFcemmZ0h01NuNPsxWGQqTQnA4bZ8ByJOQWjWYXCvcoGzLGhEOyMBb64r0DSwW/99UPTMUI2K/u6vuC3omI0wrcmZ04C4P3+5EwXUCwCPyNGOl/9Bupose/HdBb1beFlfab2n3Hn+DFg==".fromBase64(),
            "KbiFTDN4v9mnNaVUcnkXVJcEBxInl/bggyuzf2DLp8Uu28iV0v+7i8E/ScfIPkfm5baO7UqAdd4ZQRWbT46v1+fjWkgx3SQXEg2OshelVPTChVjJBwqsxtAcw80IkSraZfPmhZnKZcvIaiEjHBt6J+oV4VJejYDyYP/IFAm7J5q//fzmt2nOuWu6nx3nn7SIeN7zHyvScjjYWHohA8MVKGHclCLhgNl2X+WoiYGDqlUEvMHX6kprrmbp/C+wfgF8lf39slFs0xnUxs5GcRdbc+0iAMkFe+0js2sYRWDGa3/7gA66sq2yrdFXlfxDw74U9XW+wJkMlRe8y2iHygXhbA==".fromBase64(),
            "PUYn1xRYCpK0+cBDmpXbZhm/K+KilsAiw255oZy4sTc8BK/+N7G87iy/Qat/BRBLz1FM11uNrQlAHJbpz4FJTBwLV6bayt3HwUw4s646DEag2bkUFYBWR/JEPLRP3jVfXeyfy22XYI9gvxbDAXSOiShvNJmP1yUyfk02QqIiuLDjdTLumILOgoJTUF6Wj7rx/mpVIgYnKmIH4D/gfwErI/Gng37fAWHux2m4CoHswAb4qbJHv86RsxiApLz/tT6XjOp/8hkL4hISvvQFMw+LgcQzoASspC4OIApiR++aZCT+i3GhTnqBC2AiH1/t3Dhc55LQ1RmD0gOkkEDyHDo7Kg==".fromBase64(),
        )
        for (i in expected.indices) {
            assertArrayEquals(expected[i], signatures[i])
        }
    }
}
