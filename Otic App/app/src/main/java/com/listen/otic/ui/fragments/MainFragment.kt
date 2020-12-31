package com.listen.otic.ui.fragments

import android.animation.AnimatorInflater.loadStateListAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.afollestad.rxkprefs.Pref
import com.google.android.material.appbar.AppBarLayout
import com.listen.otic.PREF_START_PAGE
import com.listen.otic.R
import com.listen.otic.OticAudioService.Companion.TYPE_ALL_ALBUMS
import com.listen.otic.OticAudioService.Companion.TYPE_ALL_SONGS
import com.listen.otic.constants.StartPage
import com.listen.otic.extensions.addFragment
import com.listen.otic.extensions.drawable
import com.listen.otic.extensions.inflateTo
import com.listen.otic.extensions.safeActivity
import com.listen.otic.models.MediaID
import com.listen.otic.ui.activities.MainActivity
import com.listen.otic.ui.activities.SettingsActivity
import com.listen.otic.ui.dialogs.AboutDialog
import com.listen.otic.ui.fragments.base.MediaItemFragment
import kotlinx.android.synthetic.main.main_fragment.appBar
import kotlinx.android.synthetic.main.main_fragment.tabLayout
import kotlinx.android.synthetic.main.main_fragment.viewpager
import kotlinx.android.synthetic.main.toolbar.btnSearch
import kotlinx.android.synthetic.main.toolbar.mediaRouteButton
import kotlinx.android.synthetic.main.toolbar.toolbar
import org.koin.android.ext.android.inject

class MainFragment : Fragment() {
    private val startPagePref by inject<Pref<StartPage>>(name = PREF_START_PAGE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflateTo(R.layout.main_fragment, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        setupViewPager(viewpager)
        tabLayout.setupWithViewPager(viewpager)

        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val animatorRes = if (verticalOffset == 0) {
                R.animator.appbar_elevation_disable
            } else {
                R.animator.appbar_elevation_enable
            }
            appBar.stateListAnimator = loadStateListAnimator(context, animatorRes)
        })

        toolbar.overflowIcon = safeActivity.drawable(R.drawable.ic_more_vert_black_24dp)

        val mainActivity = safeActivity as MainActivity
        mainActivity.setSupportActionBar(toolbar)
        mainActivity.supportActionBar?.run {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(false)
        }

        btnSearch.setOnClickListener { safeActivity.addFragment(fragment = SearchFragment()) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (safeActivity as MainActivity).setupCastButton(mediaRouteButton)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
          //  R.id.menu_item_about -> AboutDialog.show(safeActivity)
            R.id.menu_item_settings -> startActivity(Intent(activity, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val res = context?.resources ?: return
        val adapter = Adapter(childFragmentManager).apply {
            addFragment(
                    fragment = MediaItemFragment.newInstance(MediaID(TYPE_ALL_SONGS.toString(), null)),
                    title = res.getString(R.string.songs)
            )
            addFragment(
                    fragment = MediaItemFragment.newInstance(MediaID(TYPE_ALL_ALBUMS.toString(), null)),
                    title = res.getString(R.string.current)
            )
            addFragment(
                    fragment = MediaItemFragment.newInstance(MediaID(TYPE_ALL_ALBUMS.toString(), null)),
                    title = res.getString(R.string.new_books)
            )
            addFragment(
                    fragment = MediaItemFragment.newInstance(MediaID(TYPE_ALL_ALBUMS.toString(), null)),
                    title = res.getString(R.string.finished)
            )
           /* addFragment(
                    fragment = MediaItemFragment.newInstance(MediaID(TYPE_ALL_PLAYLISTS.toString(), null)),
                    title = res.getString(R.string.playlists)
            )
            addFragment(
                    fragment = MediaItemFragment.newInstance(MediaID(TYPE_ALL_ARTISTS.toString(), null)),
                    title = res.getString(R.string.artists)
            )
            addFragment(
                    fragment = MediaItemFragment.newInstance(MediaID(TYPE_ALL_FOLDERS.toString(), null)),
                    title = res.getString(R.string.folders)
            )
            addFragment(
                    fragment = MediaItemFragment.newInstance(MediaID(TYPE_ALL_GENRES.toString(), null)),
                    title = res.getString(R.string.genres)
            )*/
        }
        adapter.notifyDataSetChanged()
        viewPager.adapter = adapter
        viewpager.offscreenPageLimit = 1
        viewPager.setCurrentItem(startPagePref.get().index, false)
    }

    internal class Adapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private val fragments = ArrayList<Fragment>()
        private val titles = ArrayList<String>()

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getItem(position: Int) = fragments[position]

        override fun getCount() = fragments.size

        override fun getPageTitle(position: Int) = titles[position]
    }
}
