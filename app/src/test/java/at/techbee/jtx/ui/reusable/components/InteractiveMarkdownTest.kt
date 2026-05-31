package at.techbee.jtx.ui.reusable.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InteractiveMarkdownTest {

    @Test
    fun `parseMarkdownSegments keeps markdown and checkboxes in original order`() {
        val content = """
            # Heading

            Intro text
            - [ ] Task 1
            Between text
            - [x] Task 2
            Closing text
        """.trimIndent()

        val segments = parseMarkdownSegments(content)

        assertEquals(5, segments.size)
        assertEquals(
            MarkdownSegment.MarkdownText(
                """
                    # Heading

                    Intro text
                """.trimIndent()
            ),
            segments[0]
        )
        assertEquals(MarkdownSegment.CheckboxItem(3, "Task 1", false), segments[1])
        assertEquals(MarkdownSegment.MarkdownText("Between text"), segments[2])
        assertEquals(MarkdownSegment.CheckboxItem(5, "Task 2", true), segments[3])
        assertEquals(MarkdownSegment.MarkdownText("Closing text"), segments[4])
    }

    @Test
    fun `parseMarkdownSegments handles checklist on final line without duplicate markdown`() {
        val content = """
            Intro text
            - [ ] Final task
        """.trimIndent()

        val segments = parseMarkdownSegments(content)

        assertEquals(2, segments.size)
        assertEquals(MarkdownSegment.MarkdownText("Intro text"), segments[0])
        assertEquals(MarkdownSegment.CheckboxItem(1, "Final task", false), segments[1])
    }

    @Test
    fun `updateCheckboxState toggles requested line and preserves indentation`() {
        val content = """
            - [ ] Task 1
              - [x] Nested task
            - [ ] Task 3
        """.trimIndent()

        val result = updateCheckboxState(content, 1, false)

        assertEquals(
            """
                - [ ] Task 1
                  - [ ] Nested task
                - [ ] Task 3
            """.trimIndent(),
            result
        )
    }

    @Test
    fun `updateCheckboxState ignores non checkbox lines`() {
        val content = """
            Intro text
            - [ ] Task 1
        """.trimIndent()

        val result = updateCheckboxState(content, 0, true)

        assertEquals(content, result)
    }

    @Test
    fun `parseMarkdownSegments returns only checkbox items for pure checklist`() {
        val content = """
            - [ ] Task 1
            - [X] Task 2
        """.trimIndent()

        val segments = parseMarkdownSegments(content)

        assertEquals(2, segments.size)
        assertTrue(segments[0] is MarkdownSegment.CheckboxItem)
        assertTrue(segments[1] is MarkdownSegment.CheckboxItem)
        assertEquals(MarkdownSegment.CheckboxItem(0, "Task 1", false), segments[0])
        assertEquals(MarkdownSegment.CheckboxItem(1, "Task 2", true), segments[1])
    }

    @Test
    fun `filterCheckedCheckboxes removes checked items and keeps everything else`() {
        val content = """
            # Heading
            - [ ] Task 1
            - [x] Task 2
            - [X] Task 3
            Some text
              - [ ] Nested open
              - [x] Nested done
            End.
        """.trimIndent()

        val result = filterCheckedCheckboxes(content)

        val expected = """
            # Heading
            - [ ] Task 1
            Some text
              - [ ] Nested open
            End.
        """.trimIndent()

        assertEquals(expected, result)
    }
}
