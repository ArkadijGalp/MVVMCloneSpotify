package space.arkady.mvvmclonespotify.data.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.list_item.view.*
import space.arkady.mvvmclonespotify.R
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseSongAdapter(R.layout.list_item) {

    override val differ = AsyncListDiffer(this, diffCallBack)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {
            tvPrimary.text = song.title
            tvSecondary.text = song.subtitle
            glide.load(song.imageUrl).into(ivItemImage)

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }
}