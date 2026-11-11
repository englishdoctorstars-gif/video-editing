package com.englishdoctor.vocabsolution

import com.englishdoctor.vocabsolution.domain.ExamRules
import com.englishdoctor.vocabsolution.domain.Placement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlacementTest {
    @Test
    fun `scores are weighted by difficulty`() {
        assertEquals(21, Placement.MAX_SCORE)
        assertEquals(3 + 2 * 2 + 3 * 1, Placement.score(mapOf(1 to 3, 2 to 2, 3 to 1)))
    }

    @Test
    fun `levels`() {
        assertEquals(Placement.BEGINNER, Placement.level(mapOf(1 to 3, 2 to 1))) // 5
        assertEquals(Placement.INTERMEDIATE, Placement.level(mapOf(1 to 3, 2 to 2))) // 7
        assertEquals(Placement.ADVANCED, Placement.level(mapOf(1 to 3, 2 to 3, 3 to 2))) // 15
        // Easy questions alone can never reach the advanced level.
        assertEquals(Placement.BEGINNER, Placement.level(mapOf(1 to 3)))
    }

    @Test
    fun `placement opens exam levels`() {
        assertFalse(ExamRules.isLevelUnlocked("intermediate", emptyMap(), Placement.BEGINNER))
        assertTrue(ExamRules.isLevelUnlocked("intermediate", emptyMap(), Placement.INTERMEDIATE))
        assertFalse(ExamRules.isLevelUnlocked("advanced", emptyMap(), Placement.INTERMEDIATE))
        assertTrue(ExamRules.isLevelUnlocked("advanced", emptyMap(), Placement.ADVANCED))
    }
}
