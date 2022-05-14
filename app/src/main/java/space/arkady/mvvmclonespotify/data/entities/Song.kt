package space.arkady.mvvmclonespotify.data.entities

import android.media.browse.MediaBrowser

data class Song(
    val mediaId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val songUrl: String = "",
    val imageUrl: String = ""
)