package com.mogujie.tt.bean
import java.io.Serializable

data class PayCodeBean(var return_code:String,
                     var return_msg:String,
                     var status:String,
                     var data: PayCodeData
                     )
data class PayCodeData(var user_openid:String,
                     var auth_code:String,
                     var creater_time:String,
                     var expire_time:String
                     ):Serializable{
    companion object {
        private const val serialVersionUID = 202003171339L
    }
}