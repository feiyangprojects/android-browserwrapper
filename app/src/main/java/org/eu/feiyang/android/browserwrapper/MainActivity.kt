package org.eu.feiyang.android.browserwrapper

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import org.eu.feiyang.android.browserwrapper.ui.theme.BrowserWrapperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = getSharedPreferences(Constants.Preferences.NAME, Context.MODE_PRIVATE)

        setContent {
            BrowserWrapperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(topBar = {
                        CenterAlignedTopAppBar(title = {
                            Text(
                                text = stringResource(R.string.app_name)
                            )
                        })
                    }) { paddingValues ->
                        Column(modifier = Modifier.padding(paddingValues)) {
                            val browser = remember {
                                mutableStateOf(
                                    preferences.getString(
                                        Constants.Preferences.NAME_BROWSER,
                                        packageName
                                    )!!
                                )
                            }
                            val editBrowser = remember { mutableStateOf(false) }

                            //Check if browser is removed after initial setup
                            val browserInfo = try {
                                packageManager.getApplicationInfo(browser.value.split("/")[0], 0)
                            } catch (_: PackageManager.NameNotFoundException) {
                                packageManager.getApplicationInfo(packageName, 0)
                            }

                            PackagePreferenceCard(
                                edit = editBrowser,
                                title = stringResource(R.string.browser),
                                description = if (browserInfo.packageName != packageName) stringResource(
                                    id = R.string.browser_set_with_name,
                                    browserInfo.packageName
                                ) else stringResource(
                                    id = R.string.browser_not_set
                                ),
                                icon = packageManager.getApplicationIcon(browserInfo)
                            )

                            if (editBrowser.value) {
                                val browsers =
                                    Utilities.PackageManager.queryBrowsersIntentActivities(
                                        packageManager = packageManager, packageName = packageName
                                    )

                                PackageListDialog(
                                    show = editBrowser,
                                    preference = browser,
                                    preferenceEditor = preferences.edit(),
                                    title = stringResource(
                                        id = R.string.choose_browser
                                    ),
                                    list = browsers
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
    edit: MutableState<Boolean>,
    title: String,
    description: String,
    icon: Drawable
) {
    Card(
        onClick = { edit.value = !edit.value },
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
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun PackageListDialog(
    show: MutableState<Boolean>,
    preference: MutableState<String>,
    preferenceEditor: SharedPreferences.Editor,
    title: String,
    list: List<ResolveInfo>
) {
    if (show.value) {
        val (selectedOption, onOptionSelected) = remember { mutableStateOf(list[0]) }
        AlertDialog(
            onDismissRequest = { show.value = !show.value },
            confirmButton = {
                TextButton(onClick = {
                    show.value = false

                    preference.value =
                        selectedOption.activityInfo.packageName + "/" + selectedOption.activityInfo.name
                    preferenceEditor.putString(Constants.Preferences.NAME_BROWSER, preference.value)
                    preferenceEditor.apply()
                }) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            }, dismissButton = {
                TextButton(onClick = {
                    show.value = !show.value
                }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
            title = { Text(text = title) },
            text = {
                Column {
                    list.forEach { resolveInfo ->
                        Row(modifier = Modifier
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