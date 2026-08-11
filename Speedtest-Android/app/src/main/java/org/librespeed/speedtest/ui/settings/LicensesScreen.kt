package org.librespeed.speedtest.ui.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import org.librespeed.speedtest.R

private data class Library(val name: String, val license: String, val url: String)

private val LIBRARIES = listOf(
    Library("LibreSpeed speedtest engine", "LGPL-3.0", "https://github.com/librespeed/speedtest-android"),
    Library("Kotlin & kotlinx.coroutines", "Apache-2.0", "https://kotlinlang.org"),
    Library("Jetpack Compose & Material 3", "Apache-2.0", "https://developer.android.com/jetpack/compose"),
    Library("Material Icons", "Apache-2.0", "https://fonts.google.com/icons"),
    Library("AndroidX Navigation Compose", "Apache-2.0", "https://developer.android.com/jetpack/androidx/releases/navigation"),
    Library("AndroidX DataStore", "Apache-2.0", "https://developer.android.com/jetpack/androidx/releases/datastore"),
    Library("AndroidX Lifecycle", "Apache-2.0", "https://developer.android.com/jetpack/androidx/releases/lifecycle"),
    Library("AndroidX Core & Activity", "Apache-2.0", "https://developer.android.com/jetpack/androidx")
)

@Composable
fun LicensesScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.dialog_close))
            }
            Text(
                text = stringResource(R.string.settings_licenses),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        LazyColumn(Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(horizontal = 20.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        LIBRARIES.forEachIndexed { index, library ->
                            if (index > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, library.url.toUri()))
                                        } catch (_: Exception) {
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = library.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = library.license,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
