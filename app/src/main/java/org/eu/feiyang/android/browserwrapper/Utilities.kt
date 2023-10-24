package org.eu.feiyang.android.browserwrapper

import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri

object Utilities {
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