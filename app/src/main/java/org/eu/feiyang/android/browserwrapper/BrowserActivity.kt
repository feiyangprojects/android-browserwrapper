package org.eu.feiyang.android.browserwrapper

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
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
import androidx.compose.ui.unit.dp
import org.eu.feiyang.android.browserwrapper.ui.theme.BrowserWrapperTheme

class BrowserActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = getSharedPreferences(Constants.Preferences.NAME, Context.MODE_PRIVATE)
        val clipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        setContent {
            BrowserWrapperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0f)
                ) {
                    val browser = remember {
                        mutableStateOf(
                            preferences.getString(Constants.Preferences.NAME_BROWSER, "")!!
                        )
                    }

                    BrowserDialog(
                        activity = this,
                        clipboardManager = clipboardManager,
                        browser = ComponentName.unflattenFromString(browser.value),
                        incognitoSupported = browser.value.endsWith(Constants.Intent.EXTRA_SUFFIX_SUPPORT_CHROME_CUSTOM_TAB_INCOGNITO)
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

    AlertDialog(onDismissRequest = { activity.finish() }, content = {
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
                    TextButton(onClick = {
                        if (browser != null) {
                            val intent = Utilities.Intent.newCustomTabIntent(
                                browser = browser,
                                uri = activity.intent.data!!,
                                incognito = incognito.value
                            )
                            activity.startActivity(intent)
                            activity.finish()
                        }
                    }, enabled = (browser != null)) {
                        Text(text = stringResource(id = R.string.open))
                    }
                }
            }
        }
    })
}