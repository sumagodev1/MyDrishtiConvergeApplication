package com.mydrishti.co.`in`.activities.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.cardview.widget.CardView
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.models.Device
import com.mydrishti.co.`in`.activities.models.Site

class SiteAdapter(
    private val onSiteClickListener: (Device) -> Unit
) : RecyclerView.Adapter<SiteAdapter.SiteViewHolder>() {

    private val sites = mutableListOf<Device>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_site, parent, false)
        return SiteViewHolder(view)
    }

    override fun onBindViewHolder(holder: SiteViewHolder, position: Int) {
        val site = sites[position]
        holder.bind(site)
    }

    override fun getItemCount(): Int = sites.size

    fun updateSites(newSites: List<Device>) {
        val diffCallback = SiteDiffCallback(sites, newSites)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        sites.clear()
        sites.addAll(newSites)
        
        diffResult.dispatchUpdatesTo(this)
    }

    inner class SiteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val siteNameTextView: TextView = itemView.findViewById(R.id.siteName)
        private val siteLocationTextView: TextView = itemView.findViewById(R.id.siteLocation)
        private val siteCard: CardView = itemView.findViewById(R.id.siteCard)

        fun bind(site: Device) {
            siteNameTextView.text = site.deviceDisplayName
            siteLocationTextView.text = site.deviceName
            
            siteCard.setOnClickListener {
                onSiteClickListener(site)
            }
        }
    }
}

class SiteDiffCallback(
    private val oldList: List<Device>,
    private val newList: List<Device>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].iotDeviceMapId == newList[newItemPosition].iotDeviceMapId
    }
    
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}