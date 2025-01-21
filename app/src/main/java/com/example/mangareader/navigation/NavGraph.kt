package com.example.mangareader.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mangareader.ui.screens.manga.MangaListScreen
import com.example.mangareader.ui.screens.details.MangaDetailsScreen
import com.example.mangareader.ui.screens.reader.ChapterReaderScreen

sealed class Screen(val route: String) {
    object MangaList : Screen("manga_list")
    object MangaDetails : Screen("manga_details/{mangaId}") {
        fun createRoute(mangaId: String) = "manga_details/$mangaId"
    }
    object ChapterReader : Screen("chapter_reader/{chapterId}") {
        fun createRoute(chapterId: String) = "chapter_reader/$chapterId"
    }
}

@Composable
fun MangaReaderNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.MangaList.route
    ) {
        composable(Screen.MangaList.route) {
            MangaListScreen(
                onMangaClick = { mangaId ->
                    navController.navigate(Screen.MangaDetails.createRoute(mangaId))
                }
            )
        }

        composable(
            route = Screen.MangaDetails.route,
            arguments = listOf(navArgument("mangaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getString("mangaId") ?: return@composable
            MangaDetailsScreen(
                mangaId = mangaId,
                onChapterClick = { chapterId ->
                    navController.navigate(Screen.ChapterReader.createRoute(chapterId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ChapterReader.route,
            arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: return@composable
            ChapterReaderScreen(
                chapterId = chapterId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
} 