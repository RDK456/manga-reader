package com.example.mangareader.ui.screens.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mangareader.data.api.ChapterDto
import com.example.mangareader.data.api.MangaDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDetailsScreen(
    mangaId: String,
    onChapterClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MangaDetailsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(mangaId) {
        viewModel.loadMangaDetails(mangaId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.manga?.attributes?.title?.get("en") ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            else -> {
                MangaDetailsContent(
                    manga = uiState.manga,
                    chapters = uiState.chapters,
                    onChapterClick = onChapterClick,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun MangaDetailsContent(
    manga: MangaDto?,
    chapters: List<ChapterDto>,
    onChapterClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (manga != null) {
            item {
                MangaHeader(manga = manga)
            }
        }

        item {
            Text(
                text = "Chapters",
                style = MaterialTheme.typography.titleLarge
            )
        }

        items(chapters) { chapter ->
            ChapterItem(
                chapter = chapter,
                onClick = { onChapterClick(chapter.id) }
            )
        }
    }
}

@Composable
private fun MangaHeader(
    manga: MangaDto,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        AsyncImage(
            model = "https://uploads.mangadex.org/covers/${manga.id}/cover.jpg",
            contentDescription = manga.attributes.title["en"],
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = manga.attributes.description["en"] ?: "",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterItem(
    chapter: ChapterDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Chapter ${chapter.attributes.chapter}",
                style = MaterialTheme.typography.titleMedium
            )
            if (!chapter.attributes.title.isNullOrBlank()) {
                Text(
                    text = chapter.attributes.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
} 