package top.stellortus.stellar_music

import org.junit.Assert.assertEquals
import org.junit.Test
import top.stellortus.stellar_music.ui.screens.uploadCacheFileName

class UploadFileNameTest {
    @Test fun acceptedNamesArePreservedWithoutGeneratingAFallback() {
        listOf("track.mp3", "歌曲.flac", " track.mp3", " ").forEach { name ->
            assertEquals(name, uploadCacheFileName(name) { error("Unexpected fallback") })
        }
    }

    @Test fun missingEmptyAndPunctuationPrefixedNamesGenerateExactlyOneFallback() {
        listOf(null, "", ".hidden.mp3", "_track.mp3").forEach { name ->
            var generated = 0
            assertEquals("music-123.mp3", uploadCacheFileName(name) {
                generated++
                "music-123.mp3"
            })
            assertEquals(1, generated)
        }
    }
}
