package com.example.mangareader.ui.screens.manga

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangareader.data.api.MangaDto
import com.example.mangareader.di.NetworkModule
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MangaListUiState(
    val mangas: List<MangaDto> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val hasMoreItems: Boolean = true
)

class MangaListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MangaListUiState())
    val uiState: StateFlow<MangaListUiState> = _uiState.asStateFlow()
    
    private var currentPage = 0
    private var currentQuery = ""
    private var searchJob: Job? = null
    private val pageSize = 20

    init {
        loadPopularManga(refresh = true)
    }

    private fun loadPopularManga(refresh: Boolean = false) {
        if (refresh) {
            currentPage = 0
            _uiState.update { it.copy(mangas = emptyList()) }
        }
        
        viewModelScope.launch {
            try {
                _uiState.update {
                    when {
                        refresh -> it.copy(isRefreshing = true, error = null)
                        currentPage == 0 -> it.copy(isLoading = true, error = null)
                        else -> it.copy(isLoadingMore = true, error = null)
                    }
                }

                val response = NetworkModule.mangaDexApi.getPopularManga(
                    offset = currentPage * pageSize,
                    limit = pageSize
                )

                _uiState.update { state ->
                    state.copy(
                        mangas = if (refresh || currentPage == 0) {
                            response.data
                        } else {
                            state.mangas + response.data
                        },
                        isLoading = false,
                        isLoadingMore = false,
                        isRefreshing = false,
                        hasMoreItems = response.data.size >= pageSize
                    )
                }
                currentPage++
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "Unknown error occurred",
                        isLoading = false,
                        isLoadingMore = false,
                        isRefreshing = false
                    )
                }
            }
        }
    }

    fun searchManga(query: String) {
        currentQuery = query
        searchJob?.cancel()
        
        if (query.isBlank()) {
            loadPopularManga(refresh = true)
            return
        }
        
        searchJob = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val response = NetworkModule.mangaDexApi.searchManga(
                    title = query,
                    offset = 0,
                    limit = pageSize
                )
                _uiState.update { 
                    it.copy(
                        mangas = response.data,
                        isLoading = false,
                        hasMoreItems = response.data.size >= pageSize
                    )
                }
                currentPage = 1
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "Unknown error occurred",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMoreItems) return
        
        if (currentQuery.isBlank()) {
            loadPopularManga()
        } else {
            loadMoreSearch()
        }
    }

    private fun loadMoreSearch() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoadingMore = true, error = null) }
                val response = NetworkModule.mangaDexApi.searchManga(
                    title = currentQuery,
                    offset = currentPage * pageSize,
                    limit = pageSize
                )
                _uiState.update { state ->
                    state.copy(
                        mangas = state.mangas + response.data,
                        isLoadingMore = false,
                        hasMoreItems = response.data.size >= pageSize
                    )
                }
                currentPage++
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "Unknown error occurred",
                        isLoadingMore = false
                    )
                }
            }
        }
    }

    fun refresh() {
        if (currentQuery.isBlank()) {
            loadPopularManga(refresh = true)
        } else {
            searchManga(currentQuery)
        }
    }
} 