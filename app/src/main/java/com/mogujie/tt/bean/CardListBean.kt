package com.mogujie.tt.bean

import java.io.Serializable

data class CardListBean(var return_code:String,
                     var return_msg:String,
                     var status:String,
                     var data: CardListData
)

data class CardListData(var list_info:List<CardInfo>)
data class CardInfo(var card_id:String,
                         var card_bank_no:String,
                         var card_bank_name:String,
                         var card_account_name:String,
                         var card_account_number:String,
                         var card_default:String
): Serializable {
    companion object {
        private const val serialVersionUID = 202003122339L
    }
}