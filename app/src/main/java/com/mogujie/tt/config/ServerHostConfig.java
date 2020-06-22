package com.mogujie.tt.config;

public class ServerHostConfig {

    private static final String NigeriaBaseHost = "http://api.nullnull.net/";
    private static final String NigeriaHost = NigeriaBaseHost + "index.php/";

    public static final String GET_INSTITUTION_NUMBER = NigeriaHost + "customer/institution_number";

    //注册
    public static final String CUSTOMER_REGISTER = NigeriaHost + "customer/register";
    //获取验证码
    public static final String CUSTOMER_EMAIL_IDENTIFY = NigeriaHost + "customer/email_identify";
    //登录
    public static final String CUSTOMER_LOGIN = NigeriaHost + "customer/login";
    //更新sig
    public static final String CUSTOMER_USER_SIG = NigeriaHost + "customer/user_sig";
    //更新用户信息
    public static final String CUSTOMER_USER_UPDATE = NigeriaHost + "customer/user_update";
    //关于
    public static final String HTML_ABOUT =NigeriaBaseHost+"html/about.html";
    //银行列表
    public static final String CUSTOMER_BANK_LIST =NigeriaHost + "customer/bank_list";
    //添加银行卡
    public static final String CUSTOMER_BANK_CARD_ADD =NigeriaHost + "customer/bank_card_add";
    //银行卡列表
    public static final String CUSTOMER_BANK_CARD_LIST =NigeriaHost + "customer/bank_card_list";
    //设置默认付款卡
    public static final String CUSTOMER_BANK_CARD_DEFAULT =NigeriaHost + "customer/bank_card_default";
    //删除银行卡
    public static final String CUSTOMER_BANK_CARD_DELETE =NigeriaHost + "customer/bank_card_delete";
    //修改密码
    public static final String CUSTOMER_USER_UPDATE_PASSWORD=NigeriaHost + "customer/user_update_password";
    //设置支付密码
    public static final String CUSTOMER_USER_SET_PASSWORD=NigeriaHost + "customer/user_set_password";
    //忘记密码
    public static final String CUSTOMER_USER_FORGET_PASSWORD=NigeriaHost + "customer/user_forget_password";
    //获取付款码
    public static final String CUSTOMER_USER_AUTH_CODE=NigeriaHost + "customer/user_auth_code";
    //订单列表
    public static final String CUSTOMER_USER_ORDER_LIST=NigeriaHost + "customer/user_order_list";
    //所有订单列表
    public static final String CUSTOMER_USER_BILL_LIST=NigeriaHost + "customer/user_bill_list";
    //查询用户
    public static final String CUSTOMER_USER_QUERY=NigeriaHost + "customer/user_query";
    //转账
    public static final String GATEWAY_TRANSFER_ACCOUNTS=NigeriaHost + "Gateway/transfer_accounts";
    //固定付款
    public static final String GATEWAY_PAY=NigeriaHost + "Gateway/pay";
    //动态付款
    public static final String GATEWAY_CODEPAY=NigeriaHost + "Gateway/codepay";
    //动态查询订单
    public static final String GATEWAY_CODEPAYQUERY=NigeriaHost + "Gateway/codepayquery";
    //再次 支付
    public static final String GATEWAY_PAYAGAIN=NigeriaHost + "Gateway/payagain";
    //验证支付密码
    public static final String CUSTOMER_USER_VERIFY_PASSWORD=NigeriaHost + "customer/user_verify_password";
    //查询是否设置了支付密码
    public static final String CUSTOMER_QUERY_USER_PAY_PASSWORD=NigeriaHost + "customer/query_user_pay_password";
    //B扫C后-App 实时监听接口
    public static final String GATEWAY_AUTHCODEQUERY=NigeriaHost + "Gateway/authcodequery";
    //红包接口
    public static final String GATEWAY_RED_ENVELOPES=NigeriaHost + "Gateway/red_envelopes";
    //红包领取查询
    public static final String CUSTOMER_QUERY_RECEIVE=NigeriaHost + "customer/query_receive";
    //红包状态更新
    public static final String CUSTOMER_UPDATE_RECEIVE=NigeriaHost + "customer/update_receive";
    //商户信息查询接口
    public static final String GATEWAY_QUERY_MER=NigeriaHost + "gateway/query_mer";
}
