package com.jetbrains.otp.exporter.processor

import com.intellij.openapi.extensions.ExtensionPointName

interface SpanProcessorProvider {
    fun getProcessors(): List<SpanProcessor>

    companion object {
        val EP_NAME = ExtensionPointName.create<SpanProcessorProvider>(
            "com.jetbrains.otp.diagnostic.spanProcessorProvider"
        )
    }
}