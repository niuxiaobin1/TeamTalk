package com.mogujie.tt.dto;

import com.google.gson.annotations.SerializedName;

public class InstitutionDto extends BaseDto<InstitutionDto.Data> {

    public static class Data{

        /**
         * institution_number : WZ3WpMaYSeGpKFn1d8lQTwcSetdPSM0Up716%2Bq7FSERz07vdAKfD%2BQNU%2BjN1w4fQUl8NhLmdlMXStTR%2BWJB56k6it6qVrQz9xosLw55sMgn8oTv%2Bye3JLOCfSxNRhNC7
         */

        @SerializedName("institution_number")
        public String institutionNumber;
    }
}
