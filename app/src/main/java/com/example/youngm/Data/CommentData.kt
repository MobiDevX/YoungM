package com.example.youngm.Data

data class CommentData(
    var id: Int,
    var username: String,
    var commentText: String,
    var countLike: Int,
    var userPhoto: String,
    var check: Boolean,
    var isLiked: Boolean
)