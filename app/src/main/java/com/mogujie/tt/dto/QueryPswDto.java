package com.mogujie.tt.dto;

import com.google.gson.annotations.SerializedName;

public class QueryPswDto extends BaseDto<QueryPswDto.Data> {

    public static class Data{

        /**
         * is_pay_password : 1
         */

        @SerializedName("is_pay_password")
        public String isPayPassword;
    }
}
