package com.jetbrains.otp.exporter

import io.opentelemetry.sdk.metrics.export.MetricExporter

class FrontendMetricExporter : FilteredMetricExporterProvider() {
    override fun getUnderlyingExporter(): MetricExporter {
        return TelemetryMetricExporter.getInstance().bufferingExporter
    }
}