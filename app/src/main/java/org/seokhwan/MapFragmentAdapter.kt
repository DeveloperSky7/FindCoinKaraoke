package org.seokhwan

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.seokhwan.findcoinsingroom.MapFragment
import org.seokhwan.findcoinsingroom.databinding.FragmentMapRecyclerBinding
import org.seokhwan.findcoinsingroom.singRoomList

class MapFragmentAdapter:RecyclerView.Adapter<Holder>() {
    var list = listOf<singRoomList>()

    val TAG = "FindCoinSingRoom"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = FragmentMapRecyclerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val place = list.get(position)
        holder.setItem(place)

        holder.binding.root.setOnClickListener {
            itemClickListener.onClick(it, position) // 클릭 인터페이스용
        }
    }

    interface OnItemClickListener{ // 클릭 인터페이스용
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener:OnItemClickListener){ // 클릭 인터페이스용
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener: OnItemClickListener // 클릭 인터페이스용

    override fun getItemCount(): Int {
        return list.size
    }


}

class Holder(val binding:FragmentMapRecyclerBinding) : RecyclerView.ViewHolder(binding.root){
    fun setItem(singRoomList: singRoomList){
        binding.mainfragmentRecyclerName.text = singRoomList.placeName
        binding.mainfragmentRecyclerAddress.text = "- ${singRoomList.roodAddress}"
        binding.mainfragmentRecyclerDistance.text = "${singRoomList.distance}km"
    }


}