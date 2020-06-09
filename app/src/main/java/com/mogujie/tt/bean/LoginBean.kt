package com.mogujie.tt.bean
import java.io.Serializable

data class LoginBean(var return_code:String,
                     var return_msg:String,
                     var status:String,
                     var data: LoginBeanData
                     )
data class LoginBeanData(var user_openid:String,
                     var user_sig:String,
                     var user_account:String
                     ):Serializable{
    companion object {
        private const val serialVersionUID = 202003121747L
    }
}