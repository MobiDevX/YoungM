package com.example.youngm.Interface

import android.view.View

interface OnItemClickListener {
    fun onItemClicked(view: View?, pos: Int, musicPosition: Int)
}