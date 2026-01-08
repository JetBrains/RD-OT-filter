package com.jetbrains.otp.settings.api

import com.intellij.platform.rpc.RemoteApiProviderService
import fleet.rpc.RemoteApi
import fleet.rpc.Rpc
import fleet.rpc.remoteApiDescriptor
import org.jetbrains.annotations.ApiStatus

@Rpc
@Suppress("UnstableApiUsage")
@ApiStatus.Internal
interface OtpDiagnosticSettingsApi : RemoteApi<Unit> {
    suspend fun syncDisabledGroups(disabledGroups: Set<String>)

    companion object {
        @JvmStatic
        suspend fun getInstance(): OtpDiagnosticSettingsApi {
            return RemoteApiProviderService.resolve(remoteApiDescriptor<OtpDiagnosticSettingsApi>())
        }
    }
}
