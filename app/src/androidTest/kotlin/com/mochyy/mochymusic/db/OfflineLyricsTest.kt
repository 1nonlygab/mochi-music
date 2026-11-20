package com.mochyy.mochymusic.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mochyy.mochymusic.db.entities.LyricsEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineLyricsTest {
    private lateinit var room: InternalDatabase
    private lateinit var database: MusicDatabase

    @Before fun setUp() {
        room = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), InternalDatabase::class.java)
            .allowMainThreadQueries().build()
        database = MusicDatabase(room)
    }

    @After fun tearDown() = room.close()

    @Test fun insertsMissingLyrics() = runBlocking {
        assertTrue(database.saveFetchedLyricsIfMissing(LyricsEntity("one", "lyrics", "source")))
        assertEquals("lyrics", database.lyrics("one").first()?.lyrics)
    }

    @Test fun replacesNotFoundButPreservesExistingLyricsAndTranslations() = runBlocking {
        database.upsert(LyricsEntity("one", LyricsEntity.LYRICS_NOT_FOUND))
        assertTrue(database.saveFetchedLyricsIfMissing(LyricsEntity("one", "found", "source")))
        database.upsert(LyricsEntity("one", "edited", "UserEdited", "translation", "en", "plain"))
        assertFalse(database.saveFetchedLyricsIfMissing(LyricsEntity("one", "other", "source")))
        val kept = database.lyrics("one").first()
        assertEquals("edited", kept?.lyrics)
        assertEquals("UserEdited", kept?.provider)
        assertEquals("translation", kept?.translatedLyrics)
    }
}
