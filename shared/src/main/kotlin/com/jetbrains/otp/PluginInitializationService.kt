@file:Suppress("UnstableApiUsage")

package com.jetbrains.otp

import com.intellij.diagnostic.VMOptions
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.Pair

class PluginInitializationService : ProjectActivity {
    private val optionsToSet = mapOf(
        "rdct.diagnostic.otlp" to "true",
        "idea.diagnostic.opentelemetry.otlp" to "true"
    )

    override suspend fun execute(project: Project) {
        try {
            if (VMOptions.getUserOptionsFile() == null) {
                val vmOptionsFile = PathManager.findBinFile(VMOptions.getFileName())
                if (vmOptionsFile == null) {
                    LOG.warn("Can't find VM options file")
                    return
                }
                val options = optionsToSet.map { (key, value) -> Pair.create("-D$key=", value) }
                VMOptions.setOptions(options, vmOptionsFile)
            } else {
                optionsToSet.forEach { (key, value) ->
                    VMOptions.setProperty(key, value)
                }
            }

        } catch (e: Exception) {
            LOG.warn("Failed to set VM options", e)
        }
    }
}


private val LOG = Logger.getInstance(PluginInitializationService::class.java)