package com.jetbrains.otp.settings

import com.intellij.openapi.components.Service

@Service(Service.Level.APP)
class BackendSpanFilterService : SpanFilterService {
    override fun isSpanEnabled(spanName: String): Boolean {
        return BackendOtpDiagnosticSettings.getInstance().isSpanEnabled(spanName)
    }
}
