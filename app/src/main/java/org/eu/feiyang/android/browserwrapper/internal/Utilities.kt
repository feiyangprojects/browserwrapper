package org.eu.feiyang.android.browserwrapper.internal

import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.core.net.toUri

object Utilities {
    object Chrome {
        fun supportCCTIncognito(activity: String): Boolean {
            return activity == "com.google.android.apps.chrome.IntentDispatcher"
        }
    }

    object PackageManager {
        fun queryBrowsersIntentActivities(
            packageManager: android.content.pm.PackageManager,
            packageName: String
        ): List<ResolveInfo> {
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = "https://example.org/".toUri()
            }
            return packageManager.queryIntentActivities(
                intent,
                android.content.pm.PackageManager.MATCH_ALL
            )
                .filter { i -> i.activityInfo.packageName != packageName }
        }
    }

}