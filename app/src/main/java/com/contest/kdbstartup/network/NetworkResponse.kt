package com.contest.kdbstartup.network

data class CategoryResponse (
    val success: Boolean, //응답 성공 여부
    val msg: String, //응답 실패 시 오류 메시지
    
    val cat1: Int, //첫 번째 카테고리 Index
    val cat2: Int, //두 번째 카테고리 Index
    val cat3: Int, //세 번째 카테고리 Index
)

data class ArticleResponse(
    val success: Boolean, //응답 성공 여부
    val msg: String //응답 실패 시 오류 메시지
)
