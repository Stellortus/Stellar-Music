package top.stellortus.stellarmusic.network

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistDto(
    val id: Int,
    val ownerId: Int,
    val name: String,
    val description: String? = null,
    val coverPath: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val trackCount: Int? = null
)

@Serializable
data class PlaylistDetailDto(
    val id: Int,
    val ownerId: Int,
    val name: String,
    val description: String? = null,
    val coverPath: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val tracks: List<TrackDto> = emptyList()
)

/** 与 data/Track.kt 的 Track 字段一一对应，服务端返回的曲目结构。 */
@Serializable
data class TrackDto(
    val id: Int = 0,
    val title: String,
    val artists: List<String> = emptyList(),
    val fileName: String = "",
    val uploader: String = "",
    val coverPath: String = "",
    val lyricsPath: String = "",
    val createdAt: Long = 0L
)

/** name 全部为空时序列化为 {}，由服务端生成“歌单N”。 */
@Serializable
data class CreatePlaylistRequest(
    val name: String? = null,
    val description: String? = null,
    val coverPath: String? = null
)

@Serializable
data class UpdatePlaylistRequest(
    val name: String? = null,
    val description: String? = null,
    val coverPath: String? = null
)

@Serializable
data class AddTracksRequest(val trackIds: List<Int>)
