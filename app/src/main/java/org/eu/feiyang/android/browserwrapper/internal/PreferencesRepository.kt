package org.eu.feiyang.android.browserwrapper.internal

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferencesRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {
    companion object {
        val BROWSER_PACKAGE = stringPreferencesKey("browser_package")
        val BROWSER_ACTIVITY = stringPreferencesKey("browser_activity")
        const val BROWSER_DEFAULT = ""
        val NO_CUSTOMTABS_IF_NOT_WITH_INCOGNITO =
            booleanPreferencesKey("no_customtabs_if_not_incognito")
    }

    val browserPackage: Flow<String> = dataStore.data.map { preferences ->
        preferences[BROWSER_PACKAGE] ?: BROWSER_DEFAULT
    }
    val browserActivity: Flow<String> = dataStore.data.map { preferences ->
        preferences[BROWSER_ACTIVITY] ?: BROWSER_DEFAULT
    }
    val noCustomTabsIfNotWithIncognito = dataStore.data.map { preferences ->
        preferences[NO_CUSTOMTABS_IF_NOT_WITH_INCOGNITO] ?: false
    }

    suspend fun setBrowser(pkg: String, act: String) {
        dataStore.edit { preferences ->
            preferences[BROWSER_PACKAGE] = pkg
            preferences[BROWSER_ACTIVITY] = act
        }
    }

    suspend fun setNoCustomTabsIfNotWithIncognito(value: Boolean) {
        dataStore.edit { preferences -> preferences[NO_CUSTOMTABS_IF_NOT_WITH_INCOGNITO] = value }
    }
}