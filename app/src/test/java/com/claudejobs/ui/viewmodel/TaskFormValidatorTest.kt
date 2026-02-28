package com.claudejobs.ui.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [TaskFormValidator].
 *
 * These are pure JVM tests — no Android runtime, Compose, or coroutines needed.
 */
class TaskFormValidatorTest {

    // ── nameError ─────────────────────────────────────────────────────────────

    @Test
    fun `blank name sets nameError`() {
        val result = TaskFormValidator.validate(name = "", prompt = "do something")
        assertTrue(result.nameError)
    }

    @Test
    fun `whitespace-only name sets nameError`() {
        val result = TaskFormValidator.validate(name = "   ", prompt = "do something")
        assertTrue(result.nameError)
    }

    @Test
    fun `non-blank name clears nameError`() {
        val result = TaskFormValidator.validate(name = "My task", prompt = "do something")
        assertFalse(result.nameError)
    }

    // ── promptError ───────────────────────────────────────────────────────────

    @Test
    fun `blank prompt sets promptError`() {
        val result = TaskFormValidator.validate(name = "My task", prompt = "")
        assertTrue(result.promptError)
    }

    @Test
    fun `whitespace-only prompt sets promptError`() {
        val result = TaskFormValidator.validate(name = "My task", prompt = "\t\n")
        assertTrue(result.promptError)
    }

    @Test
    fun `non-blank prompt clears promptError`() {
        val result = TaskFormValidator.validate(name = "My task", prompt = "Summarise the news")
        assertFalse(result.promptError)
    }

    // ── isValid ───────────────────────────────────────────────────────────────

    @Test
    fun `isValid is false when both fields are blank`() {
        val result = TaskFormValidator.validate(name = "", prompt = "")
        assertFalse(result.isValid)
        assertTrue(result.nameError)
        assertTrue(result.promptError)
    }

    @Test
    fun `isValid is false when only name is blank`() {
        val result = TaskFormValidator.validate(name = "", prompt = "A prompt")
        assertFalse(result.isValid)
        assertTrue(result.nameError)
        assertFalse(result.promptError)
    }

    @Test
    fun `isValid is false when only prompt is blank`() {
        val result = TaskFormValidator.validate(name = "My task", prompt = "")
        assertFalse(result.isValid)
        assertFalse(result.nameError)
        assertTrue(result.promptError)
    }

    @Test
    fun `isValid is true when both fields have content`() {
        val result = TaskFormValidator.validate(name = "Morning report", prompt = "Give me a news summary")
        assertTrue(result.isValid)
        assertFalse(result.nameError)
        assertFalse(result.promptError)
    }
}
