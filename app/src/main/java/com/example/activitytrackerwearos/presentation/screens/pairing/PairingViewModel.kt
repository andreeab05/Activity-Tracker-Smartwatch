package com.example.activitytrackerwearos.presentation.screens.pairing

import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import com.example.activitytrackerwearos.data.DatastoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PairingViewModel(private val datastoreRepository: DatastoreRepository
) : ViewModel() {
    private val pairingKey = booleanPreferencesKey("paired")
    var pairedState = mutableStateOf(false)
        private set
    fun updatePairingStatus(){
        CoroutineScope(Dispatchers.IO).launch {
            datastoreRepository.readData(key = pairingKey).collect{paired ->
                if (paired != null) {
                    pairedState.value = paired
                }
            }
        }
    }
}