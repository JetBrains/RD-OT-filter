package com.jetbrains.otp.crypto

import com.jetbrains.otp.crypto.rpc.CryptoRpc

internal class CryptoRpcImpl : CryptoRpc {
    private val cryptoService = BackendCryptoService.getInstance()

    override suspend fun requestKeyExchange(clientPublicKey: String): EncryptedAesKey {
        return cryptoService.encryptAesKeyForClient(clientPublicKey)
    }

    override suspend fun sendEncryptedData(data: EncryptedData): String {
        return cryptoService.decryptData(data)
    }

    override suspend fun getEncryptedHoneycombApiKey(): EncryptedData {
        val apiKey = System.getProperty("honeycomb.api.key")
            ?: System.getenv("HONEYCOMB_API_KEY")
            ?: throw IllegalStateException("Honeycomb API key not configured on backend")

        return cryptoService.encryptData(apiKey)
    }
}