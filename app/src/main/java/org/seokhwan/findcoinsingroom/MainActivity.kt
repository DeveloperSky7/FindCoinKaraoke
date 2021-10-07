package org.seokhwan.findcoinsingroom

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import org.seokhwan.findcoinsingroom.databinding.ActivityMainBinding
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

//    lateinit var resultListener: ActivityResultLauncher<Intent> // 인텐트 받아오는 변수 설정
//    val myDb = FirebaseFirestore.getInstance()

    val TAG = "FindCoinSingRoom"
    var backKeyPressedTime : Long = 0
    var userDpi = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Log.d(TAG,"MainActivity onCreate")

        // 화면 해상도 분류
        val display = windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = resources.displayMetrics.density
//        val dpHeight = outMetrics.heightPixels / density //높이dp값
//        val dpWidth = outMetrics.widthPixels / density //가로dp값

        if (3.00 < density){
            userDpi = 6
        } else if(2.00 < density && density <= 3.00) {
            userDpi = 5
        } else if(1.50< density && density <= 2.00) {
            userDpi = 4
        } else if(1.00< density && density <= 1.50) {
            userDpi = 3
        } else if(0.75< density && density <= 1.00) {
            userDpi = 2
        } else {
            userDpi = 1
        }
        // ldpi = 1(120dpi), mdpi = 2(160dpi), hdpi = 3(240dpi), xdpi = 4(320dpi), xxdpi = 5(480dpi), xxxdpi = 6(640dpi)

