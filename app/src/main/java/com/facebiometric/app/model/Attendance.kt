package com.facebiometric.app.model

data class Attendance(
    val employeeID: String = "",
    val date: String = "",
    val checkedIn: String = "",
    val checkedOut: String = "",
    val status: String = "" ,
    @Transient var isExpanded: Boolean = false // Present, Absent, Late
)
