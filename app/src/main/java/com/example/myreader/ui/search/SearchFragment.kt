package com.example.myreader.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myreader.databinding.FragmentSearchBinding
import com.example.myreader.logic.ClickListener
import com.example.myreader.logic.database.Book
import com.example.myreader.ui.MainViewModel
import com.example.myreader.ui.bookinfo.BookInfoActivity

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding ?= null
    private val viewModel by lazy {
        ViewModelProvider(this).get(SearchViewModel::class.java)
    }
    private val activityViewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: SearchAdapter

    private val readerReqCode = 1

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)

        val searchBookEdit = binding.searchBookEdit
        val recyclerView = binding.recyclerView
        val searchBtn = binding.searchBtn
        val searchLoad = binding.searchLoad

        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        adapter = SearchAdapter(this,viewModel.bookList,
            object : ClickListener {
                override fun onClick(book: Book) {
                    val intent = Intent(activity, BookInfoActivity::class.java)
                    intent.putExtra("from", "search")
                    intent.putExtra("book", book)
                    startActivityForResult(intent, readerReqCode)
                }
                override fun onLongClick(book: Book) {}
            })
        recyclerView.adapter = adapter

        searchBtn.setOnClickListener {
            recyclerView.visibility = View.GONE
            val content = searchBookEdit.text.toString()
            if (content.isNotEmpty()) {
                searchLoad.visibility = View.VISIBLE
                viewModel.searchBook(content)
            } else {
                viewModel.bookList.clear()
                adapter.notifyDataSetChanged()
            }
        }

        viewModel.bookLiveData.observe(viewLifecycleOwner, {result ->
            searchLoad.visibility = View.GONE
            val books = result.getOrNull()
            if (books != null) {
                viewModel.bookList.clear()
                viewModel.bookList.addAll(books)
                recyclerView.visibility = View.VISIBLE
                adapter.notifyDataSetChanged()
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            readerReqCode -> {
                if (data != null) {
                    activityViewModel.updateByIntent(data)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}