package org.seokhwan.findcoinsingroom

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import org.seokhwan.findcoinsingroom.databinding.FragmentBookBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

data class BookList(var no:String,var title:String,var singer:String)
class BookFragment : Fragment() {
    private var mInterstitialAd: InterstitialAd? = null

    lateinit var binding : FragmentBookBinding
    val bookAdapter = BookFragmentAdapter()
    val TAG = "FindCoinSingRoom"

    var kyBtnStatus = 0
    var tjBtnStatus = 0
    var findTitleStatus = 0
    var findSingerStatus = 0
    var sortKorean = 0
    var sortSinger = 0

    var searchItems = mutableListOf<BookList>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBookBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "BookFragment onViewCreated")

        var adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context,"ca-app-pub-8351341278971054/9091338728", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError?.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
                Log.d(TAG, "광고로딩 성공")
            }
        })

        kyBtnStatus = 1
        findTitleStatus = 1
        sortKorean = 1

        binding.bookfragmentKyButton.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bookfragment_searchoption_btn_company_select))
        binding.bookfragmentFindTitle.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bookfragment_searchoption_btn_selcet))
        binding.bookfragmentSortkorean.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bookfragment_searchoption_btn_selcet))

        searchKyRecent()

        // 리사이클러뷰 구성
        binding.bookfragmentSonglist.adapter = bookAdapter
        val manager = LinearLayoutManager(context)
        binding.bookfragmentSonglist.layoutManager = manager

        // ↓ 버튼 색상, 이벤트 구현
        binding.run {
            bookfragmentTjButton.setOnClickListener {
                searchTjRecent()
                binding.bookfragmentKyButton.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bookfragment_searchoption_btn_company))
                binding.bookfragmentTjButton.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bookfragment_searchoption_btn_company_select))
                kyBtnStatus = 0
                tjBtnStatus = 1
            }
            bookfragmentKyButton.setOnClickListener {
                searchKyRecent()
                binding.bookfragmentKyButton.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bookfragment_searchoption_btn_company_select))
                binding.bookfragmentTjButton.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bookfragment_searchoption_btn_company))
                kyBtnStatus = 1
                tjBtnStatus = 0
            }
            bookfragmentFindTitle.setOnClickListener {
                binding.bookfragmentFindTitle.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bookfragment_searchoption_btn_selcet))
                binding.bookfragmentFindSinger.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bookfragment_searchoption_btn))
                findTitleStatus = 1
                findSingerStatus = 0
            }
            bookfragmentFindSinger.setOnClickListener {
                binding.bookfragmentFindSinger.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bookfragment_searchoption_btn_selcet))
                binding.bookfragmentFindTitle.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bookfragment_searchoption_btn))
                findSingerStatus = 1
                findTitleStatus = 0
            }
            bookfragmentSortkorean.setOnClickListener {
                binding.bookfragmentSortkorean.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bookfragment_searchoption_btn_selcet))
                binding.bookfragmentSortsinger.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bookfragment_searchoption_btn))
                sortKorean = 1
                sortSinger = 0

                var sortSearchItems = searchItems.sortedBy { it.title } //버튼누르면 제목별 정렬
                bookAdapter.list = sortSearchItems
                bookAdapter.notifyDataSetChanged()
            }
            bookfragmentSortsinger.setOnClickListener {
                binding.bookfragmentSortsinger.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bookfragment_searchoption_btn_selcet))
                binding.bookfragmentSortkorean.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bookfragment_searchoption_btn))
                sortSinger = 1
                sortKorean = 0

                var sortSearchItems = searchItems.sortedBy { it.singer } //버튼누르면 가수별 정렬
                bookAdapter.list = sortSearchItems
                bookAdapter.notifyDataSetChanged()
            }
        }
        // ↑ 버튼 색상, 이벤트 구현

        var randomInt = random(3,6)
        var clickCount = 0

        // 텍스트 입력후 검색 ↓
        binding.bookfragmentFindBtn.setOnEditorActionListener { v, actionId, event ->
            var handled = false

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                var findInfo = binding.bookfragmentFindBtn.text.toString()
                if (kyBtnStatus == 1 && findTitleStatus == 1) {
                    Log.d(TAG, "BookFragment 금영+제목 검색: $findInfo")
                    searchKyTitle(findInfo)
                } else if (kyBtnStatus == 1 && findSingerStatus == 1){
                    Log.d(TAG, "BookFragment 금영+가수 검색: $findInfo")
                    searchKySinger(findInfo)
                } else if (tjBtnStatus == 1 && findTitleStatus == 1){
                    Log.d(TAG, "BookFragment TJ+제목 검색: $findInfo")
                    searchTjTitle(findInfo)
                } else if (tjBtnStatus == 1 && findSingerStatus == 1){
                    Log.d(TAG, "BookFragment TJ+가수 검색: $findInfo")
                    searchTjSinger(findInfo)
                } else {
                    Toast.makeText(context,"검색 조건을 입력해주세요",Toast.LENGTH_SHORT).show()
                }
                clickCount += 1
                Log.d(TAG,"BookFragment clickCount = $clickCount, random = $randomInt")
                if (clickCount == randomInt){
                    showAds()
                    clickCount = 0
                    randomInt = random(3,6)
                }
                handled = true
            }
            handled
        }

    }

    private fun showAds(){
        if (mInterstitialAd != null){
            mInterstitialAd?.show(requireActivity())
            Log.d(TAG, "BookFragment 광고 실행 완료")
        }else {
            Log.d(TAG, "BookFragment 광고가 아직 준비 안되었습니다.")
        }
    }

    private fun random(from: Int, to: Int) : Int { // 랜덤 숫자 추출
        var random = Random()
        return random.nextInt(to - from) + from
    }

    private fun searchKyRecent() { //금영 최신곡 리스트 받아오는 메서드
        Log.d(TAG, "BookFragment searchKyRecent 실행")
        searchItems.clear() // 메서드 실행전 리스트 초기화

        val retrofit = Retrofit.Builder()   // Retrofit 구성
            .baseUrl("https://api.manana.kr")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(SingBookApi::class.java)   // 통신 인터페이스를 객체로 생성
        val call = api.getKyRecent()   // 검색 조건 입력

        // API 서버에 요청
        call.enqueue(object: Callback<SingList> {
            override fun onResponse(
                call: Call<SingList>,
                response: Response<SingList>
            ) {
                for (i in 0 until response.body()!!.size){ //api 데이터 재가공.
                    var no = response.body()!!.get(i).no
                    var title = response.body()!!.get(i).title
                    var singer = response.body()!!.get(i).singer

                    var kyItem = BookList(no,title,singer)

                    searchItems.add(kyItem)
                }
                //리사이클러뷰 리스트 추가, 상태 확인 후 소팅
                if (sortKorean == 1 && sortSinger == 0){
                    bookAdapter.list = searchItems.sortedBy { it.title }
                } else if (sortKorean == 0 && sortSinger == 1){
                    bookAdapter.list = searchItems.sortedBy { it.singer }
                }
                bookAdapter.notifyDataSetChanged()
                Log.d(TAG, "BookFragment RecyclerView 구현 완료")
            }
            override fun onFailure(call: Call<SingList>, t: Throwable) {
                // 통신 실패
                Log.w(TAG, "BookFragment 통신 실패: ${t.message}")
            }
        })
    }

    private fun searchKyTitle(title: String){ //금영 + 제목 검색
        Log.d(TAG, "BookFragment searchKyTitle 실행")
        searchItems.clear() // 메서드 실행전 리스트 초기화

        val retrofit = Retrofit.Builder()   // Retrofit 구성
            .baseUrl("https://api.manana.kr")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(SingBookApi::class.java)   // 통신 인터페이스를 객체로 생성
        val call = api.getKyTitle(title)   // 검색 조건 입력

        // API 서버에 요청
        call.enqueue(object: Callback<SingList> {
            override fun onResponse(
                call: Call<SingList>,
                response: Response<SingList>
            ) {
                for (i in 0 until response.body()!!.size){ //api 데이터 재가공.
                    var no = response.body()!!.get(i).no
                    var title = response.body()!!.get(i).title
                    var singer = response.body()!!.get(i).singer

                    var kyItem = BookList(no,title,singer)

                    searchItems.add(kyItem)
                }
                //리사이클러뷰 리스트 추가, 상태 확인 후 소팅
                if (searchItems.size == 0){ //검색 결과 없으면 빈란 텍스트 출력
                    binding.bookfragmentEmptyText.visibility = View.VISIBLE
                } else {
                    binding.bookfragmentEmptyText.visibility = View.GONE
                }
                if (sortKorean == 1 && sortSinger == 0){
                    bookAdapter.list = searchItems.sortedBy { it.title }
                } else if (sortKorean == 0 && sortSinger == 1){
                    bookAdapter.list = searchItems.sortedBy { it.singer }
                }
                bookAdapter.notifyDataSetChanged()
                Log.d(TAG, "BookFragment RecyclerView 구현 완료")
            }
            override fun onFailure(call: Call<SingList>, t: Throwable) {
                // 통신 실패
                Log.w(TAG, "BookFragment 통신 실패: ${t.message}")
            }
        })


    }

    private fun searchKySinger(singer: String){ //금영 + 가수 검색
        Log.d(TAG, "BookFragment searchKySinger 실행")
        searchItems.clear() // 메서드 실행전 리스트 초기화

        val retrofit = Retrofit.Builder()   // Retrofit 구성
            .baseUrl("https://api.manana.kr")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(SingBookApi::class.java)   // 통신 인터페이스를 객체로 생성
        val call = api.getKySinger(singer)   // 검색 조건 입력

        // API 서버에 요청
        call.enqueue(object: Callback<SingList> {
            override fun onResponse(
                call: Call<SingList>,
                response: Response<SingList>
            ) {
                for (i in 0 until response.body()!!.size){ //api 데이터 재가공.
                    var no = response.body()!!.get(i).no
                    var title = response.body()!!.get(i).title
                    var singer = response.body()!!.get(i).singer

                    var kyItem = BookList(no,title,singer)

                    searchItems.add(kyItem)
                }
                //리사이클러뷰 리스트 추가, 상태 확인 후 소팅
                if (searchItems.size == 0){ //검색 결과 없으면 빈란 텍스트 출력
                    binding.bookfragmentEmptyText.visibility = View.VISIBLE
                } else {
                    binding.bookfragmentEmptyText.visibility = View.GONE
                }
                if (sortKorean == 1 && sortSinger == 0){
                    bookAdapter.list = searchItems.sortedBy { it.title }
                } else if (sortKorean == 0 && sortSinger == 1){
                    bookAdapter.list = searchItems.sortedBy { it.singer }
                }
                bookAdapter.notifyDataSetChanged()
                Log.d(TAG, "BookFragment RecyclerView 구현 완료")
            }
            override fun onFailure(call: Call<SingList>, t: Throwable) {
                // 통신 실패
                Log.w(TAG, "BookFragment 통신 실패: ${t.message}")
            }
        })
    }

    private fun searchTjRecent() { //tj 최신곡 리스트 받아오는 메서드
        Log.d(TAG, "BookFragment searchTjRecent 실행")
        searchItems.clear() // 메서드 실행전 리스트 초기화

        val retrofit = Retrofit.Builder()   // Retrofit 구성
            .baseUrl("https://api.manana.kr")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(SingBookApi::class.java)   // 통신 인터페이스를 객체로 생성
        val call = api.getTjRecent()   // 검색 조건 입력

        // API 서버에 요청
        call.enqueue(object: Callback<SingList> {
            override fun onResponse(
                call: Call<SingList>,
                response: Response<SingList>
            ) {
                for (i in 0 until response.body()!!.size){ //api 데이터 재가공.
                    var no = response.body()!!.get(i).no
                    var title = response.body()!!.get(i).title
                    var singer = response.body()!!.get(i).singer

                    var tjItem = BookList(no,title,singer)

                    searchItems.add(tjItem)
                }
                //리사이클러뷰 리스트 추가, 상태 확인 후 소팅
                if (sortKorean == 1 && sortSinger == 0){
                    bookAdapter.list = searchItems.sortedBy { it.title }
                } else if (sortKorean == 0 && sortSinger == 1){
                    bookAdapter.list = searchItems.sortedBy { it.singer }
                }
                bookAdapter.notifyDataSetChanged()
                Log.d(TAG, "BookFragment RecyclerView 구현 완료")
            }
            override fun onFailure(call: Call<SingList>, t: Throwable) {
                // 통신 실패
                Log.w(TAG, "BookFragment 통신 실패: ${t.message}")
            }
        })

    }

    private fun searchTjTitle(title:String){
        Log.d(TAG, "BookFragment searchTjTitle 실행")
        searchItems.clear() // 메서드 실행전 리스트 초기화

        val retrofit = Retrofit.Builder()   // Retrofit 구성
            .baseUrl("https://api.manana.kr")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(SingBookApi::class.java)   // 통신 인터페이스를 객체로 생성
        val call = api.getTjTitle(title)   // 검색 조건 입력

        // API 서버에 요청
        call.enqueue(object: Callback<SingList> {
            override fun onResponse(
                call: Call<SingList>,
                response: Response<SingList>
            ) {
                for (i in 0 until response.body()!!.size){ //api 데이터 재가공.
                    var no = response.body()!!.get(i).no
                    var title = response.body()!!.get(i).title
                    var singer = response.body()!!.get(i).singer

                    var tjItem = BookList(no,title,singer)

                    searchItems.add(tjItem)
                }
                //리사이클러뷰 리스트 추가, 상태 확인 후 소팅
                if (searchItems.size == 0){ //검색 결과 없으면 빈란 텍스트 출력
                    binding.bookfragmentEmptyText.visibility = View.VISIBLE
                } else {
                    binding.bookfragmentEmptyText.visibility = View.GONE
                }
                if (sortKorean == 1 && sortSinger == 0){
                    bookAdapter.list = searchItems.sortedBy { it.title }
                } else if (sortKorean == 0 && sortSinger == 1){
                    bookAdapter.list = searchItems.sortedBy { it.singer }
                }
                bookAdapter.notifyDataSetChanged()
                Log.d(TAG, "BookFragment RecyclerView 구현 완료")
            }
            override fun onFailure(call: Call<SingList>, t: Throwable) {
                // 통신 실패
                Log.w(TAG, "BookFragment 통신 실패: ${t.message}")
            }
        })
    }

    private fun searchTjSinger(singer:String){
        Log.d(TAG, "BookFragment searchTjSinger 실행")
        searchItems.clear() // 메서드 실행전 리스트 초기화

        val retrofit = Retrofit.Builder()   // Retrofit 구성
            .baseUrl("https://api.manana.kr")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(SingBookApi::class.java)   // 통신 인터페이스를 객체로 생성
        val call = api.getTjSinger(singer)   // 검색 조건 입력

        // API 서버에 요청
        call.enqueue(object: Callback<SingList> {
            override fun onResponse(
                call: Call<SingList>,
                response: Response<SingList>
            ) {
                for (i in 0 until response.body()!!.size){ //api 데이터 재가공.
                    var no = response.body()!!.get(i).no
                    var title = response.body()!!.get(i).title
                    var singer = response.body()!!.get(i).singer

                    var tjItem = BookList(no,title,singer)

                    searchItems.add(tjItem)
                }
                //리사이클러뷰 리스트 추가, 상태 확인 후 소팅
                if (searchItems.size == 0){ //검색 결과 없으면 빈란 텍스트 출력
                    binding.bookfragmentEmptyText.visibility = View.VISIBLE
                } else {
                    binding.bookfragmentEmptyText.visibility = View.GONE
                }
                if (sortKorean == 1 && sortSinger == 0){
                    bookAdapter.list = searchItems.sortedBy { it.title }
                } else if (sortKorean == 0 && sortSinger == 1){
                    bookAdapter.list = searchItems.sortedBy { it.singer }
                }
                bookAdapter.notifyDataSetChanged()
                Log.d(TAG, "BookFragment RecyclerView 구현 완료")
            }
            override fun onFailure(call: Call<SingList>, t: Throwable) {
                // 통신 실패
                Log.w(TAG, "BookFragment 통신 실패: ${t.message}")
            }
        })

    }




}


