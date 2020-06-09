package com.mogujie.tt.bean

import java.io.Serializable

data class UserInfo(var return_code:String,
                     var return_msg:String,
                     var status:String,
                     var data: UserInfoData
)
data class UserInfoData(var user_openid:String,
                         var user_head:String,
                         var user_nickname:String,
                         var user_account:String,
                         var user_gender:String,
                         var user_phone_number:String
): Serializable {
    companion object {
        private const val serialVersionUID = 202003122236L
    }
}