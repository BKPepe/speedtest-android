package org.librespeed.speedtest.ui.result

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.librespeed.speedtest.BuildConfig
import org.librespeed.speedtest.R
import org.librespeed.speedtest.data.GeoDistance
import org.librespeed.speedtest.data.HistoryDatabase
import org.librespeed.speedtest.data.HistoryEntry
import org.librespeed.speedtest.ui.history.formatDate
import java.util.Locale

@Composable
fun TestDetailsScreen(entryId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    var entry by remember { mutableStateOf<HistoryEntry?>(null) }
    var missing by remember { mutableStateOf(false) }

    LaunchedEffect(entryId) {
        val loaded = withContext(Dispatchers.IO) { HistoryDatabase(context.applicationContext).read(entryId) }
        if (loaded == null) missing = true else entry = loaded
    }
    LaunchedEffect(missing) { if (missing) onBack() }

    val result = entry ?: return

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.dialog_close))
            }
            Text(
                text = stringResource(R.string.test_details_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(horizontal = 20.dp)) {
            Section(stringResource(R.string.section_test)) {
                result.shareUrl?.let {
                    DetailRow(stringResource(R.string.detail_result_id), it.substringAfterLast("=", it), Icons.Filled.Tag)
                    RowDivider()
                }
                DetailRow(stringResource(R.string.section_date), formatDate(result.date), Icons.Filled.Event)
                if (result.durationMs > 0) {
                    RowDivider()
                    DetailRow(stringResource(R.string.detail_duration), String.format(Locale.US, "%.1f s", result.durationMs / 1000.0), Icons.Filled.Schedule)
                }
            }

            Section(stringResource(R.string.section_server)) {
                DetailRow(stringResource(R.string.servers_add_name), GeoDistance.cleanName(result.server), Icons.Filled.Public)
                GeoDistance.sponsor(result.server)?.let {
                    RowDivider()
                    DetailRow(stringResource(R.string.detail_isp), it, Icons.Filled.Business)
                }
                if (result.ping >= 0) {
                    RowDivider()
                    DetailRow(stringResource(R.string.info_latency), String.format(Locale.US, "%.1f %s", result.ping, stringResource(R.string.unit_ms)), Icons.Filled.NetworkPing)
                }
            }

            Section(stringResource(R.string.section_network)) {
                if (result.ipVersion != 0) {
                    DetailRow(stringResource(R.string.detail_protocol), "IPv${result.ipVersion}", Icons.Filled.Lan)
                    RowDivider()
                }
                result.networkType?.let {
                    DetailRow(stringResource(R.string.detail_network), it, Icons.Filled.Wifi)
                    RowDivider()
                }
                result.ipInfo?.takeIf { it.isNotBlank() }?.let { info ->
                    val ip = info.substringBefore(" - ").trim()
                    val provider = info.substringAfter(" - ", "").trim()
                    DetailRow(stringResource(R.string.detail_ip), ip, Icons.Filled.MyLocation)
                    if (provider.isNotEmpty()) {
                        RowDivider()
                        DetailRow(stringResource(R.string.detail_isp), provider, Icons.Filled.Business)
                    }
                }
            }

            Section(stringResource(R.string.section_data)) {
                val downloadMb = result.downloadSamples.sum() * 0.1 / 8
                val uploadMb = result.uploadSamples.sum() * 0.1 / 8
                if (downloadMb > 0) {
                    DetailRow(stringResource(R.string.detail_dl_data), String.format(Locale.US, "~ %.0f MB", downloadMb), Icons.Filled.ArrowDownward)
                    RowDivider()
                }
                if (uploadMb > 0) {
                    DetailRow(stringResource(R.string.detail_ul_data), String.format(Locale.US, "~ %.0f MB", uploadMb), Icons.Filled.ArrowUpward)
                    RowDivider()
                }
                DetailRow(stringResource(R.string.detail_data), String.format(Locale.US, "~ %.0f MB", downloadMb + uploadMb), Icons.Filled.DataUsage)
            }

            Section(stringResource(R.string.section_telemetry)) {
                DetailRow(
                    stringResource(R.string.settings_section_telemetry),
                    stringResource(if (result.shareUrl != null) R.string.telemetry_submitted else R.string.telemetry_not_submitted),
                    Icons.Filled.CloudUpload
                )
            }

            Section(stringResource(R.string.section_client)) {
                DetailRow(stringResource(R.string.section_application), "LibreSpeed", Icons.Filled.Apps)
                RowDivider()
                DetailRow(stringResource(R.string.section_version), BuildConfig.VERSION_NAME, Icons.Filled.Info)
                RowDivider()
                DetailRow("Android", Build.VERSION.RELEASE, Icons.Filled.Android)
                RowDivider()
                DetailRow(stringResource(R.string.section_device), "${Build.MANUFACTURER} ${Build.MODEL}", Icons.Filled.Smartphone)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 6.dp)
    )
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) { content() }
    }
}
