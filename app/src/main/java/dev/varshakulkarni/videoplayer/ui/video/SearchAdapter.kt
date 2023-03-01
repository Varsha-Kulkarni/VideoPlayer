package dev.varshakulkarni.videoplayer.ui.video

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import dev.varshakulkarni.videoplayer.R
import dev.varshakulkarni.videoplayer.data.db.entity.VideoEntity

class SearchAdapter(
    context: Context,
    private val layout: Int,
    private val videos: List<VideoEntity>?
) : ArrayAdapter<VideoEntity>(
    context, layout,
    videos as MutableList<VideoEntity>
) {
    override fun getCount(): Int {
        return videos?.size ?: 0
    }

    override fun getItem(position: Int): VideoEntity? {
        return videos?.get(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val retView: View
        val vi = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        retView = convertView ?: vi.inflate(layout, null)
        val videoEntity = getItem(position)
        val title = retView.findViewById(R.id.tvSearchText) as TextView
        title.text = videoEntity?.title
        return retView
    }
}