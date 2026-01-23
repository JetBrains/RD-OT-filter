package com.jetbrains.otp.exporter.processor

import io.opentelemetry.sdk.trace.data.SpanData

interface SpanProcessor {
    fun process(spans: Collection<SpanData>): Collection<SpanData>

    fun getOrder(): Int
}