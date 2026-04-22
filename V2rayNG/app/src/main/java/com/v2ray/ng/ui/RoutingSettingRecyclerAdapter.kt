package com.v2ray.ng.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.v2ray.ng.R
import com.v2ray.ng.contracts.BaseAdapterListener
import com.v2ray.ng.databinding.ItemRecyclerRoutingSettingBinding
import com.v2ray.ng.helper.ItemTouchHelperAdapter
import com.v2ray.ng.helper.ItemTouchHelperViewHolder
import com.v2ray.ng.viewmodel.RoutingSettingsViewModel

class RoutingSettingRecyclerAdapter(
    private val viewModel: RoutingSettingsViewModel,
    private val adapterListener: BaseAdapterListener?
) : RecyclerView.Adapter<RoutingSettingRecyclerAdapter.MainViewHolder>(),
    ItemTouchHelperAdapter {

    override fun getItemCount() = viewModel.getAll().size

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val rulesets = viewModel.getAll()
        val ruleset = rulesets[position]

        holder.itemRoutingSettingBinding.remarks.text = ruleset.remarks
        holder.itemRoutingSettingBinding.domainIp.text = when {
            !ruleset.domain.isNullOrEmpty() -> ruleset.domain?.joinToString(", ")
            !ruleset.ip.isNullOrEmpty() -> ruleset.ip?.joinToString(", ")
            !ruleset.process.isNullOrEmpty() -> ruleset.process?.joinToString(", ")
            !ruleset.protocol.isNullOrEmpty() -> ruleset.protocol?.joinToString(", ")
            !ruleset.port.isNullOrBlank() -> ruleset.port
            else -> ""
        }
        holder.itemRoutingSettingBinding.outboundTag.text = ruleset.outboundTag
        val disableOnWifiSummary = ruleset.disableOnWifiSsids?.joinToString(", ")
        holder.itemRoutingSettingBinding.disableOnWifi.isVisible = !disableOnWifiSummary.isNullOrBlank()
        holder.itemRoutingSettingBinding.disableOnWifi.text = holder.itemView.context.getString(
            R.string.routing_settings_disable_on_wifi_summary,
            disableOnWifiSummary.orEmpty()
        )
        holder.itemRoutingSettingBinding.chkEnable.setOnCheckedChangeListener(null)
        holder.itemRoutingSettingBinding.chkEnable.isChecked = ruleset.enabled
        holder.itemRoutingSettingBinding.imgLocked.isVisible = ruleset.locked == true
        holder.itemView.setBackgroundColor(Color.TRANSPARENT)

        holder.itemRoutingSettingBinding.layoutEdit.setOnClickListener {
            adapterListener?.onEdit("", position)
        }

        holder.itemRoutingSettingBinding.chkEnable.setOnCheckedChangeListener { it, isChecked ->
            if (!it.isPressed) return@setOnCheckedChangeListener
            ruleset.enabled = isChecked
            viewModel.update(position, ruleset)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            ItemRecyclerRoutingSettingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    class MainViewHolder(val itemRoutingSettingBinding: ItemRecyclerRoutingSettingBinding) :
        BaseViewHolder(itemRoutingSettingBinding.root), ItemTouchHelperViewHolder

    open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        fun onItemClear() {
            itemView.setBackgroundColor(0)
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        viewModel.swap(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemMoveCompleted() {
        adapterListener?.onRefreshData()
    }

    override fun onItemDismiss(position: Int) {
    }
}
