package com.englishdoctor.vocabsolution

import com.englishdoctor.vocabsolution.domain.Srs
import com.englishdoctor.vocabsolution.domain.Srs.Grade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SrsTest {
    private val day1 = 20_000L

    @Test
    fun `follows the 1-3-7-30 schedule when always remembered`() {
        var s = Srs.learn(day1)
        assertEquals(1, s.stage)
        assertEquals(day1 + 2, s.due) // day 3

        s = Srs.review(s, Grade.REMEMBERED, day1 + 2)
        assertEquals(2, s.stage)
        assertEquals(day1 + 6, s.due) // day 7

        s = Srs.review(s, Grade.REMEMBERED, day1 + 6)
        assertEquals(3, s.stage)
        assertEquals(day1 + 29, s.due) // day 30

        s = Srs.review(s, Grade.REMEMBERED, day1 + 29)
        assertEquals(4, s.stage)

        s = Srs.review(s, Grade.REMEMBERED, day1 + 40)
        assertEquals(Srs.MASTERED, s.stage)
        assertFalse(Srs.isDue(s, day1 + 10_000))
    }

    @Test
    fun `forgetting restarts the cycle from day 1`() {
        var s = Srs.learn(day1)
        s = Srs.review(s, Grade.REMEMBERED, day1 + 2)
        s = Srs.review(s, Grade.FORGOT, day1 + 6)
        assertEquals(1, s.stage)
        assertEquals(day1 + 7, s.due)
        assertEquals(1, s.wrong)
    }

    @Test
    fun `hard keeps the stage and asks again tomorrow`() {
        var s = Srs.review(Srs.learn(day1), Grade.REMEMBERED, day1 + 2)
        s = Srs.review(s, Grade.HARD, day1 + 6)
        assertEquals(2, s.stage)
        assertEquals(day1 + 7, s.due)
    }

    @Test
    fun `due only on or after the scheduled day`() {
        val s = Srs.learn(day1)
        assertFalse(Srs.isDue(s, day1 + 1))
        assertTrue(Srs.isDue(s, day1 + 2))
        assertTrue(Srs.isDue(s, day1 + 5))
    }
}
