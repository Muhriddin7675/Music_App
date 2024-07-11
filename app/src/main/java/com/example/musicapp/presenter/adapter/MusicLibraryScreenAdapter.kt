package com.example.musicapp.presenter.adapter

import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.musicapp.databinding.ItemMusicBinding
import com.example.musicapp.util.MyAppManager
import com.example.musicapp.util.getMusicDataByPosition

class MusicLibraryScreenAdapter :
    RecyclerView.Adapter<MusicLibraryScreenAdapter.MusicLibraryViewHolder>() {
    var cursor: Cursor? = null
    var clickSelectMusicPosition: ((Int) -> Unit)? = null
    var clickSend: (( musicUri: Uri) -> Unit)? = null


    inner class MusicLibraryViewHolder(private val binding: ItemMusicBinding) :
        ViewHolder(binding.root) {
        init {
            binding.viewRoot.setOnClickListener {
                clickSelectMusicPosition?.invoke(adapterPosition)
            }
            binding.send.setOnClickListener {
                val musicData = cursor?.getMusicDataByPosition(adapterPosition)
                if (musicData != null) {
                    val musicUri = getMusicUri(binding.root.context,musicData.id.toLong())
                    if (musicUri != null) {
                        clickSend?.invoke(musicUri)
                    }
                }
            }
        }

        fun bind() {
            if (cursor != null && cursor!!.moveToPosition(adapterPosition)) {
                cursor!!.getMusicDataByPosition(adapterPosition).let {
                    binding.musicName.text = it.title
                    binding.musicAuthor.text = it.artist
                    if (MyAppManager.selectMusicPos == adapterPosition) {
                        binding.musicName.setTextColor(Color.parseColor("#6156E1"))
                        binding.saund.isVisible = true
                    } else {
                        binding.musicName.setTextColor(Color.WHITE) // Or any other default color
                        binding.saund.isVisible = false
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicLibraryViewHolder =
        MusicLibraryViewHolder(
            ItemMusicBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = cursor?.count ?: 0

    override fun onBindViewHolder(holder: MusicLibraryViewHolder, position: Int) = holder.bind()

    fun setOnClickMusicPosition(block: (Int) -> Unit) {
        clickSelectMusicPosition = block
    }

    fun setOnClickSendMusic(block: (musicUri: Uri) -> Unit) {
        clickSend = block
    }
    fun getMusicUri(context: Context, musicId: Long): Uri? {
        val contentResolver = context.contentResolver
        val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicId.toString())
        return uri
    }
}