package com.example.activitytrackerwearos.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DatastoreRepository(private val dataStore: DataStore<Preferences>){
    // Write to Data Store
    suspend fun saveData(key: Preferences.Key<Boolean>, value: Boolean) {
        dataStore.edit { settings ->
            settings[key] = value
        }
    }

    // Read from Data Store
    fun readData(key: Preferences.Key<Boolean>): Flow<Boolean?> {
        return dataStore.data.map { preferences ->
            preferences[key]
        }
    }


}