package com.jetbrains.otp.exporter

import com.intellij.openapi.diagnostic.Logger
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.metrics.InstrumentType
import io.opentelemetry.sdk.metrics.data.AggregationTemporality
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.export.MetricExporter
import kotlin.collections.forEach

class FrontendMetricExporter : FilteredMetricExporterProvider() {
    override fun getUnderlyingExporter(): MetricExporter {
        return OtlpMetricExporterFactory.create() ?: throw IllegalStateException("Metric exporter not initialized")
    }
}