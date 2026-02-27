package hu.kektura.app.ui.stamps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.kektura.app.databinding.ItemSegmentBinding

class SegmentRowAdapter(
    private val onClick: (SegmentRow) -> Unit
) : ListAdapter<SegmentRow, SegmentRowAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemSegmentBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(row: SegmentRow) {
            b.tvSegmentCode.text   = "OKT-%02d".format(row.segment.id)
            b.tvSegmentName.text   = row.segment.name
            b.tvSegmentRegion.text = row.segment.region

            if (row.stampCount > 0) {
                b.tvStampCount.text      = "${row.collectedCount} / ${row.stampCount}"
                b.stampProgress.max      = row.stampCount
                b.stampProgress.progress = row.collectedCount
                b.stampProgress.visibility = View.VISIBLE
            } else {
                b.tvStampCount.text        = "Letöltés folyamatban…"
                b.stampProgress.visibility = View.GONE
            }

            b.root.setOnClickListener { onClick(row) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemSegmentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<SegmentRow>() {
            override fun areItemsTheSame(a: SegmentRow, b: SegmentRow) =
                a.segment.id == b.segment.id
            override fun areContentsTheSame(a: SegmentRow, b: SegmentRow) = a == b
        }
    }
}
