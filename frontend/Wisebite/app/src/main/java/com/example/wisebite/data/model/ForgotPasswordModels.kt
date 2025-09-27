package com.example.wisebite.data.model

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val reset_code: String,
    val new_password: String
)

data class MessageResponse(
    val message: String
)