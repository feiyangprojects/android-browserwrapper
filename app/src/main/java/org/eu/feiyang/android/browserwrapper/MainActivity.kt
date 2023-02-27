package org.eu.feiyang.android.browserwrapper

import android.content.Context
import android.content.SharedPreferences.Editor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.eu.feiyang.android.browserwrapper.ui.theme.BrowserWrapperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences =
            getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE)

        setContent {
            BrowserWrapperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()
                ) {
                    Scaffold(topBar = {
                        CenterAlignedTopAppBar(title = {
                            Text(
                                text = stringResource(
                                    id = R.string.app_name
                                )
                            )
                        })
                    }) { contentPadding ->
                        Column(modifier = Modifier.padding(contentPadding)) {
                            val browser = remember { mutableStateOf(false) }

                            if (browser.value) {
                                val preferenceBrowser =
                                    stringResource(id = R.string.preference_browser)
                                val defaultBrowser =
                                    stringResource(id = R.string.preference_browser_default)
                                val currentBrowser =
                                    rememberSaveable(stateSaver = TextFieldValue.Saver) {
                                        mutableStateOf(
                                            TextFieldValue(
                                                preferences.getString(
                                                    preferenceBrowser, defaultBrowser
                                                )!!
                                            )
                                        )
                                    }

                                PreferenceAlertDialog(
                                    visible = browser,
                                    title = stringResource(id = R.string.set_browser),
                                    key = stringResource(id = R.string.preference_browser),
                                    value = currentBrowser,
                                    editor = preferences.edit()
                                )
                            }

                            PreferenceCard(
                                visible = browser,
                                title = stringResource(id = R.string.set_browser),
                                description = stringResource(id = R.string.set_browser_description),
                                icon = painterResource(
                                    id = R.drawable.baseline_browser_updated_24
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreferenceCard(
    visible: MutableState<Boolean>, title: String, description: String, icon: Painter
) {
    Card(onClick = { visible.value = true }, modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun PreferenceAlertDialog(
    visible: MutableState<Boolean>,
    title: String,
    key: String,
    value: MutableState<TextFieldValue>,
    editor: Editor
) {
    AlertDialog(onDismissRequest = {
        visible.value = false
    }, title = {
        Text(text = title)
    }, text = {
        OutlinedTextField(value = value.value, onValueChange = { value.value = it })
    }, confirmButton = {
        TextButton(onClick = {
            visible.value = false

            editor.putString(key, value.value.text)
            editor.apply()
        }) {
            Text(text = stringResource(id = R.string.save))
        }
    }, dismissButton = {
        TextButton(onClick = {
            visible.value = false
        }) {
            Text(text = stringResource(id = R.string.cancel))
        }
    })
}