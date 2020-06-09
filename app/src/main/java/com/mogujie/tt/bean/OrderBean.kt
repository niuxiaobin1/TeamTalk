package com.mogujie.tt.bean
import java.io.Serializable

data class OrderBean(var return_code:String,
                     var return_msg:String,
                     var status:String,
                     var data: OrderData
                     )
data class OrderData(var Amount:String,
                     var MerchantName:String,
                     var SubMerchantName:String,
                     var OrderSn:String
                     ):Serializable{
    companion object {
        private const val serialVersionUID = 202003180959L
    }
}