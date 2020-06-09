package com.mogujie.tt.OkgoCallBack;

import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.request.base.Request;
import com.mogujie.tt.app.IMApplication;
import com.mogujie.tt.utils.MD5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


public abstract class NigeriaCallBack extends StringCallback {
    @Override
    public void onStart(Request<String, ? extends Request> request) {

        List<Map.Entry<String, List<String>>> infoIds =
                new ArrayList<>(request.getParams().urlParamsMap.entrySet());
        //step1:sort对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
        Collections.sort(infoIds, new Comparator<Map.Entry<String, List<String>>>() {

            public int compare(Map.Entry<String, List<String>> o1, Map.Entry<String, List<String>> o2) {
                return (o1.getKey()).compareTo(o2.getKey());
            }
        });
        // 构造签名键值对的格式
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,  List<String>> item : infoIds) {
            if (item.getKey() != null || item.getKey() != "") {

                if (item.getKey().equals("code")){
                    //注册的验证码，不参与签名
                    continue;
                }
                if (item.getValue()==null||item.getValue().size()==0){
                    continue;
                }
                String key = item.getKey();
                String val = item.getValue().get(0);
                if (!(val == "" || val == null)) {
                    sb.append(key).append("=" ).append(val).append("&");
                }
            }
        }
        if (sb.length()>0){
            sb.deleteCharAt(sb.length()-1);
        }
        //第二步：拼接key
        sb.append(IMApplication.API_KEY);

        //第三步：进行MD5
        String sign= MD5.md5Password(sb.toString()).toUpperCase();
        request.getParams().put("sign",sign);
        super.onStart(request);
    }
}
