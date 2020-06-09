package com.mogujie.tt.bean

data class QueryUserBean(var return_code:String,
                     var return_msg:String,
                     var status:String,
                     var data: QueryUserData
                     )
data class QueryUserData(var user_openid:String
                     )
