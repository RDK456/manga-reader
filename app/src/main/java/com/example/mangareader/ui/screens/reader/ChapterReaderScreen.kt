package com.example.mangareader.ui.screens.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChapterReaderScreen(
    chapterId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChapterReaderViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(chapterId) {
        viewModel.loadChapter(chapterId)
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = uiState.showControls,
                enter = slideInVertically(),
                exit = slideOutVertically()
            ) {
                TopAppBar(
                    title = { Text("Chapter Reader") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleReadingMode() }) {
                            Icon(
                                if (uiState.isVerticalMode) Icons.Default.ViewWeek
                                else Icons.Default.ViewAgenda,
                                contentDescription = "Toggle reading mode"
                            )
                        }
                        IconButton(onClick = { viewModel.toggleReadingDirection() }) {
                            Icon(
                                if (uiState.isRightToLeft) Icons.Default.ArrowBack
                                else Icons.Default.ArrowForward,
                                contentDescription = "Toggle reading direction"
                            )
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                uiState.pages.isNotEmpty() -> {
                    if (uiState.isVerticalMode) {
                        VerticalReader(
                            pages = uiState.pages,
                            onTap = { viewModel.toggleControls() }
                        )
                    } else {
                        HorizontalReader(
                            pages = uiState.pages,
                            isRightToLeft = uiState.isRightToLeft,
                            onTap = { viewModel.toggleControls() },
                            scope = scope
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalReader(
    pages: List<String>,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { onTap() }
            }
    ) {
        items(pages) { pageUrl ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(pageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HorizontalReader(
    pages: List<String>,
    isRightToLeft: Boolean,
    onTap: () -> Unit,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        pageCount = { pages.size },
        initialPage = if (isRightToLeft) pages.size - 1 else 0
    )
    
    HorizontalPager(
        state = pagerState,
        reverseLayout = isRightToLeft,
        modifier = modifier.fillMaxSize()
    ) { page ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (offset.x < size.width / 3) {
                            scope.launch {
                                if (!isRightToLeft) pagerState.animateScrollToPage(page - 1)
                                else pagerState.animateScrollToPage(page + 1)
                            }
                        } else if (offset.x > size.width * 2 / 3) {
                            scope.launch {
                                if (!isRightToLeft) pagerState.animateScrollToPage(page + 1)
                                else pagerState.animateScrollToPage(page - 1)
                            }
                        } else {
                            onTap()
                        }
                    }
                }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(pages[page])
                    .crossfade(true)
                    .build(),
                contentDescription = "Page ${page + 1}",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
} 