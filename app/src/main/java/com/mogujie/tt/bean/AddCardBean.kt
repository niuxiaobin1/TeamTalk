package com.mogujie.tt.bean
import java.io.Serializable

data class AddCardBean(var return_code:String,
                     var return_msg:String,
                     var status:String,
                     var data: AddCardData
                     )
data class AddCardData(var user_openid:String,
                     var card_id:String,
                     var is_pay_password:String
                     ):Serializable{
    companion object {
        private const val serialVersionUID = 202003140007L
    }
}