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

package de.bixilon.minosoft.util.yggdrasil

import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["signature", "yggdrasil"])
class YggdrasilUtilTest {

    fun `simple skin from mineskin_org`() {
        val texture = "ewogICJ0aW1lc3RhbXAiIDogMTY5MDQwNDM4NjczNywKICAicHJvZmlsZUlkIiA6ICI3ZGY4NmY1MWFjZmI0MjQzYTkzNDQ1OTAyZDEzYTc0MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNclJpcHRpZGUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjAxNzk1YmZhNmY3YzdkY2E0ZTI2ZWZiMzg2NGFmN2Y2MzhkZTliY2I4MjVjY2QzMjZiN2ZlZGVhZmVlZGZlNSIKICAgIH0KICB9Cn0="
        val signature = "MZrr6ClYhJoSAM67/zzdD3QhASIKcq5xAXVYJx50QY6QTNFH85h9hAD93fi17ncRzpGvHaktRCk8Jlc7RLhJunidrYZbApJ0ooYRiVMcKu6PY2GvI2/hHkfpMmmnhWUXmmIpcziWQCZAc5xx+5Seud+vvOp5+bvAM10Re1wsU+6lK8lIdSYej4Fy8LZqoX7NkCQjFeH4EVRFghhiZHZQZtBX1qqCiVe6IVqmYtCNYX7dER5VCjK9kye/WzRv41mV8U7PCyavhE0LdgDKK/uGkyAn62CZa0dvwNQ+adpPF+5uMh5UxCvs1E25cVGkOk2jWcUcQOIEDWRD4Jn2073LEYAiVKGKNzI92C7bWiaihIQg5P3Wo1lXS3PPfZ3fKvkgYf+Vju/y6zPcxkfbGngm8m39EaypGy71UJ7vj01w038NAUxEYIS0vZFB2lWDNFxwVAHN1uh831FzyyxBPQb5UaUfecbPq5RXeoquyd5Omy8oRWDvpaAIPsttvKFebxZox0sMBPn3GQWtmq8dNIAvmi2SPLROHl1VstmG9yfbPAza7Iq8Ape2siidl64M/hpFcYXBWssGMzPYjXVp+924XKx6bzImxnc8on8qOoyQFOxftEHjpkLNDvrxYSiqoK3TDNZToiTh6qClqxMkOkJZgOfOQjD41hbyzu7gWBxfIf4="

        assertTrue(YggdrasilUtil.verify(texture, signature))
    }

    fun `other skin from mineskin_org`() {
        val texture = "ewogICJ0aW1lc3RhbXAiIDogMTY5MDQwMTIzODM2NCwKICAicHJvZmlsZUlkIiA6ICJhODUxNzQ0MDNlNjg0MDgwYWNkODU3MzhlMjI5NGNhZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJEYVJpdmVyc09uZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80YjJjMjAwYjA1ZWY4NDhiY2IyZGM2YTBlOGY2OTQ0MWE3YzE3Mzg4Y2FjY2UzNzAxMzg5YTU0OGM2NTdjMzZkIgogICAgfQogIH0KfQ=="
        val signature = "dP48UYYnKFJzGidMiIsMdcA0J9ZuII6UjGqJAOOyjnxj80Uf+BHWHWPJzTuUPe+hpyJrp0wdS/n6VEK6XrkAIYXtsl3gzWPTmNGk1R+R2omALX5ldT9AogdHijPTJOenDvNxn3AsDra3r9RtC9DY8jx5tC1Joeeof87bsyMgLtXbiMrpzvkp0GLujnIDTsM1a0NtHaogkBJRMyIqwc2mImUKxsYieP7oA7MSYCaTKRxGlZZ72tfcTqCqIP5NuOwbxYjgTlfHNsGTO5qRhZP972Z5rDeSDYCuSP/4cpuYdd8CVRN/LnfZ9vV6WwDSfPodLF0TGQUY7w5zSp7DbEe73gt9Xk/wbIqohAwe9m8DPJMq5mrNW1skDjjg7mwgDgL15RIU+g2MjCuxt/2WQ/vsoudsvw/O+Pw+2lVRLFnICo9HY3UaJ9Nu/Rf3fELLI49jscG+pzBLvmon05JyTVM1f0ctiraK3VrsoY+uXNz87Ha2rzWjqhx998TAaCJJqejX+LRXjpMz6wVrhWNDDzoPphIWeXymjKsBZLCn544mlXhyqsHcnuyB6mXRf2SmXImo5rXFfFK//+rZsUavUYGhzzS4Lk9gVz+7yhHlz9PsIMLZr2m/GxxNRHZerlZX3rW5ylqcIN33nSdh+h0ZkRKWoYjRvFt5YI25aoUGL+UzqhM="

        assertTrue(YggdrasilUtil.verify(texture, signature))
    }

