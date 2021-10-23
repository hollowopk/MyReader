package com.example.myreader.ui.reader

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.ScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.example.myreader.R
import com.example.myreader.databinding.ActivityReaderBinding
import com.example.myreader.databinding.ReadPage1Binding
import com.example.myreader.databinding.ReadPage2Binding
import com.example.myreader.databinding.ReadPage3Binding
import com.example.myreader.logic.Repository
import com.example.myreader.logic.network.MyReaderNetwork
import com.example.myreader.logic.utils.showToast
import kotlinx.coroutines.CoroutineScope
import java.io.File
import kotlin.concurrent.thread

class ReaderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReaderBinding
    private lateinit var binding1: ReadPage1Binding
    private lateinit var binding2: ReadPage2Binding
    private lateinit var binding3: ReadPage3Binding
    private lateinit var viewModel: ReaderViewModel
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBindings()
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(ReaderViewModel::class.java)
        supportActionBar?.hide()

        val urlList = intent.getStringArrayListExtra("urlList")
        val file = intent.getSerializableExtra("file")

        if (file != null) {
            viewModel.initWithFile(file as File)
        }
        if (urlList != null) {
            viewModel.initWithList(urlList)
        }

        setObserver()

        viewPager = binding.readerViewPager
        val views = ArrayList<View>()
        views.addAll(listOf(binding1.root, binding2.root, binding3.root))
        viewPager.adapter = ReaderAdapter(views)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    ViewPager.SCROLL_STATE_IDLE -> {
                        val curPos = viewPager.currentItem
                        if (curPos == 0) {
                            moveToPre()
                        } else if (curPos == 2) {
                            moveToNext()
                        }
                    }
                }
            }

        })

    }

    private fun initBindings() {
        binding = ActivityReaderBinding.inflate(layoutInflater)
        binding1 = ReadPage1Binding.inflate(layoutInflater)
        binding2 = ReadPage2Binding.inflate(layoutInflater)
        binding3 = ReadPage3Binding.inflate(layoutInflater)
    }

    private fun setObserver() {
        viewModel.preArticle.observe(this, { res ->
            if (res != null) {
                binding1.page1Load.visibility = View.GONE
                binding1.readerArticle1.visibility = View.VISIBLE
                binding1.readerArticle1.text = res
            }
        })

        viewModel.curArticle.observe(this, { res ->
            if (res != null) {
                binding2.page2Load.visibility = View.GONE
                binding2.readerArticle2.visibility = View.VISIBLE
                binding2.readerArticle2.text = res
            }
        })

        viewModel.nxtArticle.observe(this, { res ->
            if (res != null) {
                binding3.page3Load.visibility = View.GONE
                binding3.readerArticle3.visibility = View.VISIBLE
                binding3.readerArticle3.text = res
            }
        })
    }

    private fun moveToPre() {
        if (binding1.readerArticle1.text.toString() == "") {
            "翻页太快了，还没有加载完".showToast(this)
            viewPager.setCurrentItem(1, false)
        } else {
            if (viewModel.curChapter >= 1) {
                viewModel.getPre()
                binding2.page2Load.visibility = binding1.page1Load.visibility
                binding1.page1Load.visibility = View.VISIBLE
                binding2.readerScroll2.scrollTo(0, 0)
                viewPager.setCurrentItem(1, false)
                binding1.page1Load.visibility = View.VISIBLE
                binding1.readerArticle1.text = ""
                viewModel.getArticle(
                    viewModel.curChapter - 1,
                    0
                )
            }
        }
    }

    private fun moveToNext() {
        if (binding3.readerArticle3.text.toString() == "") {
            "翻页太快了，还没有加载完".showToast(this)
            viewPager.setCurrentItem(1, false)
        } else {
            viewModel.getNext()
            binding2.page2Load.visibility = binding3.page3Load.visibility
            binding3.page3Load.visibility = View.VISIBLE
            binding2.readerScroll2.scrollTo(0, 0)
            viewPager.setCurrentItem(1, false)
            binding3.page3Load.visibility = View.VISIBLE
            binding3.readerArticle3.text = ""
            viewModel.getArticle(viewModel.curChapter + 3, 2)
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("lastReadChapter", viewModel.titles[viewPager.currentItem])
        setResult(RESULT_OK, intent)
        finish()
    }

}