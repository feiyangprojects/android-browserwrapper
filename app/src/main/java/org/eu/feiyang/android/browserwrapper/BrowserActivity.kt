package org.eu.feiyang.android.browserwrapper

import android.app.Activity
import android.content.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.eu.feiyang.android.browserwrapper.ui.theme.BrowserWrapperTheme
import org.eu.feiyang.android.browserwrapper.util.findActivity

class BrowserActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences =
            getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE)

        setContent {
            BrowserWrapperTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val activity = LocalContext.current.findActivity()
                    val clipboard =
                        LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                    if (activity !== null) {
                        val preferenceBrowser = stringResource(id = R.string.preference_browser)
                        val defaultBrowser =
                            stringResource(id = R.string.preference_browser_default)
                        val currentBrowser =
                            preferences.getString(preferenceBrowser, defaultBrowser)!!

                        val customtabs = stringResource(id = R.string.intent_customtabs)
                        val customtabsIncognito = remember { mutableStateOf(true) }
                        val customtabsIncognitoExtra =
                            stringResource(id = R.string.intent_customtabs_incognito)
                        var customtabsIncognitoSupported = true
                        if (!currentBrowser.endsWith(stringResource(id = R.string.intent_customtabs_incognito_suffix_chromium))) {
                            customtabsIncognito.value = false
                            customtabsIncognitoSupported = false
                        }

                        BrowserAlertDialog(
                            activity = activity,
                            clipboard = clipboard,
                            preferences = preferences,
                            customtabs = customtabs,
                            customtabsIncognito = customtabsIncognito,
                            customtabsIncognitoExtra = customtabsIncognitoExtra,
                            customtabsIncognitoSupported = customtabsIncognitoSupported,
                            defaultBrowser = defaultBrowser,
                            preferenceBrowser = preferenceBrowser
                        )
                    } else {
                        BrowserNullActivityAlertDialog()
                    }
                }
            }
        }
    }
}

@Composable
fun BrowserAlertDialog(
    activity: Activity,
    clipboard: ClipboardManager,
    preferences: SharedPreferences,
    customtabs: String,
    customtabsIncognito: MutableState<Boolean>,
    customtabsIncognitoExtra: String,
    customtabsIncognitoSupported: Boolean,
    defaultBrowser: String,
    preferenceBrowser: String
) {
    AlertDialog(onDismissRequest = {
        activity.finish()
    }, title = {
        Text(text = stringResource(id = R.string.open_it))
    }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = stringResource(id = R.string.close_dialog_tip))
            // It's impossible to receive an intent with null data with current intent-filter configuration
            Text(text = activity.intent.data!!.toString())
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(value = customtabsIncognito.value,
                        enabled = customtabsIncognitoSupported,
                        onValueChange = { customtabsIncognito.value = it })
            ) {
                Text(
                    text = stringResource(id = R.string.open_in_incognito),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.weight(1f))
                Checkbox(checked = customtabsIncognito.value,
                    enabled = customtabsIncognitoSupported,
                    onCheckedChange = { customtabsIncognito.value = it })
            }
        }

    }, confirmButton = {
        TextButton(onClick = {
            //Use null as session id since a persistent session is unnecessary for this app
            val session: String? = null

            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                component = ComponentName.unflattenFromString(
                    preferences.getString(preferenceBrowser, defaultBrowser)!!
                )
                // See L96
                data = activity.intent.data
                putExtra(customtabs, session)
            }

            if (customtabsIncognito.value) {
                intent.putExtra(customtabsIncognitoExtra, true)
            }
            activity.startActivity(intent)
            activity.finish()
        }) {
            Text(text = stringResource(id = R.string.open))
        }
    }, dismissButton = {
        TextButton(onClick = {
            // See L96
            val clip: ClipData =
                ClipData.newPlainText(activity.intent.type, activity.intent.data!!.toString())
            clipboard.setPrimaryClip(clip)
            activity.finish()
        }) {
            Text(text = stringResource(id = R.string.copy))
        }
    })
}

@Composable
fun BrowserNullActivityAlertDialog() {
    AlertDialog(onDismissRequest = { },
        confirmButton = { },
        title = { Text(text = stringResource(id = R.string.error)) },
        text = { Text(text = stringResource(id = R.string.error_null_activity)) })
}