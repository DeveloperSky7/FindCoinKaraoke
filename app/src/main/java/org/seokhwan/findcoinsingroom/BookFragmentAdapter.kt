package org.seokhwan.findcoinsingroom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.seokhwan.findcoinsingroom.databinding.FragmentBookRecyclerBinding

class BookFragmentAdapter:RecyclerView.Adapter<BookHolder>() {
    var list = listOf<BookList>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookHolder {
        val binding = FragmentBookRecyclerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return BookHolder(binding)

    }

    override fun onBindViewHolder(holder: BookHolder, position: Int) {
        val items = list.get(position)
        holder.setList(items)

    }

    override fun getItemCount(): Int {
        return list.size
    }

}

class BookHolder(val binding:FragmentBookRecyclerBinding):RecyclerView.ViewHolder(binding.root){
    fun setList(booklist:BookList){
        binding.bookfragmentRecyclerNumber.text = booklist.no
        binding.bookfragmentRecyclerSing.text = booklist.title
        binding.bookfragmentRecyclerSinger.text = booklist.singer

    }
}