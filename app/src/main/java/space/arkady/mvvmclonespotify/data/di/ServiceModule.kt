package space.arkady.mvvmclonespotify.data.di

import android.content.Context
import android.media.AudioAttributes
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import space.arkady.mvvmclonespotify.data.remote.MusicDatabase


@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideMusicDatabase() = MusicDatabase()

    @ServiceScoped
    @Provides
    fun provideAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(
                AudioAttributes.USAGE_MEDIA
            ).build()

    @ServiceScoped
    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context
    ) = ExoPlayer.Builder(context).build().apply {
        setAudioAttributes(audioAttributes, true)
        setHandleAudioBecomingNoisy(true)
    }

    @ServiceScoped
    @Provides
    fun provideDataSourceFactory(
        @ApplicationContext context: Context
    ) = DefaultDataSource.Factory(context)
}