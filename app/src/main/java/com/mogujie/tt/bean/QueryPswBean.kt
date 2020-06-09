package com.mogujie.tt.bean

data class QueryPswBean(var return_code:String,
                     var return_msg:String,
                     var status:String,
                     var data: QueryPswData
                     )
data class QueryPswData(var is_pay_password:String
                     )