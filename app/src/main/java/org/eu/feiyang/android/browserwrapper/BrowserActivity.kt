package org.eu.feiyang.android.browserwrapper

import android.content.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.eu.feiyang.android.browserwrapper.ui.theme.BrowserWrapperTheme
import org.eu.feiyang.android.browserwrapper.util.BROWSER_COMPONENT
import org.eu.feiyang.android.browserwrapper.util.findActivity

class BrowserActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE)

        super.onCreate(savedInstanceState)
        setContent {
            BrowserWrapperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    BrowserContent(preferences = preferences)
                }
            }
        }
    }
}

@Composable
fun BrowserContent(preferences: SharedPreferences) {
    val activity = LocalContext.current.findActivity()
    val clipboard =
        LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    var incognito by remember { mutableStateOf(true) }

    AlertDialog(onDismissRequest = {
        activity.finish()
    }, title = {
        Text(text = stringResource(id = R.string.open_it))
    }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // It's impossible to receive an intent with null data with current intent-filter configuration
            Text(text = activity.intent.data!!.toString())
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(value = incognito, onValueChange = { incognito = it })
            ) {
                Checkbox(checked = incognito, onCheckedChange = { incognito = it })
                Text(
                    text = stringResource(id = R.string.open_in_incognito),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }

    }, confirmButton = {
        TextButton(onClick = {
            //Use null as session id since a persistent session is unnecessary for this app
            val session: String? = null

            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                component = ComponentName.unflattenFromString(
                    preferences.getString("browser", null) ?: BROWSER_COMPONENT
                )
                // See L52
                data = activity.intent.data
                putExtra("android.support.customtabs.extra.SESSION", session)
            }

            if (incognito) {
                intent.putExtra(
                    "com.google.android.apps.chrome.EXTRA_OPEN_NEW_INCOGNITO_TAB", true
                )
            }
            activity.startActivity(intent)
            activity.finish()
        }) {
            Text(text = stringResource(id = R.string.open))
        }
    }, dismissButton = {
        TextButton(onClick = {
            // See L52
            val clip: ClipData =
                ClipData.newPlainText(activity.intent.type, activity.intent.data!!.toString())
            clipboard.setPrimaryClip(clip)
            activity.finish()
        }) {
            Text(text = stringResource(id = R.string.copy))
        }
    })

}
