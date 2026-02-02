package com.jetbrains.otp.exporter

class RemoteOtlpConfigProvider : OtlpConfigProvider {
    override fun createConfig(): OtlpConfig {
        return PropagateFromBackendOtlpConfig()
    }
}