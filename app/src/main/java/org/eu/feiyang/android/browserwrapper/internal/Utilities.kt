package org.eu.feiyang.android.browserwrapper.internal

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri

object Utilities {
    object Intent {
        fun newCustomTabIntent(
            browser: ComponentName, uri: Uri, incognito: Boolean
        ): android.content.Intent {
            //Use null as session id since a persistent session is unnecessary for this app
            val session: String? = null
            return Intent().apply {
                action = android.content.Intent.ACTION_VIEW
                data = uri
                component = browser
                putExtra(Constants.Intent.EXTEA_CHROME_CUSTOM_TAB_SESSION, session)
                if (incognito) {
                    putExtra(Constants.Intent.EXTRA_CHROME_CUSTOM_TAB_INCOGNITO, true)
                }
            }
        }
    }

    object PackageManager {
        fun queryBrowsersIntentActivities(
            packageManager: android.content.pm.PackageManager,
            packageName: String
        ): List<ResolveInfo> {
            val intent = Intent().apply {
                action = android.content.Intent.ACTION_VIEW
                data = Uri.parse("https://example.org/")
            }
            return packageManager.queryIntentActivities(
                intent,
                android.content.pm.PackageManager.MATCH_ALL
            )
                .filter { i -> i.activityInfo.packageName != packageName }
        }
    }

}