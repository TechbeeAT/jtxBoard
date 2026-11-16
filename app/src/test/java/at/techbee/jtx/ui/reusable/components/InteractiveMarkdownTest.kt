package at.techbee.jtx.ui.reusable.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

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

    @Test
    fun `filterCheckedCheckboxes handles CRLF line endings correctly`() {
        // CRLF line endings (\r\n) are common in synced iCalendar descriptions
        val content = "# Heading\r\n- [ ] Task 1\r\n- [x] Task 2\r\n- [X] Task 3\r\nSome text\r\n  - [ ] Nested open\r\n  - [x] Nested done\r\nEnd."

        val result = filterCheckedCheckboxes(content)

        val expected = "# Heading\n- [ ] Task 1\nSome text\n  - [ ] Nested open\nEnd."

        assertEquals(expected, result)
    }

    @Test
    fun `filterCheckedCheckboxes handles multiple list markers correctly`() {
        val content = """
            - [x] Checked dash
            * [x] Checked star
            + [x] Checked plus
            - [ ] Unchecked dash
            * [ ] Unchecked star
            + [ ] Unchecked plus
        """.trimIndent()

        val result = filterCheckedCheckboxes(content)

        val expected = """
            - [ ] Unchecked dash
            * [ ] Unchecked star
            + [ ] Unchecked plus
        """.trimIndent()

        assertEquals(expected, result)
    }

    @Test
    fun `updateCheckboxState preserves original list markers`() {
        val content = """
            - [ ] Dash task
            * [x] Star task
            + [ ] Plus task
        """.trimIndent()

        val result1 = updateCheckboxState(content, 0, true)
        val result2 = updateCheckboxState(result1, 1, false)

        val expected = """
            - [x] Dash task
            * [ ] Star task
            + [ ] Plus task
        """.trimIndent()

        assertEquals(expected, result2)
    }

    @Test
    fun moveCheckedCheckboxesToBottom_sortsEachGroupWithinRun() {
        val content = """
            Intro
            - [x] C
            - [ ] D
            - [x] A
            - [ ] B

            - [x] H
            - [ ] F
            - [x] G
            - [ ] E
            Middle
            - [x] I
            - [ ] J
        """.trimIndent()

        val result = moveCheckedCheckboxesToBottom(parseMarkdownSegments(content), Locale.ENGLISH)
        val order = result.map {
            when (it) {
                is MarkdownSegment.MarkdownText -> it.content
                is MarkdownSegment.CheckboxItem -> it.text
            }
        }

        assertEquals(
            listOf("Intro", "B", "D", "A", "C", "", "E", "F", "G", "H", "Middle", "J", "I"),
            order
        )
    }

    @Test
    fun moveCheckedCheckboxesToBottom_keepsOriginalLineIndex() {
        val content = "- [x] A\n- [ ] D\n  * [ ] B"
        val segments = parseMarkdownSegments(content)
        val result = moveCheckedCheckboxesToBottom(segments, Locale.ENGLISH)
        val first = result[0] as MarkdownSegment.CheckboxItem

        assertEquals("B", first.text)
        assertEquals(2, first.lineIndex)
        val checkedContent = updateCheckboxState(content, first.lineIndex, true)
        assertEquals("- [x] A\n- [ ] D\n  * [x] B", checkedContent)
        assertEquals(
            listOf("A", "D", "B"),
            segments.filterIsInstance<MarkdownSegment.CheckboxItem>().map { it.text }
        )

        val checkedSegments = moveCheckedCheckboxesToBottom(parseMarkdownSegments(checkedContent), Locale.ENGLISH)
        assertEquals(
            listOf("D", "A", "B"),
            checkedSegments.filterIsInstance<MarkdownSegment.CheckboxItem>().map { it.text }
        )
        val checkedB = checkedSegments.last() as MarkdownSegment.CheckboxItem
        assertEquals(content, updateCheckboxState(checkedContent, checkedB.lineIndex, false))
    }

    @Test
    fun moveCheckedCheckboxesToBottom_usesCzechCollationForBothGroups() {
        val words = listOf("Žampiony", "Chléb", "Hrušky", "Čaj", "Cibule")
        val expected = listOf("Cibule", "Čaj", "Hrušky", "Chléb", "Žampiony")
        val content = listOf("x", " ").flatMap { state ->
            words.map { "- [$state] $it" }
        }.joinToString("\n")

        val result = moveCheckedCheckboxesToBottom(
            parseMarkdownSegments(content), Locale.forLanguageTag("cs-CZ")
        ).filterIsInstance<MarkdownSegment.CheckboxItem>()

        assertEquals(expected + expected, result.map { it.text })
        assertEquals(List(5) { false } + List(5) { true }, result.map { it.isChecked })
    }

    @Test
    fun moveCheckedCheckboxesToBottom_changesAlphabeticalOrderWithLocale() {
        val segments = parseMarkdownSegments("- [ ] Öl\n- [ ] Zucchini\n- [ ] Äpple")
        val german = moveCheckedCheckboxesToBottom(segments, Locale.GERMAN)
        val swedish = moveCheckedCheckboxesToBottom(segments, Locale.forLanguageTag("sv-SE"))

        assertEquals(
            listOf("Äpple", "Öl", "Zucchini"),
            german.filterIsInstance<MarkdownSegment.CheckboxItem>().map { it.text }
        )
        assertEquals(
            listOf("Zucchini", "Äpple", "Öl"),
            swedish.filterIsInstance<MarkdownSegment.CheckboxItem>().map { it.text }
        )
    }

    @Test
    fun moveCheckedCheckboxesToBottom_ignoresCaseAndKeepsEqualItemsStable() {
        val content = """
            - [x] bread
            - [ ] bread
            - [ ] Bread
            - [ ] apples
            - [ ] APPLES
            - [x] Bread
        """.trimIndent()

        val result = moveCheckedCheckboxesToBottom(
            parseMarkdownSegments(content), Locale.ENGLISH
        ).filterIsInstance<MarkdownSegment.CheckboxItem>()

        assertEquals(
            listOf("apples", "APPLES", "bread", "Bread", "bread", "Bread"),
            result.map { it.text }
        )
        assertEquals(listOf(3, 4, 1, 2, 0, 5), result.map { it.lineIndex })
    }

    @Test
    fun moveCheckedCheckboxesToBottom_preservesEmptyAndNonChecklistContent() {
        assertEquals(
            emptyList<MarkdownSegment>(),
            moveCheckedCheckboxesToBottom(emptyList(), Locale.ENGLISH)
        )
        val segments = parseMarkdownSegments("# Heading\n\nSome text")
        assertEquals(segments, moveCheckedCheckboxesToBottom(segments, Locale.ENGLISH))
    }
}
