package org.eu.feiyang.android.browserwrapper

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import dagger.hilt.android.AndroidEntryPoint
import org.eu.feiyang.android.browserwrapper.internal.PreferencesViewModel
import org.eu.feiyang.android.browserwrapper.internal.Utilities
import org.eu.feiyang.android.browserwrapper.ui.theme.BrowserWrapperTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: PreferencesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val browserPackage = viewModel.browserPackage.collectAsStateWithLifecycle()
            val browserActivity = viewModel.browserActivity.collectAsStateWithLifecycle()
            val editingBrowser = remember { mutableStateOf(false) }

            //Check if browser is removed after initial setup
            val browserInfo = try {
                packageManager.getApplicationInfo(browserPackage.value, 0)
            } catch (_: PackageManager.NameNotFoundException) {
                packageManager.getApplicationInfo(packageName, 0)
            }

            BrowserWrapperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(topBar = {
                        CenterAlignedTopAppBar(title = {
                            Text(
                                text = stringResource(id = R.string.app_name)
                            )
                        })
                    }) { paddingValues ->
                        Column(modifier = Modifier.padding(paddingValues)) {
                            PackagePreferenceCard(
                                editing = editingBrowser,
                                broken = browserInfo.packageName == packageName,
                                icon = packageManager.getApplicationIcon(browserInfo),
                                packageName = browserPackage
                            )

                            if (editingBrowser.value) {
                                val browsers =
                                    Utilities.PackageManager.queryBrowsersIntentActivities(
                                        packageManager = packageManager, packageName = packageName
                                    )

                                PackageListDialog(
                                    editing = editingBrowser,
                                    onPackageSelected = { pkg, act ->
                                        viewModel.setBrowser(
                                            pkg,
                                            act
                                        )
                                    },
                                    packageName = browserPackage,
                                    packageList = browsers
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PackagePreferenceCard(
    editing: MutableState<Boolean>,
    broken: Boolean,
    icon: Drawable,
    packageName: State<String>
) {
    Card(
        onClick = { editing.value = !editing.value },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

            Image(
                painter = rememberDrawablePainter(drawable = icon),
                contentDescription = null,
                modifier = Modifier.width(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(id = R.string.browser),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = if (broken) stringResource(
                        id = R.string.browser_not_set
                    ) else stringResource(
                        id = R.string.browser_set_with_name,
                        packageName.value
                    ), style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun PackageListDialog(
    editing: MutableState<Boolean>,
    onPackageSelected: (String, String) -> Unit,
    packageName: State<String>,
    packageList: List<ResolveInfo>
) {
    if (editing.value) {
        val (selectedOption, onOptionSelected) = remember {
            mutableStateOf(
                packageList[maxOf(
                    packageList.indexOfFirst { it.activityInfo.packageName == packageName.value },
                    0
                )]
            )
        }
        AlertDialog(
            onDismissRequest = { editing.value = !editing.value },
            confirmButton = {
                TextButton(onClick = {
                    editing.value = false

                    onPackageSelected(
                        selectedOption.activityInfo.packageName,
                        selectedOption.activityInfo.name
                    )
                }) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            }, dismissButton = {
                TextButton(onClick = {
                    editing.value = !editing.value
                }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
            title = { Text(text = stringResource(id = R.string.choose_browser)) },
            text = {
                Column {
                    packageList.forEach { resolveInfo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .selectable(
                                    selected = (resolveInfo == selectedOption),
                                    onClick = { onOptionSelected(resolveInfo) }
                                )) {
                            RadioButton(
                                selected = (resolveInfo == selectedOption),
                                onClick = null
                            )
                            Text(
                                text = resolveInfo.activityInfo.packageName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }
        )
    }
}