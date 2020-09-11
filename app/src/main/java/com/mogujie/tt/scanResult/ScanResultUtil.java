package com.mogujie.tt.scanResult;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.mogujie.tt.OkgoCallBack.NigeriaCallBack;
import com.mogujie.tt.bean.BaseBean;
import com.mogujie.tt.bean.QrType0Bean;
import com.mogujie.tt.config.GeneralConfig;
import com.mogujie.tt.config.RequestCode;
import com.mogujie.tt.config.ServerHostConfig;
import com.mogujie.tt.ui.activity.QrType0Activity;
import com.mogujie.tt.ui.activity.QrType1Activity;
import com.mogujie.tt.ui.activity.WebViewActivity;
import com.mogujie.tt.ui.base.TTBaseActivity;
import com.mogujie.tt.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

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



            try {

//              byte[] encrypted1 = Base64.decode(result, Base64.DEFAULT);//NIBSS二维码
//              String originalString = new String(encrypted1, "utf-8");
                String originalString =  parseEmvcoQrCode(result);  //Emvco二维码
                Log.e("nxb", originalString);
                JSONObject jsonObject = new JSONObject(originalString);
                String qr_cate = jsonObject.getString("qr_cate");
                String sub_no = jsonObject.getString("sub_no");
                if ("2".equals(qr_cate)) {
                    String order_sn = jsonObject.getString("order_sn");
                    callBack.cSbCallBack2(order_sn);
                } else {
                    callBack.cSbCallBack3(qr_cate, sub_no);
                }

            } catch ( JSONException e) {
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
                            Intent it;
                            if (qr_cate.equals("1")) {
                                // qr_cate=1是静态码（QrType=0是无金额静态码，QrType=1是有金额静态码）
                                //qr_cate=2是动态码 在callBack2里直接返回
                                if ("0".equals(qrType0Bean.getData().getQrType())) {
                                    it = new Intent(activity, QrType0Activity.class);
                                    it.putExtra(QrType0Activity.QR_TYPE_BEAN, qrType0Bean.getData());
                                    activity.startActivity(it);
                                } else if ("1".equals(qrType0Bean.getData().getQrType())) {
                                    it = new Intent(activity, QrType1Activity.class);
                                    it.putExtra(QrType1Activity.QR_TYPE_BEAN, qrType0Bean.getData());
                                    activity.startActivity(it);
                                } else {
                                    if ("NullCode".equals(qrType0Bean.getData().getReturnCode())) {
                                        it = new Intent(activity, WebViewActivity.class);
                                        it.putExtra(WebViewActivity.WEB_URL, qrType0Bean.getData().getMerUrl());
                                        activity.startActivity(it);
                                    }
                                }
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

    public static final String parseEmvcoQrCode(String codeString) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (TextUtils.isEmpty(codeString)) {
            return null;
        }
        String[] results = new String[15];

        while (codeString != null && codeString.length() != 0) {
            if (codeString.startsWith("crc")) {
                results[14] = codeString.substring(5);
                codeString = codeString.substring(5);
            } else {
                String t = codeString.substring(0, 2);
                int l = Integer.parseInt(codeString.substring(2, 4));
                String v = codeString.substring(4, 4 + l);
                int position = 0;
                switch (t) {
                    case "00":
                        if (l == 2) {
                            position = 0;
                        } else if (l == 19) {
                            position = 4;
                        }

                        break;
                    case "01":
                        if (l == 2) {
                            position = 1;
                        } else if (l == 11) {
                            position = 5;
                        }

                        break;
                    case "15":
                        position = 2;
                        break;
                    case "26":
                        position = 3;
                        break;
                    case "02":
                        position = 6;
                        break;
                    case "58":
                        position = 7;
                        break;
                    case "52":
                        position = 8;
                        break;
                    case "53":
                        position = 9;
                        break;
                    case "59":
                        position = 10;
                        break;
                    case "60":
                        position = 11;
                        break;
                    case "54":
                        position = 12;
                        break;
                    case "63":
                        position = 13;
                        break;
                }
                results[position] = v;
                codeString = codeString.substring(4 + l);
                if (TextUtils.isEmpty(codeString) && !TextUtils.isEmpty(results[3])&&
                        TextUtils.isEmpty(results[4])) {
                    codeString = results[3];
                }
            }
        }


        jsonObject.put("qr_cate","11".equals(results[1])?"1":"2");
        jsonObject.put("order_sn",results[6]);
        jsonObject.put("sub_no",results[5]);

        return jsonObject.toString();
    }

}
