package org.seokhwan.findcoinsingroom

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import org.seokhwan.findcoinsingroom.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    lateinit var binding: FragmentMainBinding
    val TAG = "FindCoinSingRoom"

    val mapFragment = MapFragment()

    lateinit var bannerAd : AdView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(layoutInflater,container,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "MainFragment onViewCreated")

        MobileAds.initialize(context){} //배너 광고 로드

        bannerAd = view.findViewById(R.id.mainfragment_banner)
        val adRequst = AdRequest.Builder().build()
        bannerAd.loadAd(adRequst)

        binding.run {
            mainfragmentMap.setOnClickListener {
//                val transaction = childFragmentManager.beginTransaction()
//                transaction.replace(R.id.mapFragment_mainlayout,mapFragment)
//                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                transaction.commit()
                Toast.makeText(context,"다음 업데이트 구현 예정입니다..\n 아래 탭을 눌러 이동해주세요",Toast.LENGTH_SHORT).show()

            }
            mainfragmentSearch.setOnClickListener {
                Toast.makeText(context,"다음 업데이트 구현 예정입니다..\n 아래 탭을 눌러 이동해주세요",Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onResume() {
        super.onResume()

    }


}