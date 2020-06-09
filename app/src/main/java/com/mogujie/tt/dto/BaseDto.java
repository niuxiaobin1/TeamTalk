package com.mogujie.tt.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: Leo
 * Date: 2/22/2020
 * Describe:
 **/
public class BaseDto<DATA> {
    @SerializedName("data")
    @Expose public DATA data;
    @SerializedName("status")
    @Expose public int status;
    @SerializedName("return_msg")
    @Expose public String message;
    @SerializedName("return_code")
    @Expose public String result;

    public static BaseDto dummySuccess() {
        BaseDto baseDto = new BaseDto();
        baseDto.result = "";
        baseDto.status = 200;
        return baseDto;
    }

    public static <T> List<T> createList(T... ts) {
        return new ArrayList<>(Arrays.asList(ts));
    }

}
