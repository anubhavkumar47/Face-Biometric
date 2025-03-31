package com.facebiometric.app.model

data class EmployeeIDAndEmbedding(val employeeID:String =null.toString(),
                                  val embeddingList:MutableList<String>? =null){
    constructor():this(null.toString(),null)
}
