package com.englishdoctor.vocabsolution

import com.englishdoctor.vocabsolution.domain.ExamRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamRulesTest {
    @Test
    fun `primary is always open`() {
        assertTrue(ExamRules.isSetUnlocked(1, emptyMap()))
        assertTrue(ExamRules.isSetUnlocked(3, emptyMap()))
        assertFalse(ExamRules.isSetUnlocked(4, emptyMap()))
    }

    @Test
    fun `more than 20 of 25 unlocks the next level only`() {
        assertFalse(ExamRules.isSetUnlocked(4, mapOf(1 to 20)))
        assertTrue(ExamRules.isSetUnlocked(4, mapOf(2 to 21)))
        assertFalse(ExamRules.isSetUnlocked(8, mapOf(2 to 25)))
        assertTrue(ExamRules.isSetUnlocked(8, mapOf(2 to 25, 6 to 22)))
    }

    @Test
    fun `levels match the book`() {
        assertEquals("primary", ExamRules.levelOf(3))
        assertEquals("intermediate", ExamRules.levelOf(7))
        assertEquals("advanced", ExamRules.levelOf(8))
    }
}
