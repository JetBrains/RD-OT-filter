package com.jetbrains.otp.exporter

import com.jetbrains.otp.exporter.processor.SessionProcessor
import com.jetbrains.otp.span.SessionSpanListener

class ClientSessionSpanListener : SessionSpanListener {
    override fun sessionSpanInitialized(spanId: String, traceId: String) {
        SessionProcessor.onSessionInitialized(spanId, traceId)
    }
}