/*
 * Copyright 2023 Varsha Kulkarni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
