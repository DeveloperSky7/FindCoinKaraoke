package org.seokhwan.findcoinsingroom

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import org.seokhwan.MapFragmentAdapter
import org.seokhwan.findcoinsingroom.databinding.FragmentMapBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Math.*
import kotlin.math.pow

data class singRoomList(var placeName: String,var roodAddress: String, var phoneNumber: String,
                        var latitude:String, var longitude:String, var distance:String )
class MapFragment : Fragment(), OnMapReadyCallback {

    lateinit var binding : FragmentMapBinding
    lateinit var mainActivity: MainActivity
    private lateinit var locationSource: FusedLocationSource
    private lateinit var mapView: com.naver.maps.map.MapView
    lateinit var naver:NaverMap

    var listItems = mutableListOf<singRoomList>()

    var uLatitude:Double = 0.0
    var uLongitude:Double = 0.0

    var placeLat:Double = 0.0
    var placeLong:Double = 0.0

    var placeCount = 0
    var resumeCount = 0

    var searchDataforResume:ResultSearchData? = null

    val TAG = "FindCoinSingRoom"
    val recyclerAdapter = MapFragmentAdapter()

    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK c1c82102b2cadfaa7c61a1d2fe202504"  // REST API 키

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMapBinding.inflate(layoutInflater,container,false)
        return binding.root

        mapView.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "MapFragment onViewCreated")

        mainActivity = context as MainActivity

        mapView = view.findViewById(R.id.mapfragment_map)
        mapView.getMapAsync(this)

//        val mapView = MapView(context) // kakao ver.
//        binding.mapfragmentMap.addView(mapView)
//        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading //현재 위치 추적

        //↓권한 확인 후 카카오로 좌표 날리기
        val permissionCheck = ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "MapFragment 현재 좌표 로딩중, 권한 확인")
            val locationManager: LocationManager =
                mainActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
                val userNowLocation: Location? =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                uLatitude = userNowLocation?.latitude!!
                val latitudeString = uLatitude.toString()
                uLongitude = userNowLocation?.longitude!!
                val longitudeString = uLongitude.toString()

                Log.d(TAG, "MapFragment uLatitude: $uLatitude, uLongitude: $uLongitude")

//                val uNowPosition = MapPoint.mapPointWithGeoCoord(uLatitude!!, uLongitude!!) //카카오용 카메라 이동(초기 내위치로)
//                mapView.setMapCenterPoint(uNowPosition, true) //카카오용 카메라 이동(초기 내위치로)

