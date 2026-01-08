package com.jetbrains.otp.settings

import com.jetbrains.otp.settings.api.OtpDiagnosticSettingsApi

internal class BackendOtpDiagnosticSettingsApiImpl : OtpDiagnosticSettingsApi {
    override suspend fun syncDisabledGroups(disabledGroups: Set<String>) {
        BackendOtpDiagnosticSettings.getInstance().syncDisabledGroups(disabledGroups)
    }
}
