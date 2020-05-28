package com.huawei.hms.ads6

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.huawei.hms.ads.R
import kotlinx.android.synthetic.main.text_view.view.*

class RvAdapter(private val dateset: List<AdFormat>, private val activity: AppCompatActivity) :
        RecyclerView.Adapter<RvAdapter.RvDataHolder>() {

    class RvDataHolder(val textView: LinearLayout) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvDataHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
                .inflate(R.layout.text_view, parent, false) as LinearLayout
        return RvDataHolder(textView)
    }

    override fun getItemCount(): Int = dateset.size


    override fun onBindViewHolder(holder: RvDataHolder, position: Int) {
        holder.itemView.text.text = dateset[position].title
        holder.itemView.setOnClickListener {
            activity.startActivity(Intent(activity, dateset[position].targetClass))
        }
    }
}