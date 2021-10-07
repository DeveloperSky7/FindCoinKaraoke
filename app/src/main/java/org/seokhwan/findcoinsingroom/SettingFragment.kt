package org.seokhwan.findcoinsingroom

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import org.seokhwan.findcoinsingroom.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {

    lateinit var binding : FragmentSettingBinding
    val TAG = "FindCoinSingRoom"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "SettingFragment onViewCreated")

        binding.settingfragmentEventBtn.setOnClickListener {
            Toast.makeText(context,"진행중인 이벤트가 없습니다.",Toast.LENGTH_SHORT).show()
        }

        binding.settingfragmentVersionBtn.setOnClickListener {
            var versionName = BuildConfig.VERSION_NAME //버전정보 가져오는 코드
            Toast.makeText(context,"현재 버전은 $versionName 입니다.",Toast.LENGTH_SHORT).show()
        }




    }
}