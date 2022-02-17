package com.example.youngm.Data

class URLData {
    companion object {
        const val MAIN_URL: String = "https://youngm-backend.herokuapp.com"
//        const val MAIN_URL: String = "http://192.168.0.103:80"
        const val CHECK_TOKEN: String = "/api/v1/authorization/check_token/"
        const val PROFILE_IMAGE: String = "/api/v1/additionaluserdata/get_profile_photo/"
        const val GET_FOLLOWING: String = "/api/v1/additionaluserdata/get_following/"
        const val GET_FOLLOWERS: String = "/api/v1/additionaluserdata/get_followers/"
        const val SUBSCRIBE: String = "/api/v1/additionaluserdata/subscribe/"
        const val LOGOUT: String = "/api/v1/authorization/logout/"
        const val PROFILE_INFORMANTION: String = "/api/v1/additionaluserdata/profile_information/"
        const val LIKE_COMMENT: String = "/api/v1/posts/like_comment/"
        const val LIKE_POST: String = "/api/v1/posts/like_post/"
        const val GET_FOLLOWING_POSTS: String = "/api/v1/posts/get_following_posts/"
        const val GET_POPULAR_POSTS: String = "/api/v1/posts/get_popular_posts/"
        const val GET_RECOMMEND_POSTS: String = "/api/v1/posts/get_recommend_posts/"
        const val GET_RECENT_POSTS: String = "/api/v1/posts/get_recent_posts/"
        const val ADD_COMMENT: String = "/api/v1/posts/add_comment/"
        const val GET_COMMENT: String = "/api/v1/posts/get_comment/"
        const val GET_DATA_OPEN_POST: String = "/api/v1/posts/get_data_open_post/"
        const val GET_LAST_POST_AUTHOR: String = "/api/v1/posts/get_last_posts_author/"
        const val GET_POPULAR_POST_AUTHOR: String = "/api/v1/posts/get_popular_posts_author/"
        const val LOGIN: String = "/api/v1/authorization/login/"
        const val CHECK_USERNAME: String = "/api/v1/registration/check_username/"
        const val GET_ALL_CATEGORY: String = "/api/v1/additionaluserdata/get_all_category/"
        const val CREATE_USER: String = "/api/v1/registration/create_user/"
        const val CREATE_POST: String = "/api/v1/posts/create_post/"
        const val DELETE_COMMENT: String = "/api/v1/posts/delete_comment/"
        const val UPDATE_COMMENT: String = "/api/v1/posts/update_comment/"
        const val GET_LIKED_POSTS: String = "/api/v1/posts/get_liked_post/"
        const val EDIT_PHOTO: String = "/api/v1/additionaluserdata/update_photo/"
        const val DELETE_POST: String = "/api/v1/posts/delete_post/"
    }
}

