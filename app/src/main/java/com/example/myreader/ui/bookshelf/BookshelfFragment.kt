package com.example.myreader.ui.bookshelf

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myreader.databinding.FragmentBookshelfBinding
import com.example.myreader.logic.ClickListener
import com.example.myreader.logic.database.Book
import com.example.myreader.ui.MainViewModel
import com.example.myreader.ui.bookinfo.BookInfoActivity
import com.example.myreader.ui.reader.ReaderActivity

class BookshelfFragment : Fragment() {

    private var _binding: FragmentBookshelfBinding? = null
    private val infoReqCode = 1

    private val binding get() = _binding!!

    private val activityViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBookshelfBinding
            .inflate(inflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val recyclerView = binding.bookshelfRecyclerView

        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        val adapter = BookshelfAdapter(this, activityViewModel.bookList,
            object : ClickListener {
                override fun onClick(book: Book) {
                    val intent = Intent(activity, BookInfoActivity::class.java)
                    intent.putExtra("from", "shelf")
                    intent.putExtra("book", book)
                    startActivityForResult(intent, infoReqCode)
                }
                override fun onLongClick(book: Book) {}
            })
        recyclerView.adapter = adapter

        activityViewModel.books.observe(viewLifecycleOwner, { result ->
            adapter.notifyDataSetChanged()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            infoReqCode -> {
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