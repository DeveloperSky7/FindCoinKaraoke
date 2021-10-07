package org.seokhwan.findcoinsingroom

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoApi {
    @GET("v2/local/search/keyword.json")    // Keyword.json의 정보를 받아옴
    fun getSearchKeyword(
        @Header("Authorization") key: String,     // 카카오 API 인증키 [필수]
        @Query("query") query: String,             // 검색을 원하는 질의어 [필수]
        @Query("x") x:String,                       // 중심 좌표 x 값 (경도 longitude)
        @Query("y") y:String,                       // 중심 좌표 y 값 (위도 latitude)
        @Query("radius") radius: Int,               // 중심 좌표 기준 거리
        @Query("sort") sort: String                 // 기준 좌표
        // 매개변수 추가 가능
        // @Query("category_group_code") category: String

    ): Call<ResultSearchData>    // 받아온 정보가 ResultSearchKeyword 클래스의 구조로 담김
}
