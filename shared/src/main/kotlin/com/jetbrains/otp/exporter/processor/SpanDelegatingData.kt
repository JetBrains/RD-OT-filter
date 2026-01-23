package com.jetbrains.otp.exporter.processor

import io.opentelemetry.api.trace.SpanContext
import io.opentelemetry.sdk.common.InstrumentationScopeInfo
import io.opentelemetry.sdk.trace.data.EventData
import io.opentelemetry.sdk.trace.data.SpanData


class SpanDelegatingData(
    private val delegate: SpanData,
    private val newParent: SpanContext,
    private val additionalEvents: List<EventData>
) : SpanData by delegate {

    override fun getTraceId(): String = newParent.traceId
    override fun getSpanId(): String = delegate.spanId
    override fun getParentSpanContext(): SpanContext = newParent
    override fun getParentSpanId(): String = newParent.spanId
    override fun getInstrumentationScopeInfo(): InstrumentationScopeInfo = delegate.instrumentationScopeInfo

    override fun getEvents(): List<EventData> {
        return if (additionalEvents.isEmpty()) {
            delegate.events
        } else {
            delegate.events + additionalEvents
        }
    }
}