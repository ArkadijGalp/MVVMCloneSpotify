package space.arkady.mvvmclonespotify.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import space.arkady.mvvmclonespotify.data.entities.Song
import space.arkady.mvvmclonespotify.extensions.SONG_COLLECTION
import java.lang.Exception

class MusicDatabase {

    private val firestore = Firebase.firestore
    private val songCollection = firestore.collection(SONG_COLLECTION)

    suspend fun getAllSongs(): List<Song>{
        return try {
            songCollection.get().await().toObjects(Song::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}