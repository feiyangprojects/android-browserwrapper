package org.eu.feiyang.android.browserwrapper

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.eu.feiyang.android.browserwrapper.ui.theme.BrowserWrapperTheme
import org.eu.feiyang.android.browserwrapper.util.BROWSER_COMPONENT

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE)

        super.onCreate(savedInstanceState)
        setContent {
            BrowserWrapperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(preferences = preferences)
                }
            }
        }
    }
}

@Composable
fun MainContent(preferences: SharedPreferences) {
    var browser by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                preferences.getString("browser", null) ?: BROWSER_COMPONENT
            )
        )
    }
    var setBrowser by remember { mutableStateOf(false) }

    if (setBrowser) {
        AlertDialog(
            onDismissRequest = {
                setBrowser = false
            },
            title = {
                Text(text = stringResource(id = R.string.set_browser))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = stringResource(id = R.string.set_browser_description))
                    OutlinedTextField(
                        value = browser,
                        onValueChange = { browser = it }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        setBrowser = false

                        val preferencesEditor = preferences.edit()
                        preferencesEditor.putString("browser", browser.text)
                        preferencesEditor.apply()
                    }
                ) {
                    Text(text = stringResource(id = R.string.save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        setBrowser = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {
        FilledTonalButton(
            onClick = { setBrowser = true }
        ) {
            Text(text = stringResource(id = R.string.set_browser))
        }
    }
}