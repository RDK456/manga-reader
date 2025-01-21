package com.example.mangareader.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangareader.data.api.ChapterDto
import com.example.mangareader.data.api.MangaDto
import com.example.mangareader.di.NetworkModule
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MangaDetailsUiState(
    val manga: MangaDto? = null,
    val chapters: List<ChapterDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MangaDetailsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MangaDetailsUiState())
    val uiState: StateFlow<MangaDetailsUiState> = _uiState.asStateFlow()

    fun loadMangaDetails(mangaId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Load manga details and chapters in parallel
                val mangaDeferred = viewModelScope.async { 
                    NetworkModule.mangaDexApi.getMangaDetails(mangaId)
                }
                val chaptersDeferred = viewModelScope.async {
                    NetworkModule.mangaDexApi.getMangaChapters(mangaId)
                }

                val manga = mangaDeferred.await()
                val chapters = chaptersDeferred.await()

                _uiState.update { 
                    it.copy(
                        manga = manga.data,
                        chapters = chapters.data,
                        isLoading = false
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
} 