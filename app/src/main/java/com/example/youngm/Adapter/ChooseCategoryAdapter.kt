package com.example.youngm.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.youngm.R
import kotlinx.android.synthetic.main.choose_category_item.view.*
import kotlinx.coroutines.processNextEventInCurrentThread

class ChooseCategoryAdapter: RecyclerView.Adapter<ChooseCategoryAdapter.ChooseCategoryViewHolder>(){

    var categories: ArrayList<String> = ArrayList()
    var selectedCategory: ArrayList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseCategoryViewHolder =
        ChooseCategoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.choose_category_item, parent, false))

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: ChooseCategoryViewHolder, position: Int) {
        holder.bind(categories[position])
        val checkBox = holder.linearLayout.checkBoxChooseCategroy
        val category = holder.linearLayout.textViewChooseCategory.text.toString()
        checkBox.setOnClickListener {

            if (checkBox.isChecked){
                selectedCategory.add(category)
            }else{
                selectedCategory.remove(category)
            }
        }
    }

    class ChooseCategoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        lateinit var linearLayout: LinearLayout
        fun bind(item: String) = with(itemView){
            textViewChooseCategory.text = item
            linearLayout = itemView.linearLayoutChooseCategory
        }
    }

    fun refreshData(categories: ArrayList<String>){
        this.categories = categories
        notifyDataSetChanged()
    }

}