package com.facebiometric.app.model

data class UserData(
    val employeeId: String,
    val nameOfUser: String,
    val emailOfUser: String,
    val passwordOfUser: String,
    val postOfUser: String,
    val department:String,
    val embeddingList: MutableList<String>
)
data class UserDataWithCon(
    val employeeId: String=null.toString(),
    val nameOfUser: String=null.toString(),
    val emailOfUser: String=null.toString(),
    val passwordOfUser: String=null.toString(),
    val postOfUser: String=null.toString(),
    val department:String=null.toString(),
    val embeddingList: MutableList<String>? =null
){
    constructor():this( null.toString(),null.toString(),null.toString(),null.toString(),null.toString(),null.toString(),null)
}
