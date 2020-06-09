package com.mogujie.tt.bean

import java.io.Serializable

data class BankBean(var return_code:String,
                     var return_msg:String,
                     var status:String,
                     var data: BankData
)

data class BankData(var list_info:List<BankInfo>)
data class BankInfo(var bank_no:String,
                         var bank_name:String,
                         var bank_cate:String
): Serializable {
    companion object {
        private const val serialVersionUID = 202003122339L
    }
}