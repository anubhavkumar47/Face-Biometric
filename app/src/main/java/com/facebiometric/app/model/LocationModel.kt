package com.facebiometric.app.model

data class LocationModel(val latitude: Double =0.0,
                         val longitude: Double=0.0,
                         val employeeID: String=""){
    constructor():this(0.0,0.0,"")
}