//                val marker = MapPOIItem() //카카오용 마커
//                marker.apply {
//                    itemName = "현재 위치"
//                    mapPoint = MapPoint.mapPointWithGeoCoord(uLatitude!!, uLongitude!!)
//                    markerType = MapPOIItem.MarkerType.CustomImage
//                    customImageResourceId = R.drawable.marker
//                    selectedMarkerType = MapPOIItem.MarkerType.CustomImage
//                    isCustomImageAutoscale = true
//                    setCustomImageAnchor(0.5f, 1.0f)
//                }
//                mapView.addPOIItem(marker)

                searchKeyword("코인노래방", longitudeString, latitudeString) //키워드 검색용

                Log.d(TAG, "MapFragment 현재 좌표 로딩 완료, 검색 좌표 보냄")

            } catch (e: NullPointerException) {
                Log.e(TAG, e.toString())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ActivityCompat.finishAffinity(context as MainActivity)
                } else {
                    ActivityCompat.finishAffinity(context as MainActivity)
                }
            }
        }
        //↑권한 확인 후 카카오로 좌표 날리기

        binding.mainframgentRecycler.adapter = recyclerAdapter
        val manager = LinearLayoutManager(context)
        binding.mainframgentRecycler.layoutManager = manager

        recyclerAdapter.setItemClickListener(object: MapFragmentAdapter.OnItemClickListener{ //리사이클러뷰 아이템 클릭시 이동
            override fun onClick(v: View, position: Int) {
                Log.d(TAG,"${listItems[position].placeName} 이동")
                val cameraUpdate = CameraUpdate.scrollAndZoomTo(
                    LatLng(listItems[position].latitude.toDouble(),listItems[position].longitude.toDouble()),16.0)
                cameraUpdate.animate(CameraAnimation.Linear,1000) //애니메이션
                naver.moveCamera(cameraUpdate)
            }
        })


    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MapFragment onResume")

        mapView.onResume()


        if (resumeCount == 0){
            resumeCount += 1
        } else{
            Log.d(TAG, "MapFragment onResume resume: $resumeCount")
            resumeSetMarker(searchDataforResume)

        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    // 키워드 검색 함수
    private fun searchKeyword(keyword: String, x:String, y:String) {
        val retrofit = Retrofit.Builder()   // Retrofit 구성
            .baseUrl(MapFragment.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoApi::class.java)   // 통신 인터페이스를 객체로 생성
        val call = api.getSearchKeyword(MapFragment.API_KEY, keyword, x , y, 5000, "distance")   // 검색 조건 입력

        // API 서버에 요청
        call.enqueue(object: Callback<ResultSearchData> {
            override fun onResponse(
                call: Call<ResultSearchData>,
                response: Response<ResultSearchData>
            ) {
                Log.d(TAG, "MapFragment 검색 성공, 리사이클러뷰 구현 메서드 실행, response.body")
                setRecyclerView(response.body(), y, x)
                searchDataforResume = response.body()
            }

            override fun onFailure(call: Call<ResultSearchData>, t: Throwable) {
                // 통신 실패
                Log.w(TAG, "MapFragment 통신 실패: ${t.message}")
            }
        })
    }

    private fun resumeSetMarker(searchResult: ResultSearchData?){
        Log.d(TAG, "MapFragment resumeSetMarker 실행")

        if (!searchResult?.documents.isNullOrEmpty()) {
            for (document in searchResult!!.documents){
                val resumePlaceLat = document.y.toDouble()
                val resumePlaceLong = document.x.toDouble()
                val resumePlaceName = document.place_name

                val marker = Marker()
                marker.position = LatLng(resumePlaceLat,resumePlaceLong)
                marker.width = Marker.SIZE_AUTO
                marker.height = Marker.SIZE_AUTO
                marker.captionMinZoom = 12.0
                marker.captionMaxZoom = 16.0
                marker.map = naver
                marker.captionText = "${resumePlaceName}"


            }
//            val locationOverlay = naver.locationOverlay //내위치 표시
//            locationOverlay.position = LatLng(uLatitude, uLongitude)
//            locationOverlay.isVisible = true
//            locationOverlay.circleRadius = 10

            val marker = Marker()
            marker.position = LatLng(uLatitude,uLongitude)
            marker.icon = OverlayImage.fromResource(R.drawable.arrow_direction_gps_location_navigation_icon)
            marker.width = Marker.SIZE_AUTO
            marker.height = Marker.SIZE_AUTO
            marker.captionMinZoom = 12.0
            marker.captionMaxZoom = 16.0
            marker.map = naver

        }else {  // 검색 결과 없음
            Log.d(TAG, "MapFragment resumeSetMarker 실행 -> 검색결과가 없습니다.")
        }
    }

    private fun setRecyclerView(searchResult: ResultSearchData?, latitude:String,longitude: String){
        Log.d(TAG, "MapFragment setRecyclerView 실행")
        val R = 6372.8 * 1000
        listItems.clear()

        if (!searchResult?.documents.isNullOrEmpty()) {
            for (document in searchResult!!.documents) {
                // ↓ 위도,경도 받아서 거리 계산식

                placeCount += 1
                placeLat = document.y.toDouble()
                placeLong = document.x.toDouble()

                var userLat = latitude.toDouble()
                var userLong = longitude.toDouble()

                val dLat = toRadians(placeLat - userLat)
                val dLong = toRadians(placeLong - userLong)

                val a = sin(dLat / 2).pow(2.0) + sin(dLong / 2).pow(2.0) * cos(toRadians(userLat)) * cos(
                    toRadians(placeLat))
                val c = 2 * asin(sqrt(a))

                val distanceDouble = (R * c)/1000
                val distance = String.format("%.1f",distanceDouble)
                // ↑ 위도,경도 받아서 거리 계산식

                val item = singRoomList(document.place_name,
                    document.road_address_name,
                    document.phone, document.y, document.x, distance)

                listItems.add(item)

                val marker = Marker()
                marker.position = LatLng(placeLat,placeLong)
                marker.width = Marker.SIZE_AUTO
                marker.height = Marker.SIZE_AUTO
                marker.captionMinZoom = 12.0
                marker.captionMaxZoom = 16.0
                marker.map = naver
                marker.captionText = "${document.place_name}"

//                // 지도에 마커 추가 kakao ver.
//                val mapView = MapView(context)
//                val point = MapPOIItem()
//                point.apply {
//                    itemName = document.place_name
//                    mapPoint = MapPoint.mapPointWithGeoCoord(document.y.toDouble(),
//                        document.x.toDouble())
//                    markerType = MapPOIItem.MarkerType.BluePin
//                }
//                mapView.addPOIItem(point)
            }
            if(listItems.size == 0){
                binding.mapfragmentEmptyText.visibility = View.VISIBLE
            }else {
                binding.mapfragmentEmptyText.visibility = View.GONE
            }
            recyclerAdapter.list = listItems
            recyclerAdapter.notifyDataSetChanged()
            Log.d(TAG, "MapFragment setRecyclerView 구현 완료, placeCount -> $placeCount")

        } else {  // 검색 결과 없음
            Toast.makeText(context, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show()
        }


    }

    override fun onMapReady(p0: NaverMap) {
        Log.d(TAG, "MapFragment onMapReady 실행, lat: $uLatitude, long: $uLongitude")
        val naverMap = p0
        naver = p0

        val marker = Marker()
        marker.position = LatLng(uLatitude,uLongitude)
        marker.icon = OverlayImage.fromResource(R.drawable.arrow_direction_gps_location_navigation_icon)
        marker.width = Marker.SIZE_AUTO
        marker.height = Marker.SIZE_AUTO
        marker.captionMinZoom = 12.0
        marker.captionMaxZoom = 16.0
        marker.map = naver

        naverMap.maxZoom = 18.0
        naverMap.minZoom = 10.0

        val cameraUpdate = CameraUpdate.scrollTo(LatLng(uLatitude,uLongitude)) //현재 위치 정보 받아서 카메라 이동
        naverMap.moveCamera(cameraUpdate)

        val uiSetting = naverMap.uiSettings // 현재 위치 UI 사용
        uiSetting.isLocationButtonEnabled = true
        uiSetting.isCompassEnabled = true

        locationSource = FusedLocationSource(mainActivity,LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        naverMap.addOnLocationChangeListener { location ->
            val cameraUpdate = CameraUpdate.scrollTo(LatLng(location.latitude,location.longitude))
            naverMap.moveCamera(cameraUpdate)
        }
    }
}