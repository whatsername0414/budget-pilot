package com.budgetpilot.core.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

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

    /** A [ChatRole.MODEL] turn's request to invoke a tool — echoed back so a later
     * [FunctionResponse] has a matching call in history, as multi-turn function calling requires. */
    @Serializable
    @SerialName("function_call")
    data class FunctionCall(
        val name: String,
        val args: JsonObject,
    ) : MessagePart

    /** A [ChatRole.TOOL] turn carrying a tool's result (or error) back to the model. */
    @Serializable
    @SerialName("function_response")
    data class FunctionResponse(
        val name: String,
        val response: JsonObject,
    ) : MessagePart
}
