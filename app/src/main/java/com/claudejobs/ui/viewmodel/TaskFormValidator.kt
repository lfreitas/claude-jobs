package com.claudejobs.ui.viewmodel

/**
 * Pure validation logic for the Add/Edit Task form, extracted from [AddEditTaskViewModel]
 * so it can be tested without any Android or Compose dependencies.
 */
object TaskFormValidator {

    data class Result(val nameError: Boolean, val promptError: Boolean) {
        val isValid: Boolean get() = !nameError && !promptError
    }

    fun validate(name: String, prompt: String) = Result(
        nameError  = name.isBlank(),
        promptError = prompt.isBlank()
    )
}
