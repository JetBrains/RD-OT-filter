package com.jetbrains.otp.exporter

import com.intellij.openapi.diagnostic.Logger
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter
import io.opentelemetry.sdk.metrics.export.MetricExporter
import java.util.concurrent.TimeUnit

object OtlpMetricExporterFactory {
    private val LOG = Logger.getInstance(OtlpMetricExporterFactory::class.java)

    fun create(config: OtlpConfig = OtlpConfig.fromEnvironment()): MetricExporter? {
        if (config.apiKey.isNullOrBlank()) {
            LOG.warn("Honeycomb API key not configured. Set HONEYCOMB_API_KEY environment variable or honeycomb.api.key system property.")
            return null
        }

        return try {
            OtlpHttpMetricExporter.builder()
                .setEndpoint("https://api.honeycomb.io/v1/metrics")
                .addHeader("x-honeycomb-team", config.apiKey)
                .addHeader("x-honeycomb-dataset", config.dataset)
                .setTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            LOG.error("Failed to initialize Honeycomb metric exporter", e)
            null
        }
    }
}