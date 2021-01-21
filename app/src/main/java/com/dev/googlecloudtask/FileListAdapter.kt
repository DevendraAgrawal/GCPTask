package com.dev.googlecloudtask

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FileListAdapter(var data: ArrayList<Uri>) : RecyclerView.Adapter<FileListAdapter.FileLiseViewHolder>(){

    class FileLiseViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var fileName = view.findViewById<TextView>(R.id.fileName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileLiseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_adapter, parent, false)
        return FileLiseViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileLiseViewHolder, position: Int) {
        holder.fileName.text = data[position].lastPathSegment
    }

    override fun getItemCount(): Int {
        return data.size
    }
}