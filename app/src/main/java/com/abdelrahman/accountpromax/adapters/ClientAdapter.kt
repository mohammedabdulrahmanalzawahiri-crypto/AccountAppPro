package com.abdelrahman.accountpromax.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.abdelrahman.accountpromax.R
import com.abdelrahman.accountpromax.databinding.ItemClientBinding
import com.abdelrahman.accountpromax.models.ClientBalanceUi
import com.abdelrahman.accountpromax.utils.UiStyleManager

class ClientAdapter(
    private val onClick: (ClientBalanceUi) -> Unit,
    private val onLongClick: (ClientBalanceUi) -> Unit
) : ListAdapter<ClientBalanceUi, ClientAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<ClientBalanceUi>() {
        override fun areItemsTheSame(oldItem: ClientBalanceUi, newItem: ClientBalanceUi) =
            oldItem.clientId == newItem.clientId
        override fun areContentsTheSame(oldItem: ClientBalanceUi, newItem: ClientBalanceUi) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemClientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b, onClick, onLongClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class VH(
        private val b: ItemClientBinding,
        private val onClick: (ClientBalanceUi) -> Unit,
        private val onLongClick: (ClientBalanceUi) -> Unit
    ) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: ClientBalanceUi) {
            val tf = UiStyleManager.typeface(b.root.context)
            b.clientName.typeface = tf
            b.clientBalance.typeface = tf
            b.clientName.text = item.clientName
            b.clientBalance.text = String.format("%.2f", item.balance)
            b.clientBalance.setTextColor(
                ContextCompat.getColor(
                    b.root.context,
                    if (item.balance >= 0) R.color.leh_green else R.color.aleh_red
                )
            )
            b.root.setOnClickListener { onClick(item) }
            b.root.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }
}
