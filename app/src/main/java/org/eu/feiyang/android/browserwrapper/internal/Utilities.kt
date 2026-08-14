package org.eu.feiyang.android.browserwrapper.internal

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri

object Utilities {
    object Chrome {
        fun newCustomTabIntent(
            browser: ComponentName, uri: Uri, incognito: Boolean
        ): android.content.Intent {
            //Use null as session id since a persistent session is unnecessary for this app
            val session: String? = null
            return Intent().apply {
                action = android.content.Intent.ACTION_VIEW
                data = uri
                component = browser
                putExtra("android.support.customtabs.extra.SESSION", session)
                if (incognito) {
                    putExtra("com.google.android.apps.chrome.EXTRA_OPEN_NEW_INCOGNITO_TAB", true)
                }
            }
        }

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