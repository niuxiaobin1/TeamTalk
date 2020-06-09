package com.mogujie.tt.bean

data class QueryAuthPayBean(var return_code:String,
                     var return_msg:String,
                     var status:String,
                     var data: QueryAuthPayData
                     )
data class QueryAuthPayData(
        var order_sn:String,
        var sub_name:String,
        var amount:String
                     )