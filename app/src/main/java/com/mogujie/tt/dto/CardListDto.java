package com.mogujie.tt.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CardListDto extends BaseDto<CardListDto.Data> {

    public static class Data{

        /**
         * user_openid : 2a422f203c17ad09c70ec61666a61038
         * list_info : [{"card_id":"47","card_bank_no":"999070","card_bank_name":"Test Fidelity Bank","card_account_name":"OLUWATOMIWO AYODEJI ILORI","card_account_number":"5050138001","card_default":"0"},{"card_id":"40","card_bank_no":"999022","card_bank_name":"Test Bank3","card_account_name":"Peter Ojo","card_account_number":"8086882292","card_default":"1"}]
         */

        @SerializedName("user_openid")
        public String userOpenid;
        @SerializedName("list_info")
        public List<ListInfoBean> listInfo;

        public static class ListInfoBean {
            /**
             * card_id : 47
             * card_bank_no : 999070
             * card_bank_name : Test Fidelity Bank
             * card_account_name : OLUWATOMIWO AYODEJI ILORI
             * card_account_number : 5050138001
             * card_default : 0
             */

            @SerializedName("card_id")
            public String cardId;
            @SerializedName("card_bank_no")
            public String cardBankNo;
            @SerializedName("card_bank_name")
            public String cardBankName;
            @SerializedName("card_account_name")
            public String cardAccountName;
            @SerializedName("card_account_number")
            public String cardAccountNumber;
            @SerializedName("card_default")
            public String cardDefault;
        }
    }
}
