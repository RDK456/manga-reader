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
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 1
)

class MangaListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MangaListUiState())
    val uiState: StateFlow<MangaListUiState> = _uiState.asStateFlow()
    
    private var currentQuery = ""
    private var searchJob: Job? = null
    private val pageSize = 18  // Changed to show 3x3 grid

    init {
        loadPopularManga()
    }

    private fun loadPopularManga() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val response = NetworkModule.mangaDexApi.getPopularManga(
                    offset = _uiState.value.currentPage * pageSize,
                    limit = pageSize
                )

                val totalPages = (response.total + pageSize - 1) / pageSize
                
                _uiState.update { state ->
                    state.copy(
                        mangas = response.data,
                        isLoading = false,
                        isRefreshing = false,
                        totalPages = totalPages
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "Unknown error occurred",
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
        }
    }

    fun loadPage(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
        if (currentQuery.isBlank()) {
            loadPopularManga()
        } else {
            searchManga(currentQuery)
        }
    }

    fun searchManga(query: String) {
        currentQuery = query
        searchJob?.cancel()
        
        if (query.isBlank()) {
            loadPopularManga()
            return
        }
        
        searchJob = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val response = NetworkModule.mangaDexApi.searchManga(
                    title = query,
                    offset = _uiState.value.currentPage * pageSize,
                    limit = pageSize
                )

                val totalPages = (response.total + pageSize - 1) / pageSize
                
                _uiState.update { 
                    it.copy(
                        mangas = response.data,
                        isLoading = false,
                        totalPages = totalPages
                    )
                }
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

    fun refresh() {
        _uiState.update { it.copy(currentPage = 0) }
        if (currentQuery.isBlank()) {
            loadPopularManga()
        } else {
            searchManga(currentQuery)
        }
    }
} 