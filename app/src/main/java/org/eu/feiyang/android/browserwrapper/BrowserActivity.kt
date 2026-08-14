package org.eu.feiyang.android.browserwrapper

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import org.eu.feiyang.android.browserwrapper.internal.PreferencesViewModel
import org.eu.feiyang.android.browserwrapper.internal.Utilities.Chrome
import org.eu.feiyang.android.browserwrapper.ui.theme.BrowserWrapperTheme

@AndroidEntryPoint
class BrowserActivity : ComponentActivity() {
    private val viewModel: PreferencesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ignore explicit launch without uri data
        if (intent.data == null) {
            finish()
        }

        val clipboardManager =
            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        setContent {
            val browserPackage = viewModel.browserPackage.collectAsStateWithLifecycle()
            val browserActivity = viewModel.browserActivity.collectAsStateWithLifecycle()

            BrowserWrapperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0f)
                ) {
                    BrowserDialog(
                        activity = this,
                        clipboardManager = clipboardManager,
                        browser = ComponentName(browserPackage.value, browserActivity.value),
                        incognitoSupported = Chrome.supportCCTIncognito(browserActivity.value)
                    )
                }
            }
        }
    }
}

@Composable
fun BrowserDialog(
    activity: Activity,
    clipboardManager: ClipboardManager,
    browser: ComponentName?,
    incognitoSupported: Boolean
) {
    val incognito = remember { mutableStateOf(incognitoSupported) }

    BasicAlertDialog(
        onDismissRequest = { activity.finish() },
        modifier = Modifier,
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = AlertDialogDefaults.shape,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.open_this_uri),
                        style = MaterialTheme.typography.titleLarge,
                        color = AlertDialogDefaults.titleContentColor
                    )
                    Text(
                        text = activity.intent.dataString!!,
                        modifier = Modifier.weight(1f, false),
                        overflow = TextOverflow.Ellipsis,
                        color = AlertDialogDefaults.textContentColor
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = incognito.value,
                                enabled = incognitoSupported,
                                onValueChange = { incognito.value = it }),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.open_with_incognito),
                            color = AlertDialogDefaults.textContentColor
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Checkbox(
                            checked = incognito.value,
                            onCheckedChange = { incognito.value = it },
                            enabled = incognitoSupported
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { activity.finish() }) {
                            Text(text = stringResource(id = R.string.close))
                        }
                        TextButton(onClick = {
                            val clip = ClipData.newPlainText(
                                activity.intent.type,
                                activity.intent.dataString!!
                            )
                            clipboardManager.setPrimaryClip(clip)
                            activity.finish()
                        }) {
                            Text(text = stringResource(id = R.string.copy))
                        }
                        if (browser != null) {
                            TextButton(onClick = {
                                val intent = Chrome.newCustomTabIntent(
                                    browser = browser,
                                    uri = activity.intent.data!!,
                                    incognito = incognito.value
                                )
                                activity.startActivity(intent)
                                activity.finish()
                            }) {
                                Text(text = stringResource(id = R.string.open))
                            }
                        }
                    }
                }
            }
        })
}