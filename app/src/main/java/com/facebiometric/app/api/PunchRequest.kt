package com.facebiometric.app.api

data class PunchRequest(
    val BranchID: String="",
    val EmployeeID: String="",
    val Latitude: String="",
    val Longitude: String="",
    val ApiKey:String=""
){
    constructor():this("","","","","")
}
