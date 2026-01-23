package com.jetbrains.otp.freeze

import com.intellij.diagnostic.FreezeNotifier
import com.intellij.diagnostic.LogMessage
import com.intellij.diagnostic.ThreadDump
import com.intellij.openapi.diagnostic.IdeaLogRecordFormatter
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.StatusCode
import java.io.PrintWriter
import java.io.StringWriter
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
            .setStartTimestamp(startTime)
            .setAllAttributes(
                Attributes.of(
                    AttributeKey.stringKey("stackTrace"), getAbbreviatedStackTrace(event.throwable),
                    AttributeKey.longKey("duration.ms"), durationMs
                )
            ).startSpan()

        span.setStatus(StatusCode.ERROR)

        span.end(endTime)
    }

    fun getAbbreviatedStackTrace(throwable: Throwable): String {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        return sw.toString().lines().joinToString("\n") { line ->
            if (line.trimStart().startsWith("at ")) {
                val abbreviated = IdeaLogRecordFormatter.smartAbbreviate(
                    line.substringAfter("at ").substringBefore("(")
                )
                if (abbreviated != null) {
                    line.replace(
                        line.substringAfter("at ").substringBefore("("),
                        abbreviated
                    )
                } else line
            } else {
                line
            }
        }
    }
}