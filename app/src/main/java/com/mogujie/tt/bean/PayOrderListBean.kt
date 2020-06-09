package com.mogujie.tt.bean

data class PayOrderListBean(var return_code: String,
                            var return_msg: String,
                            var status: String,
                            var data: PayOrderListData
)

data class PayOrderListData(var page: String,
                            var totalrows: String,
                            var totalpages: String,
                            var list_info: List<PayOrderInfoData1>
)

data class PayOrderInfoData(var order_time: String,
                            var order_amount: String,
                            var m_name: String
)

data class PayOrderInfoData1(var b_time: String,
                             var amount: String,
                             var m_name: String,
                             var user_name: String,
                             var payee_user_name: String,
                             var order_sn: String,
                             var b_type: String
)