package com.jetbrains.otp.settings

import com.intellij.openapi.components.service

interface SpanFilterService {
    fun isSpanEnabled(spanName: String): Boolean

    companion object {
        fun getInstance(): SpanFilterService = service<SpanFilterService>()
    }
}