//        val userId = getMyId() // uid 받아오기
        getAppKeyHash() // keyHash 받아오기

        //권한 요청 ↓
        var requestPermissions = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_COARSE_LOCATION)

        val permissionCheck = PermissionCheck(this, requestPermissions)
        permissionCheck.permissioinCheck()
        //권한 요청 ↑

        //뷰페이져 생성 ↓
        val fragmentList = listOf(MainFragment(),MapFragment(),BookFragment(),SettingFragment())
        val fragmentAdapter = FragmentAdapter(this)
        fragmentAdapter.fragmentList = fragmentList
        binding.mainactivityViewpager.adapter = fragmentAdapter
        binding.mainactivityViewpager.isUserInputEnabled = false //뷰페이저 스와이프 막기

        val tabTitle = listOf<String>("HOME","MAP","BOOK","SETTING")
        TabLayoutMediator(binding.mainactivityTablayout,binding.mainactivityViewpager){
                tab,position -> tab.text = tabTitle[position]
        }.attach()

        when(userDpi){
            // ldpi = 1(120dpi), mdpi = 2(160dpi), hdpi = 3(240dpi), xdpi = 4(320dpi), xxdpi = 5(480dpi), xxxdpi = 6(640dpi)
            6-> {
                Log.d(TAG,"MainActivity userDpi => xxxdpi.")
                binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.xxxhdpi_icon_home_select)
                binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.xxxhdpi_icon_map)
                binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.xxxhdpi_icon_book)
                binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.xxxhdpi_icon_setting) }
            5 -> {
                Log.d(TAG,"MainActivity userDpi => xxdpi.")
                binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.xxhdpi_icon_home_select)
                binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.xxhdpi_icon_map)
                binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.xxhdpi_icon_book)
                binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.xxhdpi_icon_setting) }
            4-> {
                Log.d(TAG,"MainActivity userDpi => xdpi.")
                binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.icon_xhdpi__home_select)
                binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.icon_xhdpi__map)
                binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.icon_xhdpi_book)
                binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.icon_xhdpi__setting) }
            3 -> {
                Log.d(TAG,"MainActivity userDpi => hdpi.")
                binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.hdpi_icon_home_select)
                binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.hdpi_icon_map)
                binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.hdpi_icon_book)
                binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.hdpi_icon_setting) }
            2 -> {
                Log.d(TAG,"MainActivity userDpi => mdpi.")
                binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.mdpi_icon_home_select)
                binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.mdpi_icon_map)
                binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.mdpi_icon_book)
                binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.mdpi_icon_setting) }
            1 -> {
                Log.d(TAG,"MainActivity userDpi => ldpi.")
                binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.ldpi_icon_home_select)
                binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.ldpi_icon_map)
                binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.ldpi_icon_book)
                binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.ldpi_icon_setting) }
        }

        binding.mainactivityViewpager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                when (position) {
                    0-> { //home 이동
                        if (userDpi == 6){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.xxxhdpi_icon_home_select)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.xxxhdpi_icon_map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.xxxhdpi_icon_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.xxxhdpi_icon_setting)
                        } else if(userDpi == 5){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.xxhdpi_icon_home_select)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.xxhdpi_icon_map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.xxhdpi_icon_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.xxhdpi_icon_setting)
                        } else if (userDpi == 4){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.icon_xhdpi__home_select)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.icon_xhdpi__map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.icon_xhdpi_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.icon_xhdpi__setting)
                        } else if (userDpi == 3){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.hdpi_icon_home_select)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.hdpi_icon_map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.hdpi_icon_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.hdpi_icon_setting)
                        } else if (userDpi == 2){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.mdpi_icon_home_select)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.mdpi_icon_map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.mdpi_icon_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.mdpi_icon_setting)
                        } else if (userDpi == 1){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.ldpi_icon_home_select)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.ldpi_icon_map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.ldpi_icon_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.ldpi_icon_setting)
                        }

                    }
                    1-> { //map 이동
                        if (userDpi == 6){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.xxxhdpi_icon_home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.xxxhdpi_icon_map_select)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.xxxhdpi_icon_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.xxxhdpi_icon_setting)
                        }else if (userDpi == 5){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.xxhdpi_icon_home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.xxhdpi_icon_map_select)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.xxhdpi_icon_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.xxhdpi_icon_setting)
                        }else if (userDpi == 4){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.icon_xhdpi__home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.icon_xhdpi__map_select)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.icon_xhdpi_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.icon_xhdpi__setting)
                        }else if (userDpi == 3){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.hdpi_icon_home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.hdpi_icon_map_select)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.hdpi_icon_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.hdpi_icon_setting)
                        }else if (userDpi == 2){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.mdpi_icon_home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.mdpi_icon_map_select)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.mdpi_icon_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.mdpi_icon_setting)
                        }else if (userDpi == 1){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.ldpi_icon_home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.ldpi_icon_map_select)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.ldpi_icon_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.ldpi_icon_setting)
                        }
                        }

                    2-> { //book 이동
                        if(userDpi == 6){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.xxxhdpi_icon_home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.xxxhdpi_icon_map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.xxxhdpi_icon_book_select)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.xxxhdpi_icon_setting)
                        }else if (userDpi == 5){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.xxhdpi_icon_home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.xxhdpi_icon_map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.xxhdpi_icon_book_select)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.xxhdpi_icon_setting)
                        }else if (userDpi == 4){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.icon_xhdpi__home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.icon_xhdpi__map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.icon_xhdpi_book_select)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.icon_xhdpi__setting)
                        }else if (userDpi == 3){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.hdpi_icon_home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.hdpi_icon_map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.hdpi_icon_book_select)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.hdpi_icon_setting)
                        }else if (userDpi == 2){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.mdpi_icon_home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.mdpi_icon_map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.mdpi_icon_book_select)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.mdpi_icon_setting)
                        }else if (userDpi == 1){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.ldpi_icon_home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.ldpi_icon_map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.ldpi_icon_book_select)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.ldpi_icon_setting)
                        }
                    }
                    3-> { //setting 이동
                        if(userDpi == 6){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.xxxhdpi_icon_home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.xxxhdpi_icon_map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.xxxhdpi_icon_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.xxxhdpi_icon_setting_select)
                        }else if (userDpi == 5){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.xxhdpi_icon_home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.xxhdpi_icon_map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.xxhdpi_icon_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.xxhdpi_icon_setting_select)
                        }else if(userDpi == 4){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.icon_xhdpi__home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.icon_xhdpi__map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.icon_xhdpi_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.icon_xhdpi__setting_select)
                        }else if(userDpi == 3){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.hdpi_icon_home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.hdpi_icon_map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.hdpi_icon_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.hdpi_icon_setting_select)
                        }else if (userDpi == 2){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.mdpi_icon_home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.mdpi_icon_map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.mdpi_icon_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.mdpi_icon_setting_select)
                        }else if (userDpi == 1){
                            binding.mainactivityTablayout.getTabAt(0)?.setIcon(R.drawable.ldpi_icon_home)
                            binding.mainactivityTablayout.getTabAt(1)?.setIcon(R.drawable.ldpi_icon_map)
                            binding.mainactivityTablayout.getTabAt(2)?.setIcon(R.drawable.ldpi_icon_book)
                            binding.mainactivityTablayout.getTabAt(3)?.setIcon(R.drawable.ldpi_icon_setting_select)
                        }
                    }

                }
            }
            override fun onPageScrollStateChanged(state: Int) { //탭 아이콘 설정, select/default
            }
        })
        Log.d(TAG,"MainActivity 뷰페이져, 탭 생성 성공")
        //뷰페이져 생성 ↑
    }
    //키 해시 받아오는 메서드
    private fun getAppKeyHash() {
        try {
            val info =
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                var md: MessageDigest
                md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val something = String(Base64.encode(md.digest(), 0))
                Log.e(TAG, "keyHash -> $something")
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }
    //uid 받아오는 메서드
    @SuppressLint("HardwareIds")
    fun getMyId(): String {
        val uid = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        return uid
    }
    //뒤로가기 두번 누르면 종료
    override fun onBackPressed() {
        if(System.currentTimeMillis() > backKeyPressedTime + 2500){
            backKeyPressedTime = System.currentTimeMillis()
            Toast.makeText(this,"뒤로가기 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2500){
            Log.d(TAG,"종료하겠습니다.")
            finishAffinity()
        }
    }

}