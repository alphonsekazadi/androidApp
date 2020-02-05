package com.hedvig.app.feature.keygear.ui.tab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.app.BASE_MARGIN
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.createitem.label
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.key_gear_add_item.view.*
import kotlinx.android.synthetic.main.key_gear_item.view.*

class KeyGearItemsAdapter(
    private val createItem: (view: View) -> Unit,
    private val openItem: (view: View) -> Unit
) : RecyclerView.Adapter<KeyGearItemsAdapter.ViewHolder>() {
    var items: List<KeyGearItemsQuery.KeyGearItemsSimple> = listOf()

    override fun getItemViewType(position: Int) = when (position) {
        0 -> NEW_ITEM
        else -> ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        NEW_ITEM -> {
            ViewHolder.NewItem(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.key_gear_add_item,
                    parent,
                    false
                )
            )
        }
        ITEM -> {
            ViewHolder.Item(
                LayoutInflater.from(parent.context).inflate(R.layout.key_gear_item, parent, false)
            )
        }
        else -> {
            throw Error("Invalid viewType: $viewType")
        }
    }

    override fun getItemCount() = items.size + 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.NewItem -> {
                holder.root.setHapticClickListener { v ->
                    createItem(v)
                }
            }
            is ViewHolder.Item -> {
                val item = items[position - 1]
                holder.root.setHapticClickListener {
                    openItem(holder.image)
                }
                item.photos.getOrNull(0)?.let { photo ->
                    // TODO: Load the file proper
                    // Glide
                    //     .with(holder.image)
                    //     .load(TODO SOMETHING BASED ON THE PHOTO PROPER)
                    //     .transform(RoundedCorners(BASE_MARGIN))
                    //     .into(holder.image)
                } ?: run {
                    Glide
                        .with(holder.image)
                        .load("https://images.unsplash.com/photo-1505156868547-9b49f4df4e04")
                        .transform(CenterCrop(), RoundedCorners(BASE_MARGIN))
                        .into(holder.image)
                }
                holder.category.text = item.category.label
            }
        }
    }

    companion object {
        private const val NEW_ITEM = 0
        private const val ITEM = 1
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class NewItem(view: View) : ViewHolder(view) {
            val root: ConstraintLayout = view.root
        }

        class Item(view: View) : ViewHolder(view) {
            val root: FrameLayout = view.keyGearItemRoot
            val image: ImageView = view.itemPhoto
            val category: TextView = view.itemCategory
        }
    }
}
