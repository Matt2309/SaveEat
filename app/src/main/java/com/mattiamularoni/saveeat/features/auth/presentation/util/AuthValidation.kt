package com.mattiamularoni.saveeat.features.auth.presentation.util

/**
 * Validation utilities for auth inputs.
 */
object AuthValidation {
    private const val MIN_PASSWORD_LENGTH = 6
    private val EMAIL_REGEX =
        Regex(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$",
        )

    /**
     * Validates email format.
     *
     * @param email The email to validate
     * @return true if email format is valid, false otherwise
     */
    fun isValidEmail(email: String): Boolean = email.isNotEmpty() && EMAIL_REGEX.matches(email)

    /**
     * Validates password length.
     *
     * @param password The password to validate
     * @return true if password length >= MIN_PASSWORD_LENGTH, false otherwise
     */
    fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

    /**
     * Gets email validation error message.
     *
     * @param email The email to validate
     * @return Error message if invalid, null if valid
     */
    fun getEmailError(email: String): String? =
        when {
            email.isEmpty() -> "Email is required"
            !isValidEmail(email) -> "Please enter a valid email"
            else -> null
        }

    /**
     * Gets password validation error message.
     *
     * @param password The password to validate
     * @return Error message if invalid, null if valid
     */
    fun getPasswordError(password: String): String? =
        when {
            password.isEmpty() -> "Password is required"
            !isValidPassword(password) -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
            else -> null
        }

    /**
     * Checks if email and password are both valid.
     *
     * @param email The email to validate
     * @param password The password to validate
     * @return true if both are valid, false otherwise
     */
    fun isFormValid(
        email: String,
        password: String,
    ): Boolean = isValidEmail(email) && isValidPassword(password)
}
