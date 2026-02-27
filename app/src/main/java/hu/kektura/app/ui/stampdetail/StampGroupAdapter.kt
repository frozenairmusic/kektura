package hu.kektura.app.ui.stampdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.kektura.app.R
import hu.kektura.app.databinding.ItemStampGroupBinding
import hu.kektura.app.databinding.ItemStampPointBinding

class StampGroupAdapter(
    private val onToggle: (String) -> Unit,
    private val onCollect: (String) -> Unit,
    private val onRemove: (String) -> Unit
) : ListAdapter<StampListItem, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val VT_HEADER = 0
        private const val VT_STAMP  = 1

        val DIFF = object : DiffUtil.ItemCallback<StampListItem>() {
            override fun areItemsTheSame(a: StampListItem, b: StampListItem) = when {
                a is StampListItem.GroupHeader && b is StampListItem.GroupHeader ->
                    a.group.groupKey == b.group.groupKey
                a is StampListItem.StampEntry  && b is StampListItem.StampEntry  ->
                    a.item.point.id == b.item.point.id
                else -> false
            }
            override fun areContentsTheSame(a: StampListItem, b: StampListItem) = a == b
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is StampListItem.GroupHeader -> VT_HEADER
        is StampListItem.StampEntry  -> VT_STAMP
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VT_HEADER -> GroupViewHolder(ItemStampGroupBinding.inflate(inflater, parent, false))
            else      -> StampViewHolder(ItemStampPointBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is StampListItem.GroupHeader -> (holder as GroupViewHolder).bind(item)
            is StampListItem.StampEntry  -> (holder as StampViewHolder).bind(item)
        }
    }

    // ── Group header view holder ──────────────────────────────────────────────

    inner class GroupViewHolder(private val b: ItemStampGroupBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(header: StampListItem.GroupHeader) {
            val group = header.group

            b.tvGroupKey.text = group.groupKey

            if (group.groupName.isNotBlank()) {
                b.tvGroupName.text       = group.groupName
                b.tvGroupName.visibility = View.VISIBLE
            } else {
                b.tvGroupName.visibility = View.GONE
            }

            // Collect / un-collect button
            b.btnCollectGroup.setImageResource(
                if (group.allCollected) R.drawable.ic_stamp_collected
                else R.drawable.ic_stamp_pending
            )
            b.btnCollectGroup.setOnClickListener {
                if (group.allCollected) onRemove(group.groupKey) else onCollect(group.groupKey)
            }

            // Chevron rotation: 180° = expanded, 0° = collapsed
            b.btnExpand.rotation = if (group.expanded) 180f else 0f
            b.btnExpand.setOnClickListener { onToggle(group.groupKey) }
            b.root.setOnClickListener { onToggle(group.groupKey) }
        }
    }

    // ── Individual stamp view holder ──────────────────────────────────────────

    inner class StampViewHolder(private val b: ItemStampPointBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(entry: StampListItem.StampEntry) {
            val pt = entry.item.point

            b.tvName.text = pt.name

            // Show sub-code only when it differs from the group key
            b.tvCode.text = if (pt.stampCode.isNotBlank() && pt.stampCode != entry.groupKey)
                pt.stampCode.removePrefix("${entry.groupKey}_")
            else ""

            if (pt.elevation > 0.0) {
                b.tvElevation.text       = "%.0f m".format(pt.elevation)
                b.tvElevation.visibility = View.VISIBLE
            } else {
                b.tvElevation.visibility = View.GONE
            }

            if (pt.notes.isNotBlank()) {
                b.tvDesc.text       = pt.notes
                b.tvDesc.visibility = View.VISIBLE
            } else {
                b.tvDesc.visibility = View.GONE
            }

            b.ivStatus.setImageResource(
                if (entry.item.collected) R.drawable.ic_stamp_collected
                else R.drawable.ic_stamp_pending
            )

            b.root.isClickable = false
        }
    }
}
