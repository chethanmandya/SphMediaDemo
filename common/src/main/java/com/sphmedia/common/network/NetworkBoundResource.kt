package com.sphmedia.common.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

abstract class NetworkBoundResource<LocalType, RemoteType> {


    suspend fun asFlow(): Flow<Resource<LocalType>> = flow {
        emit(Resource.Loading())

        // Load data from database first
        val dbSource = loadFromDb().firstOrNull()
        if (shouldFetch(dbSource)) {
            // Fetch new data from network if necessary
            emit(Resource.Loading(dbSource))
            try {
                val apiResponse = fetchFromNetwork()
                saveNetworkResult(apiResponse)
                emitAll(loadFromDb().map { Resource.Success(it) })
            } catch (throwable: Throwable) {
                // Handle network failure
                emit(Resource.Error(throwable.message ?: "Network Error", dbSource))
            }
        } else {
            // Load data from the database if no need to fetch from the network
            emitAll(loadFromDb().map { Resource.Success(it) })
        }
    }

    protected abstract fun loadFromDb(): Flow<LocalType>
    protected abstract suspend fun fetchFromNetwork(): RemoteType
    protected abstract suspend fun saveNetworkResult(item: RemoteType)
    protected open fun shouldFetch(data: LocalType?): Boolean = true
}