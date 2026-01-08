package com.jetbrains.otp.settings

import com.intellij.openapi.components.Service

@Service(Service.Level.APP)
class FrontendSpanFilterService : SpanFilterService {
    override fun isSpanEnabled(spanName: String): Boolean {
        return OtpDiagnosticSettings.getInstance().isSpanEnabled(spanName)
    }
}
