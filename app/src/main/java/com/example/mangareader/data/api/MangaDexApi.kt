package com.example.mangareader.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MangaDexApi {
    @GET("manga")
    suspend fun searchManga(
        @Query("title") title: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("includes[]") includes: List<String> = listOf("cover_art"),
        @Query("contentRating[]") contentRating: List<String> = listOf("safe", "suggestive"),
        @Query("availableTranslatedLanguage[]") languages: List<String> = listOf("en")
    ): MangaResponse

    @GET("manga/{mangaId}")
    suspend fun getMangaDetails(
        @Path("mangaId") mangaId: String,
        @Query("includes[]") includes: List<String> = listOf("cover_art")
    ): SingleMangaResponse

    @GET("manga/{mangaId}/feed")
    suspend fun getMangaChapters(
        @Path("mangaId") mangaId: String,
        @Query("translatedLanguage[]") languages: List<String> = listOf("en"),
        @Query("order[chapter]") order: String = "asc"
    ): ChapterResponse

    @GET("at-home/server/{chapterId}")
    suspend fun getChapterPages(
        @Path("chapterId") chapterId: String
    ): ChapterPagesResponse

    @GET("manga")
    suspend fun getPopularManga(
        @Query("order[followedCount]") order: String = "desc",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("includes[]") includes: List<String> = listOf("cover_art"),
        @Query("contentRating[]") contentRating: List<String> = listOf("safe", "suggestive"),
        @Query("availableTranslatedLanguage[]") languages: List<String> = listOf("en")
    ): MangaResponse
}

data class MangaResponse(
    val result: String,
    val response: String,
    val data: List<MangaDto>,
    val limit: Int,
    val offset: Int,
    val total: Int
)

data class SingleMangaResponse(
    val result: String,
    val response: String,
    val data: MangaDto
)

data class MangaDto(
    val id: String,
    val type: String,
    val attributes: MangaAttributes,
    val relationships: List<Relationship>? = null
)

data class MangaAttributes(
    val title: Map<String, String>,
    val description: Map<String, String>,
    val status: String,
    val year: Int?,
    val contentRating: String,
    val tags: List<TagDto>
)

data class TagDto(
    val id: String,
    val type: String,
    val attributes: TagAttributes
)

data class TagAttributes(
    val name: Map<String, String>,
    val group: String
)

data class ChapterResponse(
    val result: String,
    val data: List<ChapterDto>
)

data class ChapterDto(
    val id: String,
    val type: String,
    val attributes: ChapterAttributes
)

data class ChapterAttributes(
    val volume: String?,
    val chapter: String?,
    val title: String?,
    val pages: Int,
    val publishAt: String,
    val translatedLanguage: String
)

data class ChapterPagesResponse(
    val result: String,
    val baseUrl: String,
    val chapter: ChapterData
)

data class ChapterData(
    val hash: String,
    val data: List<String>,
    val dataSaver: List<String>
)

data class Relationship(
    val id: String,
    val type: String,
    val attributes: CoverArtAttributes? = null
)

data class CoverArtAttributes(
    val fileName: String
) 