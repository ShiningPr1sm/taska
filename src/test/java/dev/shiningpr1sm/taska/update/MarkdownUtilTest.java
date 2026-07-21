package dev.shiningpr1sm.taska.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownUtilTest {

    @Test
    void nullReturnsFallback() {
        assertEquals("(no release notes)", MarkdownUtil.toPlainText(null));
    }

    @Test
    void emptyStringReturnsFallback() {
        assertEquals("(no release notes)", MarkdownUtil.toPlainText(""));
    }

    @Test
    void blankStringReturnsFallback() {
        assertEquals("(no release notes)", MarkdownUtil.toPlainText("   "));
    }

    @Test
    void stripsHeading() {
        assertEquals("Hello", MarkdownUtil.toPlainText("# Hello"));
    }

    @Test
    void stripsH2Heading() {
        assertEquals("Title", MarkdownUtil.toPlainText("## Title"));
    }

    @Test
    void stripsH6Heading() {
        assertEquals("Deep", MarkdownUtil.toPlainText("###### Deep"));
    }

    @Test
    void stripsBold() {
        assertEquals("bold text", MarkdownUtil.toPlainText("**bold text**"));
    }

    @Test
    void stripsItalic() {
        assertEquals("italic text", MarkdownUtil.toPlainText("*italic text*"));
    }

    @Test
    void stripsInlineCode() {
        assertEquals("code here", MarkdownUtil.toPlainText("`code here`"));
    }

    @Test
    void convertsBulletList() {
        String md = "- item one\n- item two";
        String result = MarkdownUtil.toPlainText(md);
        assertTrue(result.contains("• item one"));
        assertTrue(result.contains("• item two"));
    }

    @Test
    void convertsAsteriskBulletList() {
        String md = "* alpha\n* beta";
        String result = MarkdownUtil.toPlainText(md);
        assertTrue(result.contains("• alpha"));
        assertTrue(result.contains("• beta"));
    }

    @Test
    void mixedFormatting() {
        String md = "## Changelog\n\n- **Fixed** a `bug`\n- *Minor* fix";
        String result = MarkdownUtil.toPlainText(md);
        assertFalse(result.contains("##"));
        assertTrue(result.contains("Fixed"));
        assertFalse(result.contains("**"));
        assertTrue(result.contains("bug"));
        assertTrue(result.contains("•"));
    }

    @Test
    void plainTextPassesThrough() {
        assertEquals("just plain text", MarkdownUtil.toPlainText("just plain text"));
    }
}
