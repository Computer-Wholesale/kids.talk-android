package org.linphone.ui.main.kidstalk

/**
 * Shared non-sensitive input feedback for the Kids.Talk registration extension.
 *
 * The valid extension rule is defined only by [KidsTalkContactPolicy], preventing live
 * feedback and submit validation from diverging.
 */
enum class SetupUsernameValidation {
    Empty,
    Valid,
    NonDigit,
    InvalidExtension
}

object KidsTalkSetupInputPolicy {
    fun validateUsername(value: String): SetupUsernameValidation = when {
        value.isEmpty() -> SetupUsernameValidation.Empty
        value.any { !it.isDigit() } -> SetupUsernameValidation.NonDigit
        KidsTalkContactPolicy.isValidExtension(value) -> SetupUsernameValidation.Valid
        else -> SetupUsernameValidation.InvalidExtension
    }
}
