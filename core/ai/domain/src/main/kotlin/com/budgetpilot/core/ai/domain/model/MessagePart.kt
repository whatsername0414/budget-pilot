package com.budgetpilot.core.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface MessagePart {
    @Serializable
    @SerialName("text")
    data class Text(
        val text: String,
    ) : MessagePart

    @Serializable
    @SerialName("image")
    data class Image(
        val mimeType: String,
        val base64Data: String,
    ) : MessagePart
}
