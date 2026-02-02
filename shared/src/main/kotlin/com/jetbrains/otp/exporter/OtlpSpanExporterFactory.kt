package com.jetbrains.otp.exporter

import com.intellij.openapi.diagnostic.Logger
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.concurrent.TimeUnit

interface OtlpConfig {
    suspend fun initialize()

    val dataset: String
    val endpoint: String
    val timeoutSeconds: Long

    fun getApiKey(): String?
}

class FromEnvOtlpConfig(
    override val dataset: String = System.getProperty("honeycomb.dataset")
        ?: System.getenv("HONEYCOMB_DATASET")
        ?: "intellij-plugin",
    override val endpoint: String = "https://api.honeycomb.io/v1/traces",
    override val timeoutSeconds: Long = 10
) : OtlpConfig {
    private var apiKey: String? = null

    override suspend fun initialize() {
        apiKey = System.getProperty("honeycomb.api.key")
            ?: System.getenv("HONEYCOMB_API_KEY")
    }

    override fun getApiKey(): String? = apiKey
}

object OtlpSpanExporterFactory {
    private val LOG = Logger.getInstance(OtlpSpanExporterFactory::class.java)

    suspend fun create(config: OtlpConfig): SpanExporter? {
        config.initialize()

        val apiKey = config.getApiKey()
        if (apiKey.isNullOrBlank()) {
            LOG.warn("Honeycomb API key not configured. Set HONEYCOMB_API_KEY environment variable or honeycomb.api.key system property.")
            return null
        }

        return try {
            OtlpHttpSpanExporter.builder()
                .setEndpoint(config.endpoint)
                .addHeader("x-honeycomb-team", apiKey)
                .addHeader("x-honeycomb-dataset", config.dataset)
                .setTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            LOG.error("Failed to initialize Honeycomb exporter", e)
            null
        }
    }
}