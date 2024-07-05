package com.contest.kdbstartup.network

//게시글 카테고리 자동 분석 결과 DTO
data class CategoryResponse (
    val success: Boolean, //응답 성공 여부
    val msg: String, //응답 실패 시 오류 메시지
    
    val cat1: String, //첫 번째 카테고리
    val cat2: String, //두 번째 카테고리
    val keywords: List<String>, //키워드
)

//게시글 작성 결과 DTO
data class ArticleCreateResponse(
    val success: Boolean, //응답 성공 여부
    val msg: String //응답 실패 시 오류 메시지
)

data class RegisterResponse(
    val success: Boolean,
    val msg: String,

    val displayName: String,
    val uuid: String
)

data class LoginResponse(
    val success: Boolean,
    val msg: String,

    val displayName: String,
    val uuid: String
)
