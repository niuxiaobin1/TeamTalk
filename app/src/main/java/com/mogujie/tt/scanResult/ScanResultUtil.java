package com.mogujie.tt.scanResult;

import android.text.TextUtils;

import com.mogujie.tt.config.GeneralConfig;
import com.mogujie.tt.config.SysConstant;

public class ScanResultUtil {

    public static void doResult(String result,ScanResultCallBack callBack){
        if (TextUtils.isEmpty(result)){
            return;
        }

        if (result.startsWith(GeneralConfig.FALG_NCAHT_ADD_FRIEND)){
            callBack.addCallBack(result.substring(GeneralConfig.FALG_NCAHT_ADD_FRIEND.length()));
        }else if(result.startsWith(GeneralConfig.FALG_NCAHT_PAY_CODE)){
            callBack.payCallBack(result.substring(GeneralConfig.FALG_NCAHT_PAY_CODE.length()));
        }else if(result.startsWith(GeneralConfig.FALG_NCAHT_CODEPAY_START)){
            callBack.cSbCallBack2(result.substring(GeneralConfig.FALG_NCAHT_CODEPAY_START.length()));
        }else if(result.startsWith("http://")||result.startsWith("https://")){
            callBack.cSbCallBack1(result);
        }

    }





}
