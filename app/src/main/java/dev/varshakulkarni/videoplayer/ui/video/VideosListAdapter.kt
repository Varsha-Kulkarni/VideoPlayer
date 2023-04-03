package dev.varshakulkarni.videoplayer.ui.video

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import dev.varshakulkarni.videoplayer.databinding.ItemVideoBinding
import dev.varshakulkarni.videoplayer.utils.formatDurationString


class VideosListAdapter(
    private val onVideoClick: (VideoItem) -> Unit
) : ListAdapter<VideoItem, VideosListAdapter.VideoViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VideoViewHolder(
        ItemVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position), onVideoClick)
    }

    inner class VideoViewHolder(
        private val binding: ItemVideoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(videoItem: VideoItem, onVideoClick: (VideoItem) -> Unit) {
            with(binding) {
                tvVideoDuration.text =
                    videoItem.duration.formatDurationString(tvVideoDuration.context)
                Glide.with(ivThumbnail.context)
                    .load(videoItem.uri)
                    .transform(CenterCrop())
                    .into(ivThumbnail)
                root.setOnClickListener { onVideoClick(videoItem) }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<VideoItem>() {
            override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem) =
                oldItem == newItem
        }
    }
}