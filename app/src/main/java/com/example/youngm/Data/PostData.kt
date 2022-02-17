package com.example.youngm.Data

data class PostData(
    val id: Int,
    var musicCategory: String,
    var postUsername: String,
    var postUserPhoto: String,
    var postPhoto: String,
    var likeCount: Int,
    var commentCount: Int,
    var isLiked: Boolean,
    var music: ArrayList<String>,
    var musicName: ArrayList<String>,
    var isPlaying: Boolean,
    var isSelected: Boolean,
    var musicPositionPlaying: Int
)