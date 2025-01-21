package com.example.mangareader.ui.screens.reader

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.mangareader.di.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChapterReaderUiState(
    val pages: List<String> = emptyList(),
    val currentPage: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isVerticalMode: Boolean = false,
    val isRightToLeft: Boolean = true,  // Manga default
    val showControls: Boolean = true
)

class ChapterReaderViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChapterReaderUiState())
    val uiState: StateFlow<ChapterReaderUiState> = _uiState.asStateFlow()

    fun loadChapter(chapterId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val response = NetworkModule.mangaDexApi.getChapterPages(chapterId)
                
                val pages = response.chapter.data.map { page ->
                    "${response.baseUrl}/data/${response.chapter.hash}/$page"
                }
                
                _uiState.update { 
                    it.copy(
                        pages = pages,
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

    fun toggleReadingMode() {
        _uiState.update { it.copy(isVerticalMode = !it.isVerticalMode) }
    }

    fun toggleReadingDirection() {
        _uiState.update { it.copy(isRightToLeft = !it.isRightToLeft) }
    }

    fun toggleControls() {
        _uiState.update { it.copy(showControls = !it.showControls) }
    }

    fun nextPage() {
        _uiState.update { state ->
            if (state.currentPage < state.pages.size - 1) {
                state.copy(currentPage = state.currentPage + 1)
            } else {
                state
            }
        }
    }

    fun previousPage() {
        _uiState.update { state ->
            if (state.currentPage > 0) {
                state.copy(currentPage = state.currentPage - 1)
            } else {
                state
            }
        }
    }
} 