package com.mogujie.tt.dto;

import com.google.gson.annotations.SerializedName;

public class VersionDto extends BaseDto<VersionDto.Data> {

    public static final class Data{

        /**
         * version : 0
         * url :
         */

        @SerializedName("version")
        public int version;
        @SerializedName("url")
        public String url;
    }
}
