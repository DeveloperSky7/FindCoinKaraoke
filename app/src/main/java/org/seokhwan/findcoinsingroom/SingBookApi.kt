package org.seokhwan.findcoinsingroom

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SingBookApi {

    @GET("karaoke/kumyoung.json")
    fun getKyRecent(
    ): Call<SingList>

    @GET("karaoke/tj.json")
    fun getTjRecent(
    ): Call<SingList>

    @GET("karaoke/song/{title}/kumyoung.json")
    fun getKyTitle(
        @Path("title") title:String
    ): Call<SingList>

    @GET("karaoke/singer/{singer}/kumyoung.json")
    fun getKySinger(
        @Path("singer") singer:String
    ): Call<SingList>

    @GET("karaoke/song/{title}/tj.json")
    fun getTjTitle(
        @Path("title") title:String
    ): Call<SingList>

    @GET("karaoke/singer/{singer}/tj.json")
    fun getTjSinger(
        @Path("singer") singer:String
    ): Call<SingList>



}