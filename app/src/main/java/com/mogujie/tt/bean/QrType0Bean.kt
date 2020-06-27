package com.mogujie.tt.bean
import java.io.Serializable

data class QrType0Bean(var return_code:String,
                     var return_msg:String,
                     var status:String,
                     var data: QrType0Data
                     )
data class QrType0Data(var ReturnCode:String,
                     var QrType:String,
                     var SubMerchantName:String,
                     var Mch_no:String,
                     var Sub_mch_no:String,
                     var Amount:String,
                     var MerUrl:String,
                     var MerchantName:String
                     ):Serializable{
    companion object {
        private const val serialVersionUID = 202006221104L
    }
}