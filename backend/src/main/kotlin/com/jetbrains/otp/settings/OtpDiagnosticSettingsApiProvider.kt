package com.jetbrains.otp.settings

import com.intellij.platform.rpc.backend.RemoteApiProvider
import com.jetbrains.otp.api.OtpDiagnosticSettingsApi
import fleet.rpc.remoteApiDescriptor

internal class OtpDiagnosticSettingsApiProvider : RemoteApiProvider {
    override fun RemoteApiProvider.Sink.remoteApis() {
        remoteApi(remoteApiDescriptor<OtpDiagnosticSettingsApi>()) {
            BackendOtpDiagnosticSettingsApiImpl()
        }
    }
}
