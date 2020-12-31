package com.listen.otic.ui.fragments.album

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.rxkprefs.Pref
import com.listen.otic.PREF_ALBUM_SORT_ORDER
import com.listen.otic.R
import com.listen.otic.constants.AlbumSortOrder
import com.listen.otic.constants.AlbumSortOrder.ALBUM_A_Z
import com.listen.otic.constants.AlbumSortOrder.ALBUM_Z_A
import com.listen.otic.constants.AlbumSortOrder.ALBUM_YEAR
import com.listen.otic.constants.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS
import com.listen.otic.extensions.addOnItemClick
import com.listen.otic.extensions.filter
import com.listen.otic.extensions.inflateTo
import com.listen.otic.extensions.observe
import com.listen.otic.extensions.disposeOnDetach
import com.listen.otic.extensions.ioToMain
import com.listen.otic.extensions.safeActivity
import com.listen.otic.models.Album
import com.listen.otic.ui.adapters.AlbumAdapter
import com.listen.otic.ui.fragments.base.MediaItemFragment
import com.listen.otic.ui.listeners.SortMenuListener
import com.listen.otic.util.SpacesItemDecoration
import kotlinx.android.synthetic.main.layout_recyclerview_padding.recyclerView
import org.koin.android.ext.android.inject

class AlbumsFragment : MediaItemFragment() {
    private lateinit var albumAdapter: AlbumAdapter
    private val sortOrderPref by inject<Pref<AlbumSortOrder>>(name = PREF_ALBUM_SORT_ORDER)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflateTo(R.layout.layout_recyclerview_padding, container)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        albumAdapter = AlbumAdapter().apply {
            this.notifyDataSetChanged()
            showHeader = true
            sortMenuListener = sortListener
        }

        recyclerView.apply {
            layoutManager = GridLayoutManager(safeActivity, 2).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position == 0) 2 else 1
                    }
                }
            }
            adapter = albumAdapter
            addOnItemClick { position: Int, _: View ->
                albumAdapter.getAlbumForPosition(position)?.let {
                    mainViewModel.mediaItemClicked(it, null)
                }
            }

            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.album_art_spacing)
            addItemDecoration(SpacesItemDecoration(spacingInPixels))
        }

        mediaItemFragmentViewModel.mediaItems
                .filter { it.isNotEmpty() }
                .observe(this) { list ->
                    @Suppress("UNCHECKED_CAST")
                    albumAdapter.updateData(list as List<Album>)
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Auto trigger a reload when the sort order pref changes
        sortOrderPref.observe()
                .ioToMain()
                .subscribe { mediaItemFragmentViewModel.reloadMediaItems() }
                .disposeOnDetach(view)
    }

    private val sortListener = object : SortMenuListener {
        override fun shuffleAll() {}

        override fun sortAZ() {
            sortOrderPref.set(ALBUM_A_Z)
        }

        override fun sortDuration() {}

        override fun sortYear() {
            sortOrderPref.set(ALBUM_YEAR)
        }

        override fun sortZA() {
            sortOrderPref.set(ALBUM_Z_A)
        }

        override fun numOfSongs() {
            sortOrderPref.set(ALBUM_NUMBER_OF_SONGS)
        }
    }
}
