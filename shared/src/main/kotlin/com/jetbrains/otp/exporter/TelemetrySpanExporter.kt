package com.jetbrains.otp.exporter

import com.intellij.openapi.diagnostic.Logger
import com.intellij.platform.diagnostic.telemetry.impl.TelemetryReceivedListener
import com.jetbrains.otp.settings.SpanFilterService
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

@Suppress("UnstableApiUsage")
class TelemetrySpanExporter : TelemetryReceivedListener {
    private val spanExporter: SpanExporter? by lazy { OtlpSpanExporterFactory.create() }
    @Volatile
    private var isSessionInitialized = false
    private val bufferedSpans = CopyOnWriteArrayList<SpanData>()

    override fun sendSpans(spanData: Collection<SpanData>) {
        exportSpans(spanData)
    }

    fun onSessionInitialized() {
        val spansToFlush = synchronized(this) {
            if (isSessionInitialized) {
                LOG.debug("Session already initialized, ignoring duplicate call")
                return
            }
            isSessionInitialized = true
            val spans = bufferedSpans.toList()
            bufferedSpans.clear()
            spans
        }

        if (spansToFlush.isNotEmpty()) {
            LOG.info("Session initialized. Flushing ${spansToFlush.size} buffered spans")
            doExport(spansToFlush)
        } else {
            LOG.debug("Session initialized. No buffered spans to flush")
        }
    }

    private fun exportSpans(spanData: Collection<SpanData>) {
        val filteredSpans = filterSpans(spanData)
        if (filteredSpans.isEmpty()) return

        val spansToExport = synchronized(this) {
            bufferedSpans.addAll(filteredSpans)
            if (isSessionInitialized) {
                val spans = bufferedSpans.toList()
                bufferedSpans.clear()
                return@synchronized spans
            } else {
                LOG.debug("Session not initialized. Buffered ${filteredSpans.size} spans (total buffered: ${bufferedSpans.size})")
                return
            }
        }

        if (spansToExport.isNotEmpty()) {
            doExport(spansToExport)
        }
    }

    private fun doExport(spans: Collection<SpanData>) {
        val exporter = spanExporter
        if (exporter == null) {
            LOG.debug("Honeycomb exporter not initialized. Spans will not be sent.")
            return
        }

        try {
            val result = exporter.export(spans)
            result.join(5, TimeUnit.SECONDS)
            if (!result.isSuccess) {
                LOG.warn("Failed to export spans to Honeycomb: $result")
            }
        } catch (e: Exception) {
            LOG.warn("Error exporting spans to Honeycomb", e)
        }
    }

    private fun filterSpans(spans: Collection<SpanData>): Collection<SpanData> {
        val filterService = SpanFilterService.getInstance()
        return spans.filter { filterService.isSpanEnabled(it.name) }
    }

    companion object {
        private val LOG = Logger.getInstance(TelemetrySpanExporter::class.java)
    }
}
