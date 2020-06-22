package com.mogujie.tt.scanResult;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.mogujie.tt.OkgoCallBack.NigeriaCallBack;
import com.mogujie.tt.R;
import com.mogujie.tt.bean.BaseBean;
import com.mogujie.tt.bean.LoginBean;
import com.mogujie.tt.bean.QrType0Bean;
import com.mogujie.tt.config.GeneralConfig;
import com.mogujie.tt.config.RequestCode;
import com.mogujie.tt.config.ServerHostConfig;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.utils.PhoneUtil;
import com.mogujie.tt.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static com.mogujie.tt.app.IMApplication.INSTITUTION_NUMBER;

public class ScanResultUtil {

    public static void doResult(String result, ScanResultCallBack callBack) {
        if (TextUtils.isEmpty(result)) {
            return;
        }

        if (result.startsWith(GeneralConfig.FALG_NCAHT_ADD_FRIEND)) {
            callBack.addCallBack(result.substring(GeneralConfig.FALG_NCAHT_ADD_FRIEND.length()));
        } else if (result.startsWith(GeneralConfig.FALG_NCAHT_PAY_CODE)) {
            callBack.payCallBack(result.substring(GeneralConfig.FALG_NCAHT_PAY_CODE.length()));
        } else {
            byte[] encrypted1 = Base64.decode(result, Base64.DEFAULT);
            try {
                String originalString = new String(encrypted1, "utf-8");
                JSONObject jsonObject = new JSONObject(originalString);
                String qr_cate = jsonObject.getString("qr_cate");
                String sub_no = jsonObject.getString("sub_no");

                callBack.cSbCallBack3(qr_cate, sub_no);
            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
        }


//        else if(result.startsWith(GeneralConfig.FALG_NCAHT_CODEPAY_START)){
//            callBack.cSbCallBack2(result.substring(GeneralConfig.FALG_NCAHT_CODEPAY_START.length()));
//        }else if(result.startsWith("http://")||result.startsWith("https://")){
//            callBack.cSbCallBack1(result);
//        }

    }

    public static void queryMer(TTBaseActivity activity, String qr_cate, String sub_no) {
        activity.showDialog();
        HttpParams param = new HttpParams();
        param.put("institution_number", INSTITUTION_NUMBER);
        param.put("qr_cate", qr_cate);
        param.put("sub_no", sub_no);
        param.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        OkGo.<String>post(ServerHostConfig.GATEWAY_QUERY_MER)
                .params(param)
                .execute(new NigeriaCallBack() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        activity.dismissDialog();
                        BaseBean bean = new Gson().fromJson(response.body(), BaseBean.class);
                        if (RequestCode.SUCCESS.equals(bean.getStatus())) {
                            QrType0Bean qrType0Bean = new Gson().fromJson(response.body(), QrType0Bean.class);
                            if (qr_cate.equals("1")) {
                               // qr_cate=1是静态码（QrType=0是无金额静态码，QrType=1是有金额静态码）
                                if (qrType0Bean.getData().getQrType().equals("0")) {

                                } else if (qrType0Bean.getData().getQrType().equals("1")) {

                                }
                            } else if (qr_cate.equals("2")) {
                                //qr_cate=2是动态码

                            }

                        } else {
                            ToastUtil.toastShortMessage(bean.getReturn_msg());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        activity.dismissDialog();
                    }
                });
    }


}
