package org.eu.feiyang.android.browserwrapper.internal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(private val preferencesRepository: PreferencesRepository) :
    ViewModel() {
    val browserPackage: StateFlow<String> = preferencesRepository.browserPackage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PreferencesRepository.BROWSER_DEFAULT
    )
    val browserActivity: StateFlow<String> = preferencesRepository.browserActivity.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PreferencesRepository.BROWSER_DEFAULT
    )
    val noCustomTabsIfNotWithIncognito: StateFlow<Boolean> =
        preferencesRepository.noCustomTabsIfNotWithIncognito.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun setBrowser(pkg: String, act: String) {
        viewModelScope.launch {
            preferencesRepository.setBrowser(pkg, act)
        }
    }

    fun setNoCustomTabsIfNotWithIncognito(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNoCustomTabsIfNotWithIncognito(value)
        }
    }
}