package org.eu.feiyang.android.browserwrapper

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                    MainContent(preferences = preferences)
                }
            }
        }
    }
}

@Composable
fun MainContent(preferences: SharedPreferences) {
    val browser = remember { mutableStateOf(false) }
    if (browser.value) {
        val preferenceBrowser = stringResource(id = R.string.preference_browser)
        val defaultBrowser = stringResource(id = R.string.preference_browser_default)
        val currentBrowser = rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(
                TextFieldValue(
                    preferences.getString(preferenceBrowser, defaultBrowser)!!
                )
            )
        }

        PreferenceAlertDialog(
            visible = browser,
            title = stringResource(id = R.string.set_browser),
            description = stringResource(id = R.string.set_browser_description),
            key = stringResource(id = R.string.preference_browser),
            value = currentBrowser,
            editor = preferences.edit()
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        FilledTonalButton(onClick = { browser.value = true }) {
            Text(text = stringResource(id = R.string.set_browser))
        }
    }
}

@Composable
fun PreferenceAlertDialog(
    visible: MutableState<Boolean>,
    title: String,
    description: String,
    key: String,
    value: MutableState<TextFieldValue>,
    editor: Editor
) {
    AlertDialog(onDismissRequest = {
        visible.value = false
    }, title = {
        Text(text = title)
    }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = description)
            OutlinedTextField(value = value.value, onValueChange = { value.value = it })
        }
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