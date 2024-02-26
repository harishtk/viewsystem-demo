package com.example.viewsystem.feature.listmotions.util

import androidx.recyclerview.widget.RecyclerView.ViewHolder

interface OnItemDragListener {
    fun onDragStart(viewHolder: ViewHolder): Boolean
}