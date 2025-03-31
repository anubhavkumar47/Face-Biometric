package com.facebiometric.app.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.facebiometric.app.R
import com.facebiometric.app.model.Attendance

 class DateAdapter(private val dates :MutableList<Attendance>):RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

     private val expandedStates = mutableMapOf<Int, Boolean>() // Store expansion states


     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
         val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attendance_sheet,parent,false)
         return DateViewHolder(view)
     }



     override fun getItemCount(): Int {
        return dates.size
     }



     override fun onBindViewHolder(holder: DateViewHolder, position: Int) {

         val date = dates[position]
         holder.date.text = date.date
         holder.checkedIn.text = date.checkedIn
         holder.checkedOut.text = date.checkedOut
         holder.status.text = date.status

         if(date.status =="Late"){
             holder.status.setTextColor(Color.RED)
         }
         else{
             holder.status.setTextColor(Color.GREEN)
         }

         val isExpanded = expandedStates[position] ?: false

         holder.expandAbleLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
         holder.expandMore.setImageResource(if (isExpanded) R.drawable.expand_less else R.drawable.expand_more)

         holder.expandMore.setOnClickListener {
             expandedStates[position] = !isExpanded // Toggle state
             notifyItemChanged(position)
         }

     }


     class DateViewHolder(itemView:View):ViewHolder(itemView) {

         val date: TextView = itemView.findViewById(R.id.date)
         val checkedIn: TextView = itemView.findViewById(R.id.checkedIn)
         val checkedOut: TextView = itemView.findViewById(R.id.checkedOut)
         val status: TextView = itemView.findViewById(R.id.status)
         val dayImage:ImageView = itemView.findViewById(R.id.dayImage)
         val expandMore:ImageView = itemView.findViewById(R.id.expandMore)
         val expandAbleLayout: LinearLayout = itemView.findViewById(R.id.expandAbleLayout)


     }
 }