    fun `tainted signature`() {
        val texture = "ewogICJ0aW1lc3RhbXAiIDogMTY5MDQwMTIzODM2NCwKICAicHJvZmlsZUlkIiA6ICJhODUxNzQ0MDNlNjg0MDgwYWNkODU3MzhlMjI5NGNhZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJEYVJpdmVyc09uZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80YjJjMjAwYjA1ZWY4NDhiY2IyZGM2YTBlOGY2OTQ0MWE3YzE3Mzg4Y2FjY2UzNzAxMzg5YTU0OGM2NTdjMzZkIgogICAgfQogIH0KfQ=="
        val signature = "cP48UYYnKFJzGidMiIsMdcA0J9ZuII6UjGqJAOOyjnxj80Uf+BHWHWPJzTuUPe+hpyJrp0wdS/n6VEK6XrkAIYXtsl3gzWPTmNGk1R+R2omALX5ldT9AogdHijPTJOenDvNxn3AsDra3r9RtC9DY8jx5tC1Joeeof87bsyMgLtXbiMrpzvkp0GLujnIDTsM1a0NtHaogkBJRMyIqwc2mImUKxsYieP7oA7MSYCaTKRxGlZZ72tfcTqCqIP5NuOwbxYjgTlfHNsGTO5qRhZP972Z5rDeSDYCuSP/4cpuYdd8CVRN/LnfZ9vV6WwDSfPodLF0TGQUY7w5zSp7DbEe73gt9Xk/wbIqohAwe9m8DPJMq5mrNW1skDjjg7mwgDgL15RIU+g2MjCuxt/2WQ/vsoudsvw/O+Pw+2lVRLFnICo9HY3UaJ9Nu/Rf3fELLI49jscG+pzBLvmon05JyTVM1f0ctiraK3VrsoY+uXNz87Ha2rzWjqhx998TAaCJJqejX+LRXjpMz6wVrhWNDDzoPphIWeXymjKsBZLCn544mlXhyqsHcnuyB6mXRf2SmXImo5rXFfFK//+rZsUavUYGhzzS4Lk9gVz+7yhHlz9PsIMLZr2m/GxxNRHZerlZX3rW5ylqcIN33nSdh+h0ZkRKWoYjRvFt5YI25aoUGL+UzqhM="

        assertFalse(YggdrasilUtil.verify(texture, signature))
    }

    fun `empty signature`() {
        val texture = "ewogICJ0aW1lc3RhbXAiIDogMTY5MDQwMTIzODM2NCwKICAicHJvZmlsZUlkIiA6ICJhODUxNzQ0MDNlNjg0MDgwYWNkODU3MzhlMjI5NGNhZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJEYVJpdmVyc09uZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80YjJjMjAwYjA1ZWY4NDhiY2IyZGM2YTBlOGY2OTQ0MWE3YzE3Mzg4Y2FjY2UzNzAxMzg5YTU0OGM2NTdjMzZkIgogICAgfQogIH0KfQ=="
        val signature = ""

        assertThrows { YggdrasilUtil.verify(texture, signature) }
    }

    fun `require signature`() {
        val texture = "ewogICJ0aW1lc3RhbXAiIDogMTY5MDQwMTIzODM2NCwKICAicHJvZmlsZUlkIiA6ICJhODUxNzQ0MDNlNjg0MDgwYWNkODU3MzhlMjI5NGNhZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJEYVJpdmVyc09uZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80YjJjMjAwYjA1ZWY4NDhiY2IyZGM2YTBlOGY2OTQ0MWE3YzE3Mzg4Y2FjY2UzNzAxMzg5YTU0OGM2NTdjMzZkIgogICAgfQogIH0KfQ=="
        val signature = ""

        assertThrows { YggdrasilUtil.requireSignature(texture, signature) }
    }
}
