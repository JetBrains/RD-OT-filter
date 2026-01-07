package com.jetbrains.otp.freeze

import com.intellij.diagnostic.FreezeNotifier
import com.intellij.diagnostic.LogMessage
import com.intellij.diagnostic.ThreadDump
import com.jetbrains.otp.span.DefaultRootSpanService
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.context.Context
import java.nio.file.Path
import java.time.Instant

@Suppress("UnstableApiUsage")
class OtpFreezeNotifier : FreezeNotifier {
    private val tracer = GlobalOpenTelemetry.get().getTracer("com.jetbrains.otp.diagnostic")

    override fun notifyFreeze(
        event: LogMessage,
        currentDumps: Collection<ThreadDump>,
        reportDir: Path,
        durationMs: Long
    ) {
        val endTime = Instant.now()
        val startTime = endTime.minusMillis(durationMs)

        val span = tracer.spanBuilder("ui-thread-freeze")
            .setParent(Context.current().with(DefaultRootSpanService.currentSpan()))
            .setStartTimestamp(startTime)
            .setAllAttributes(
                Attributes.of(
                    AttributeKey.stringKey("message"), event.throwable.message,
                    AttributeKey.longKey("duration.ms"), durationMs
                )
            ).startSpan()

        span.end(endTime)
    }
}