package com.jetbrains.otp.exporter

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import io.opentelemetry.sdk.metrics.export.MetricExporter

@Service(Service.Level.APP)
class TelemetryMetricExporter {
    val bufferingExporter = BufferingMetricExporter()

    suspend fun initExporter(config: OtlpConfig) {
        try {
            val exporter = OtlpMetricExporterFactory.create(config)
            if (exporter != null) {
                bufferingExporter.setDelegate(exporter)
            }
        } catch (e: Exception) {
        }
    }

    companion object {
        fun getInstance(): TelemetryMetricExporter = service()
    }
}