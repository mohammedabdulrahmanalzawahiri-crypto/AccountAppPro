package com.abdelrahman.accountpromax.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.GradientDrawable
import com.abdelrahman.accountpromax.R
import com.abdelrahman.accountpromax.databinding.ItemTransactionBinding
import com.abdelrahman.accountpromax.models.TransactionTimelineItem
import com.abdelrahman.accountpromax.utils.UiStyleManager

class TransactionAdapter(
    private val onLongClick: (com.abdelrahman.accountpromax.models.TransactionEntity) -> Unit
) : ListAdapter<TransactionTimelineItem, TransactionAdapter.VH>(Diff) {
    object Diff : DiffUtil.ItemCallback<TransactionTimelineItem>() {
        override fun areItemsTheSame(oldItem: TransactionTimelineItem, newItem: TransactionTimelineItem) = oldItem.tx.id == newItem.tx.id
        override fun areContentsTheSame(oldItem: TransactionTimelineItem, newItem: TransactionTimelineItem) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b, onLongClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class VH(
        private val b: ItemTransactionBinding,
        private val onLongClick: (com.abdelrahman.accountpromax.models.TransactionEntity) -> Unit
    ) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: TransactionTimelineItem) {
            val tx = item.tx
            val tf = UiStyleManager.typeface(b.root.context)
            b.mainText.typeface = tf
            b.subText.typeface = tf
            val sign = if (tx.type == "leh") "+" else "-"
            val isLeh = tx.type == "leh"
            b.typeBadge.text = if (isLeh) "له" else "عليه"
            b.mainText.text = "${tx.date} | $sign ${tx.amount}"
            b.subText.text = "${tx.desc} | الرصيد التراكمي: ${"%.2f".format(item.cumulativeBalance)}"
            b.mainText.setTextColor(
                b.root.context.getColor(if (isLeh) R.color.leh_green else R.color.aleh_red)
            )
            b.typeBadge.setTextColor(
                b.root.context.getColor(if (isLeh) R.color.leh_green else R.color.aleh_red)
            )
            b.typeBadge.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(if (isLeh) 0x1A1B8F3A else 0x1AC62928)
            }
            b.root.alpha = 0f
            b.root.animate().alpha(1f).setDuration(180).start()
            b.root.setOnLongClickListener {
                onLongClick(tx)
                true
            }
        }
    }
}
