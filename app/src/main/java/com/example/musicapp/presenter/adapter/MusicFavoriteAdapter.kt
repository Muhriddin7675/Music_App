package com.example.musicapp.presenter.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.musicapp.data.local.room.entity.MusicEntity
import com.example.musicapp.databinding.ItemMusicBinding
import com.example.musicapp.util.MyAppManager
import com.example.musicapp.util.getMusicDataByPosition

class MusicFavoriteAdapter : ListAdapter<MusicEntity, MusicFavoriteAdapter.LibraryInnerViewHolder>(
    LibraryDiffUtil
) {
    var cursor: Cursor? = MyAppManager.cursor
    var clickSelectMusicPosition: ((Int) -> Unit)? = null
    var clickSend: ((musicUri: Uri) -> Unit)? = null
    private var time = System.currentTimeMillis()

    object LibraryDiffUtil : DiffUtil.ItemCallback<MusicEntity>() {
        override fun areItemsTheSame(oldItem: MusicEntity, newItem: MusicEntity): Boolean =
            oldItem.musicId == newItem.musicId

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: MusicEntity, newItem: MusicEntity): Boolean =
            oldItem == newItem
    }

    inner class LibraryInnerViewHolder(private val binding: ItemMusicBinding) :
        ViewHolder(binding.root) {
        init {
            binding.viewRoot.setOnClickListener {
                if (System.currentTimeMillis() - time > 500) {
                    clickSelectMusicPosition?.invoke(getItem(adapterPosition).adapterPos)
                }
                time = System.currentTimeMillis()
            }
            binding.send.setOnClickListener {
                val musicData =
                    cursor?.getMusicDataByPosition(getItem(adapterPosition).adapterPos)
                if (musicData != null) {
                    val musicUri = getMusicUri(binding.root.context, musicData.id.toLong())
                    if (musicUri != null) {
                        clickSend?.invoke(musicUri)
                    }
                }
            }
        }

        fun bind(data: MusicEntity) {
            binding.musicName.text = data.title
            binding.musicAuthor.text = data.artist
            if (MyAppManager.selectMusicPos == data.adapterPos) {
                binding.musicName.setTextColor(Color.parseColor("#6156E1"))
                binding.saund.isVisible = true
            } else {
                binding.musicName.setTextColor(Color.WHITE) // Or any other default color
                binding.saund.isVisible = false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryInnerViewHolder =
        LibraryInnerViewHolder(
            ItemMusicBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: LibraryInnerViewHolder, position: Int) =
        holder.bind(getItem(position))


    fun setOnClickMusicPosition(block: (Int) -> Unit) {
        clickSelectMusicPosition = block
    }

    fun setOnClickSendMusic(block: (musicUri: Uri) -> Unit) {
        clickSend = block
    }

    fun getMusicUri(context: Context, musicId: Long): Uri? {
        val contentResolver = context.contentResolver
        val uri =
            Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicId.toString())
        return uri
    }

}