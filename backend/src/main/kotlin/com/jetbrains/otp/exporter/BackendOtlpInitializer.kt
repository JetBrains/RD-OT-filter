package com.jetbrains.otp.exporter

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class BackendOtlpInitializer : ProjectActivity {
    override suspend fun execute(project: Project) {
        val config = FromEnvOtlpConfig()
        TelemetrySpanExporter.getInstance().initExporter(config)
        TelemetryMetricExporter.getInstance().initExporter(config)
    }
}