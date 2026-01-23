package com.jetbrains.otp.exporter

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.jetbrains.otp.exporter.processor.SessionProcessor
import com.jetbrains.otp.exporter.processor.SpanProcessor
import com.jetbrains.otp.exporter.processor.SpanProcessorProvider
import com.jetbrains.otp.settings.SpanFilterService
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.concurrent.TimeUnit

@Service(Service.Level.APP)
class TelemetrySpanExporter {
    private val spanExporter: SpanExporter? by lazy { OtlpSpanExporterFactory.create() }

    private val sessionProcessor = SessionProcessor

    private val processors = createProcessors()

    private fun createProcessors(): List<SpanProcessor> {
        val processors = mutableListOf<SpanProcessor>(sessionProcessor)

        SpanProcessorProvider.EP_NAME.extensionList.forEach { provider ->
            processors.addAll(provider.getProcessors())
        }
        return processors.sortedBy { it.getOrder() }
    }

    fun sendSpans(spanData: Collection<SpanData>) {
        val filteredSpans = filterSpans(spanData)
        if (filteredSpans.isEmpty()) return

        val processedSpans = processSpans(filteredSpans)

        if (processedSpans.isNotEmpty()) {
            doExport(processedSpans)
        }
    }

    private fun processSpans(spans: Collection<SpanData>): Collection<SpanData> {
        return processors.fold(spans) { currentSpans, processor ->
            processor.process(currentSpans)
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
        fun getInstance(): TelemetrySpanExporter = service()
    }
}
