package com.example.youngm.Data

class CategoryInformation(val result: ArrayList<CategoryName>)
class CategoryName(val name: String)
class SubscribeInformation(val subscribe: String)
class Information(val username: String, val name: String, val location: String, val about_me: String, val photo: String, val followers: Int, val following: Int, val favorite_categories: List<FavoriteCategory>, val posts: List<UserPost>, val count_posts: Int, val isFollower: Boolean, val image_bar: Int, val open_liked_post: Boolean)
class FavoriteCategory(val category: String)
class UserPost(val music: List<UserMusic>, val photo: String)
class UserMusic(val music: String, val music_name: String)
class FollowInformationData(val information: List<FollowInformation>)
class FollowInformation(val username: String, val photo: String)
class Token(val token: String)
class AuthorPhoto(val image: String)
class PostsFeed(val posts: List<Post>)
class Post(val id: Int, val comment: Int, val likes: Int, val author: String, val photo: String, val photo_author: String, val music: List<Music>, val category: String, var isLiked: Boolean)
class Music(val id: Int, val music_name: String, val music:String, val music_time: String)
class PostCommentData(val comments: List<CommentFromPost>, val comment_count: Int)
class CommentFromPost(val id: Int, val author: String, val comment: String, val count_like: Int, val user_photo: String, val check: Boolean, val isLiked: Boolean)
class PostInformation(val id: Int, val comment: Int, val likes: Int, val author: String, val photo: String, val photo_author: String, val music: List<Music>, val category: String, var isLiked: Boolean, var description: String)


