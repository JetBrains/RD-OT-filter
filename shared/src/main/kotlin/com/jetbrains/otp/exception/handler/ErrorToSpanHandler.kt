package com.jetbrains.otp.exception.handler

import com.intellij.ide.plugins.PluginUtil
import com.jetbrains.otp.span.DefaultRootSpanService
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import java.util.logging.Handler
import java.util.logging.LogRecord

object ErrorToSpanHandler : Handler() {
    override fun publish(record: LogRecord?) {
        val throwable = record?.thrown ?: return
        val plugin = PluginUtil.getInstance().findPluginId(throwable)?.idString
        val attributes: Attributes = plugin?.let { Attributes.of(AttributeKey.stringKey("plugin"), it) }
            ?: Attributes.empty()
        DefaultRootSpanService.currentSpan().recordException(throwable, attributes)
    }

    override fun flush() {}
    override fun close() {}
}