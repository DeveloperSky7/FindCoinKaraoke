package org.seokhwan.findcoinsingroom

class SingList : ArrayList<SingListItem>()

data class SingListItem(
    val brand: String,
    val composer: String,
    val lyricist: String,
    val no: String,
    val release: String,
    val singer: String,
    val title: String
)