package space.arkady.mvvmclonespotify.data.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import space.arkady.mvvmclonespotify.R
import space.arkady.mvvmclonespotify.data.adapters.SongAdapter
import space.arkady.mvvmclonespotify.data.ui.viewmodel.MainViewModel
import space.arkady.mvvmclonespotify.extensions.Status.*
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {


    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var songAdapter: SongAdapter

    private fun setupRecyclerView() = rvAllSongs.apply {
        adapter = songAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        setupRecyclerView()
        subscribeToObservers()
        songAdapter.setOnItemCLickListener {
            mainViewModel.playOrToggleSong(it)
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                SUCCESS -> {
                    allSongsProgressBar.isVisible = false
                    result.data?.let { songs ->
                        songAdapter.songs = songs
                    }
                }
                ERROR -> Unit
                LOADING -> allSongsProgressBar.isVisible = true
            }
        }
    }
}