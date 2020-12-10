package dev.polek.adbwifi.report

import com.intellij.diagnostic.AbstractMessage
import com.intellij.ide.DataManager
import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.Consumer
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.SENTRY_DNS
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryOptions
import io.sentry.protocol.Message
import java.awt.Component

class ReportSubmitter : ErrorReportSubmitter() {

    init {
        if (SENTRY_ENABLED) {
            Sentry.init { options: SentryOptions ->
                options.dsn = SENTRY_DNS
            }
        }
    }

    override fun getReportActionText() = PluginBundle.message("submitReportActionText")

    override fun submit(
        events: Array<out IdeaLoggingEvent>,
        additionalInfo: String?,
        parentComponent: Component,
        consumer: Consumer<SubmittedReportInfo>
    ): Boolean {
        if (!SENTRY_ENABLED) return false

        val context = DataManager.getInstance().getDataContext(parentComponent)
        val project = CommonDataKeys.PROJECT.getData(context)
        object : Task.Backgroundable(project, PluginBundle.message("submitReportProgressText")) {
            override fun run(indicator: ProgressIndicator) {
                events.forEach { reportEvent(it, additionalInfo) }

                ApplicationManager.getApplication().invokeLater {
                    consumer.consume(SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE))
                }
            }
        }.queue()

        return true
    }

    private fun reportEvent(ideaEvent: IdeaLoggingEvent, additionalInfo: String?) {
        val data = ideaEvent.data as? AbstractMessage ?: return

        val event = SentryEvent()
        event.throwable = data.throwable
        event.release = pluginVersion()
        event.environment = environment()
        if (additionalInfo != null) {
            event.message = Message().apply { message = additionalInfo }
        }

        val appInfo = ApplicationInfo.getInstance()
        event.setTag("IDEA API Version", appInfo.apiVersion)
        event.setTag("IDEA Build", appInfo.build.asString())

        Sentry.captureEvent(event)
    }

    private fun pluginVersion(): String {
        return PluginManager.getPlugin(pluginDescriptor.pluginId)?.version.orEmpty()
    }

    private fun environment(): String {
        val os = SystemInfo.getOsNameAndVersion()
        val ideaName = ApplicationInfo.getInstance().fullApplicationName
        return "$os, $ideaName"
    }

    private companion object {
        private val SENTRY_ENABLED = SENTRY_DNS != null
    }
}
