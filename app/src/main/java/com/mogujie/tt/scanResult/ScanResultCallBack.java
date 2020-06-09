package com.mogujie.tt.scanResult;

public interface ScanResultCallBack {

    void addCallBack(String openId);
    void payCallBack(String payCode);
    void bScCallBack();
    void cSbCallBack1(String url);
    void cSbCallBack2(String order_sn);
}
