package com.example.alone.error

sealed class ErrorMapper {

    companion object {

        @JvmStatic
        fun mapErrorToUserMessage(
            httpStatusCode: Int? = null,
            errorCode: String? = null,
            errorMessage: String? = null,
            username: String? = null,
            profileName: String? = null
        ): String {
            // 1) HTTP status codes
            val statusMessage = when (httpStatusCode) {
                200, 201 -> "Success."
                400 -> "Invalid input. Please check your details."
                401 -> "Incorrect email or password. Please try again."
                403 -> "Account access denied. Contact support."
                404 -> "Account not found. Please check your details."
                408 -> "The request took too long. Please try again."
                409 -> "This email or username is already in use."
                429 -> "Too many attempts. Please wait a few minutes."
                500 -> "The server had a problem. Please try again later."
                502, 503, 504 -> "The service is temporarily unavailable. Please try again later."
                else -> null
            }
            if (statusMessage != null) return statusMessage

            // 2) Backend application error codes (if your API sends them)
            return when (errorCode?.uppercase()) {
                "EMAIL_ALREADY_EXISTS" -> "This Gmail is already registered."
                "USERNAME_TAKEN" -> "Username '${username ?: "your username"}' is already taken."
                "INVALID_CREDENTIALS" -> "Incorrect email or password."
                "ACCOUNT_LOCKED" -> "Account locked due to many failed attempts. Try again later."
                "EMAIL_NOT_VERIFIED" -> "Please verify your Gmail address first."
                "PASSWORD_RESET_REQUIRED" -> "You need to reset your password. Check your email."
                "SESSION_EXPIRED" -> "Your session has expired. Please login again."
                "TOO_MANY_REQUESTS" -> "Too many attempts. Please wait a few minutes."
                "PROFILE_NAME_TAKEN" -> "Profile name '${profileName ?: "your profile name"}' is already in use."
                "NETWORK_ERROR" -> "No internet connection. Please check your network."
                else -> {
                    // 3) Smart fallback based on raw backend message
                    when {
                        errorMessage?.contains("password", ignoreCase = true) == true ->
                            "There is a problem with the password. Please use a stronger one."
                        errorMessage?.contains("email", ignoreCase = true) == true ->
                            "There is a problem with the email. Please check it and try again."
                        errorMessage?.contains("unauthorized", ignoreCase = true) == true ||
                                errorMessage?.contains("invalid credentials", ignoreCase = true) == true ->
                            "Incorrect email or password. Please try again."
                        errorMessage?.contains("duplicate", ignoreCase = true) == true ->
                            "This information is already registered."
                        else -> "Something went wrong. Please try again."
                    }
                }
            }
        }

        @JvmStatic
        fun mapHttpStatusToMessage(httpStatusCode: Int): String {
            return mapErrorToUserMessage(httpStatusCode = httpStatusCode)
        }

        @JvmStatic
        fun mapErrorCodeToMessage(errorCode: String): String {
            return mapErrorToUserMessage(errorCode = errorCode)
        }
    }
}
