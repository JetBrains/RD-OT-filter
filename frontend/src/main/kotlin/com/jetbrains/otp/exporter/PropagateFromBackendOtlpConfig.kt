package com.jetbrains.otp.exporter

import com.intellij.openapi.diagnostic.Logger
import com.jetbrains.otp.crypto.FrontendCryptoClient
import com.jetbrains.otp.crypto.rpc.CryptoRpc

class PropagateFromBackendOtlpConfig(
    override val dataset: String = System.getProperty("honeycomb.dataset")
        ?: System.getenv("HONEYCOMB_DATASET")
        ?: "intellij-plugin",
    override val endpoint: String = "https://api.honeycomb.io/v1/traces",
    override val timeoutSeconds: Long = 10
) : OtlpConfig {
    private val cryptoClient = FrontendCryptoClient.getInstance()
    private var apiKey: String? = null

    override suspend fun initialize() {
        if (apiKey != null) return
        try {
            if (!cryptoClient.isInitialized()) {
                cryptoClient.initialize()
            }

            val cryptoRpc = CryptoRpc.getInstance()
            val encryptedApiKey = cryptoRpc.getEncryptedHoneycombApiKey()

            apiKey = cryptoClient.decryptData(encryptedApiKey)
        } catch (e: Exception) {
            LOG.error("Failed to initialize OTLP config from backend", e)
            throw e
        }
    }

    override fun getApiKey(): String? = apiKey

    companion object {
        private val LOG = Logger.getInstance(PropagateFromBackendOtlpConfig::class.java)
    }
}