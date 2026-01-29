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
                    AttributeKey.stringKey("stackTrace"), getAbbreviatedStackTrace(event.throwable)
                )
            ).startSpan()

        span.setStatus(StatusCode.ERROR)

        span.end(endTime)
    }

    fun getAbbreviatedStackTrace(throwable: Throwable): String {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        return abbreviateStackTraces(sw.toString())
    }

    internal fun abbreviateStackTraces(stackTraceText: String): String {
        return stackTraceText.lines().joinToString("\n") { line ->
            if (line.trimStart().startsWith("at ")) {
                val fullName = line.substringAfter("at ").substringBefore("(")
                val abbreviated = abbreviateFullyQualifiedName(fullName)
                if (abbreviated != null) {
                    line.replace(fullName, abbreviated)
                } else line
            } else {
                line
            }
        }
    }
    
    internal fun abbreviateFullyQualifiedName(fullName: String): String? {
        val className = if (fullName.contains('/')) {
            fullName.substringAfter('/')
        } else {
            fullName
        }

        val abbreviatablePrefixes = listOf(
            "java.", "javax.", "jdk.", "sun.",
            "com.intellij.", "com.jetbrains.", "org.jetbrains."
        )

        if (abbreviatablePrefixes.none { className.startsWith(it) }) {
            return null
        }
            
        val parts = className.split('.')
        var classNameIndex = parts.size - 1
        for (i in parts.indices.reversed()) {
            if (parts[i].isNotEmpty() && parts[i][0].isUpperCase()) {
                classNameIndex = i
                break
            }
        }

        val result = StringBuilder()
        for (i in 0 until classNameIndex) {
            result.append(parts[i][0]).append('.')
        }

        for (i in classNameIndex until parts.size) {
            if (i > classNameIndex) {
                result.append('.')
            }
            result.append(parts[i])
        }

        return result.toString()
    }
}