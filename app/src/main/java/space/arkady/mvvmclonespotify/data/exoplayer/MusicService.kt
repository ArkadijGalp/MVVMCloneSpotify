package space.arkady.mvvmclonespotify.data.exoplayer

import android.app.PendingIntent
import android.app.PendingIntent.*
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import space.arkady.mvvmclonespotify.data.exoplayer.callbacks.MusicPlaybackPreparer
import space.arkady.mvvmclonespotify.data.exoplayer.callbacks.MusicPlayerEventListener
import space.arkady.mvvmclonespotify.data.exoplayer.callbacks.MusicPlayerNotificationListener
import space.arkady.mvvmclonespotify.extensions.MEDIA_ROOT_ID
import space.arkady.mvvmclonespotify.extensions.NETWORK_ERROR
import space.arkady.mvvmclonespotify.extensions.SERVICE_TAG
import javax.inject.Inject


@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var datasourceFactory: DefaultDataSource.Factory

    @Inject
    lateinit var exoplayer: ExoPlayer

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    private lateinit var musicNotificationManager: MusicNotificationManager

    private val servicejob = Job()
    private val serviceScope = CoroutineScope(context = Dispatchers.Main + servicejob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    var isForegroundService = false

    private var curPlayingSong: MediaMetadataCompat? = null

    private var isPlayerInitialized = false

    companion object {
        var curSongDuration = 0L
            private set
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            firebaseMusicSource.fetchMediaData()
        }


        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, FLAG_IMMUTABLE)
        }
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ) {
            curSongDuration = exoplayer.duration
        }

        val musicPlaybackPreparer = MusicPlaybackPreparer(firebaseMusicSource) {
            curPlayingSong = it
            preparePlayer(
                firebaseMusicSource.songs,
                it,
                true
            )
        }
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(exoplayer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)

        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoplayer.addListener(musicPlayerEventListener)
        musicNotificationManager
    }

    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val curSongIndex = if (curPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        exoplayer.setMediaSource(firebaseMusicSource.asMediaSource(datasourceFactory))
        exoplayer.seekTo(curSongIndex, 0L)
        exoplayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoplayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        exoplayer.release()

        exoplayer.removeListener(musicPlayerEventListener)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            MEDIA_ROOT_ID -> {
                val resultSent = firebaseMusicSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(firebaseMusicSource.asMediaItems())
                    }
                    if (!isInitialized && firebaseMusicSource.songs.isNotEmpty()) {
                        preparePlayer(
                            firebaseMusicSource.songs,
                            firebaseMusicSource.songs[0],
                            false
                        )
                        isPlayerInitialized = true
                    } else {
                        mediaSession.sendSessionEvent(NETWORK_ERROR, null)

                    }
                }
                if (!resultSent) {
                    result.detach()
                }
            }
        }
    }
}