package com.lighthousepark.intervalsgym.core

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private const val DIAGNOSTIC_LOG_DIR_NAME = "diagnostics"
private const val DIAGNOSTIC_LOG_FILE_NAME = "intervals-gym.log"
private const val DIAGNOSTIC_LOG_MAX_BYTES = 1_000_000L

internal object DiagnosticsLogger {
    private val lock = Any()
    @Volatile private var uncaughtExceptionLoggerInstalled = false

    fun installUncaughtExceptionLogger(context: Context) {
        if (uncaughtExceptionLoggerInstalled) return
        val appContext = context.applicationContext
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            log(
                context = appContext,
                tag = "UncaughtException",
                message = "thread=${thread.name}",
                throwable = throwable
            )
            previousHandler?.uncaughtException(thread, throwable)
        }
        uncaughtExceptionLoggerInstalled = true
    }

    fun log(
        context: Context,
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        runCatching {
            synchronized(lock) {
                appendDiagnosticLogEntry(
                    logFile = diagnosticLogFile(context),
                    tag = tag,
                    message = message,
                    throwable = throwable
                )
            }
        }
    }

    fun diagnosticLogFile(context: Context): File {
        val externalDir = context.getExternalFilesDir(DIAGNOSTIC_LOG_DIR_NAME)
        val dir = externalDir ?: File(context.filesDir, DIAGNOSTIC_LOG_DIR_NAME)
        return File(dir, DIAGNOSTIC_LOG_FILE_NAME)
    }
}

internal fun appendDiagnosticLogEntry(
    logFile: File,
    tag: String,
    message: String,
    throwable: Throwable? = null,
    timestamp: String = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()),
    maxBytes: Long = DIAGNOSTIC_LOG_MAX_BYTES,
) {
    logFile.parentFile?.mkdirs()
    if (logFile.exists() && logFile.length() > maxBytes) {
        val rotatedLogFile = File(logFile.parentFile, "${logFile.name}.1")
        if (rotatedLogFile.exists()) rotatedLogFile.delete()
        logFile.renameTo(rotatedLogFile)
    }

    val entry = buildString {
        append('[')
        append(timestamp)
        append("] [")
        append(tag.ifBlank { "Diagnostics" })
        appendLine(']')
        message.lineSequence().forEach { line ->
            append("  ")
            appendLine(line)
        }
        throwable?.let { error ->
            appendLine("  throwable=${error::class.java.name}: ${error.message.orEmpty()}")
            append(error.stackTraceText().prependIndent("  "))
            if (!endsWith("\n")) appendLine()
        }
        appendLine()
    }
    logFile.appendText(entry)
}

private fun Throwable.stackTraceText(): String {
    val writer = StringWriter()
    printStackTrace(PrintWriter(writer))
    return writer.toString()
}
