package com.mogujie.tt.config;

/**
 * @author : yingmu on 15-3-16.
 * @email : yingmu@mogujie.com.
 *
 */
public class UrlConstant {

    // 头像路径前缀
    public final static String AVATAR_URL_PREFIX = "";

    // access 地址
//    public final static String ACCESS_MSG_ADDRESS = "http://61.153.100.221:8080/msg_server";
    public final static String ACCESS_MSG_ADDRESS = "http://39.98.224.73:8080/msg_server";
    private static final String NIGERIABASEHOST = "http://api.nullnull.net/";
    private static final String NIGERIAHOST = NIGERIABASEHOST + "index.php/";


    //银行卡列表
    public static final String CUSTOMER_BANK_CARD_LIST =NIGERIAHOST + "customer/bank_card_list";
    //查询是否设置了支付密码
    public static final String CUSTOMER_QUERY_USER_PAY_PASSWORD=NIGERIAHOST + "customer/query_user_pay_password";
    //B扫C后-App 实时监听接口
    public static final String GATEWAY_AUTHCODEQUERY=NIGERIAHOST + "Gateway/authcodequery";

}
