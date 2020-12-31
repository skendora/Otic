package com.listen.otic.ui.fragments.base

import android.os.Bundle
import com.listen.otic.OticAudioService.Companion.MEDIA_CALLER
import com.listen.otic.OticAudioService.Companion.MEDIA_ID_ARG
import com.listen.otic.OticAudioService.Companion.MEDIA_TYPE_ARG
import com.listen.otic.OticAudioService.Companion.TYPE_ALBUM
import com.listen.otic.OticAudioService.Companion.TYPE_ALL_ALBUMS
import com.listen.otic.OticAudioService.Companion.TYPE_ALL_ARTISTS
import com.listen.otic.OticAudioService.Companion.TYPE_ALL_FOLDERS
import com.listen.otic.OticAudioService.Companion.TYPE_ALL_GENRES
import com.listen.otic.OticAudioService.Companion.TYPE_ALL_PLAYLISTS
import com.listen.otic.OticAudioService.Companion.TYPE_ALL_SONGS
import com.listen.otic.OticAudioService.Companion.TYPE_ARTIST
import com.listen.otic.OticAudioService.Companion.TYPE_GENRE
import com.listen.otic.OticAudioService.Companion.TYPE_PLAYLIST
import com.listen.otic.constants.Constants.ACTION_REMOVED_FROM_PLAYLIST
import com.listen.otic.constants.Constants.ACTION_SONG_DELETED
import com.listen.otic.constants.Constants.ALBUM
import com.listen.otic.constants.Constants.ARTIST
import com.listen.otic.constants.Constants.CATEGORY_SONG_DATA
import com.listen.otic.extensions.argumentOrEmpty
import com.listen.otic.extensions.map
import com.listen.otic.extensions.observe
import com.listen.otic.models.CategorySongData
import com.listen.otic.models.Genre
import com.listen.otic.models.MediaID
import com.listen.otic.models.Playlist
import com.listen.otic.ui.fragments.FolderFragment
import com.listen.otic.ui.fragments.GenreFragment
import com.listen.otic.ui.fragments.PlaylistFragment
import com.listen.otic.ui.fragments.album.AlbumDetailFragment
import com.listen.otic.ui.fragments.album.AlbumsFragment
import com.listen.otic.ui.fragments.artist.ArtistDetailFragment
import com.listen.otic.ui.fragments.artist.ArtistFragment
import com.listen.otic.ui.fragments.songs.CategorySongsFragment
import com.listen.otic.ui.fragments.songs.SongsFragment
import com.listen.otic.ui.viewmodels.MediaItemFragmentViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

open class MediaItemFragment : BaseNowPlayingFragment() {

    protected lateinit var mediaItemFragmentViewModel: MediaItemFragmentViewModel

    private lateinit var mediaType: String
    private var mediaId: String? = null
    private var caller: String? = null

    companion object {
        fun newInstance(mediaId: MediaID): MediaItemFragment {
            val args = Bundle().apply {
                putString(MEDIA_TYPE_ARG, mediaId.type)
                putString(MEDIA_ID_ARG, mediaId.mediaId)
                putString(MEDIA_CALLER, mediaId.caller)
            }
            return when (mediaId.type?.toInt()) {
                TYPE_ALL_SONGS -> SongsFragment().apply { arguments = args }
                TYPE_ALL_ALBUMS -> AlbumsFragment().apply { arguments = args }
                TYPE_ALL_PLAYLISTS -> PlaylistFragment().apply { arguments = args }
                TYPE_ALL_ARTISTS -> ArtistFragment().apply { arguments = args }
                TYPE_ALL_FOLDERS -> FolderFragment().apply { arguments = args }
                TYPE_ALL_GENRES -> GenreFragment().apply { arguments = args }
                TYPE_ALBUM -> AlbumDetailFragment().apply {
                    arguments = args.apply { putParcelable(ALBUM, mediaId.mediaItem) }
                }
                TYPE_ARTIST -> ArtistDetailFragment().apply {
                    arguments = args.apply { putParcelable(ARTIST, mediaId.mediaItem) }
                }
                TYPE_PLAYLIST -> CategorySongsFragment().apply {
                    arguments = args.apply {
                        (mediaId.mediaItem as Playlist).apply {
                            val data = CategorySongData(name, songCount, TYPE_PLAYLIST, id)
                            putParcelable(CATEGORY_SONG_DATA, data)
                        }
                    }
                }
                TYPE_GENRE -> CategorySongsFragment().apply {
                    arguments = args.apply {
                        (mediaId.mediaItem as Genre).apply {
                            val data = CategorySongData(name, songCount, TYPE_GENRE, id)
                            putParcelable(CATEGORY_SONG_DATA, data)
                        }
                    }
                }
                else -> SongsFragment().apply {
                    arguments = args
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mediaType = argumentOrEmpty(MEDIA_TYPE_ARG)
        mediaId = argumentOrEmpty(MEDIA_ID_ARG)
        caller = argumentOrEmpty(MEDIA_CALLER)

        val mediaId = MediaID(mediaType, mediaId, caller)
        mediaItemFragmentViewModel = getViewModel { parametersOf(mediaId) }

        mainViewModel.customAction
                .map { it.getContentIfNotHandled() }
                .observe(this) {
                    when (it) {
                        ACTION_SONG_DELETED -> mediaItemFragmentViewModel.reloadMediaItems()
                        ACTION_REMOVED_FROM_PLAYLIST -> mediaItemFragmentViewModel.reloadMediaItems()
                    }
                }
    }
}
