package com.mogujie.tt.imservice.manager;

import android.text.TextUtils;
import android.util.Log;

import com.mogujie.tt.DB.DBInterface;
import com.mogujie.tt.DB.entity.DepartmentEntity;
import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.imservice.callback.Packetlistener;
import com.mogujie.tt.imservice.event.ChangeHeaderEvent;
import com.mogujie.tt.imservice.event.ChangeUserInfoEvent;
import com.mogujie.tt.imservice.event.OtherUserInfoUpdateEvent;
import com.mogujie.tt.imservice.event.UserApplyInfoEvent;
import com.mogujie.tt.imservice.event.UserInfoEvent;
import com.mogujie.tt.protobuf.IMBaseDefine;
import com.mogujie.tt.protobuf.IMBuddy;
import com.mogujie.tt.protobuf.helper.ProtoBuf2JavaBean;
import com.mogujie.tt.utils.IMUIHelper;
import com.mogujie.tt.utils.Logger;
import com.mogujie.tt.utils.ToastUtil;
import com.mogujie.tt.utils.pinyin.PinYin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;

import static com.mogujie.tt.imservice.event.OtherUserInfoUpdateEvent.UpdateEvent.USER_DELETE_INFO_OK;
import static com.mogujie.tt.protobuf.IMBaseDefine.AddFriendResourceType.SEARCH_RES;
import static com.mogujie.tt.protobuf.IMBaseDefine.SearchUsersType.EMAIL_SEARCH;

/**
 * 负责用户信息的请求
 * 为回话页面以及联系人页面提供服务
 * <p>
 * 联系人信息管理
 * 普通用户的version  有总版本
 * 群组没有总version的概念， 每个群有version
 * 具体请参见 服务端具体的pd协议
 */
public class IMContactManager extends IMManager {
    private Logger logger = Logger.getLogger(IMContactManager.class);

    // 单例
    private static IMContactManager inst = new IMContactManager();

    public static IMContactManager instance() {
        return inst;
    }

    private IMSocketManager imSocketManager = IMSocketManager.instance();
    private DBInterface dbInterface = DBInterface.instance();

    // 自身状态字段
    private boolean userDataReady = false;
    private Map<Integer, UserEntity> userMap = new ConcurrentHashMap<>();
    private Map<Integer, UserEntity> applyUserMap = new ConcurrentHashMap<>();
    private Map<Integer, DepartmentEntity> departmentMap = new ConcurrentHashMap<>();


    @Override
    public void doOnStart() {
    }

    /**
     * 登陆成功触发
     * auto自动登陆
     */
    public void onNormalLoginOk() {
        onLocalLoginOk();
        onLocalNetOk();
    }

    /**
     * 加载本地DB的状态
     * 不管是离线还是在线登陆，loadFromDb 要运行的
     */
    public void onLocalLoginOk() {
        logger.d("contact#loadAllUserInfo");

        List<DepartmentEntity> deptlist = dbInterface.loadAllDept();
        logger.d("contact#loadAllDept dbsuccess");

        List<UserEntity> userlist = dbInterface.loadAllUsers();
        List<UserEntity> applyUserlist = dbInterface.loadAllApplyUsers();
        logger.d("contact#loadAllUserInfo dbsuccess");

        for (UserEntity userInfo : userlist) {
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            PinYin.getPinYin(userInfo.getMainName(), userInfo.getPinyinElement());
            userMap.put(userInfo.getPeerId(), userInfo);
        }
        for (UserEntity userInfo : applyUserlist) {
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            PinYin.getPinYin(userInfo.getMainName(), userInfo.getPinyinElement());
            applyUserMap.put(userInfo.getPeerId(), userInfo);
        }

        for (DepartmentEntity deptInfo : deptlist) {
            PinYin.getPinYin(deptInfo.getDepartName(), deptInfo.getPinyinElement());
            departmentMap.put(deptInfo.getDepartId(), deptInfo);
        }
        triggerEvent(UserInfoEvent.USER_INFO_OK);
    }

    /**
     * 网络链接成功，登陆之后请求
     */
    public void onLocalNetOk() {
        // 部门信息
        int deptUpdateTime = dbInterface.getDeptLastTime();
        reqGetDepartment(deptUpdateTime);

        // 用户信息
        int updateTime = dbInterface.getUserInfoLastTime();
        logger.d("contact#loadAllUserInfo req-updateTime:%d", updateTime);
        reqGetAllFriendsUsers(updateTime);
    }


    @Override
    public void reset() {
        userDataReady = false;
        userMap.clear();
        applyUserMap.clear();
    }


    /**
     * @param event
     */
    public void triggerEvent(UserInfoEvent event) {
        //先更新自身的状态
        switch (event) {
            case USER_INFO_OK:
                userDataReady = true;
                break;
        }
        EventBus.getDefault().postSticky(event);
    }

    /**
     * -----------------------事件驱动---end---------
     */

    private void reqGetAllFriendsUsers(int lastUpdateTime) {
        logger.i("contact#reqGetAllFriendsUsers");
        int userId = IMLoginManager.instance().getLoginId();

        IMBuddy.IMGetFriendListReq getFriendListReq = IMBuddy.IMGetFriendListReq.newBuilder()
                .setUserId(userId)
                .setLatestUpdateTime(lastUpdateTime).build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_GET_FRIEND_LIST_REQUEST_VALUE;
        imSocketManager.sendRequest(getFriendListReq, sid, cid);
    }

    /**
     * yingmu change id from string to int
     *
     * @param imAllUserRsp 1.请求所有用户的信息,总的版本号version
     *                     2.匹配总的版本号，返回可能存在变更的
     *                     3.选取存在变更的，请求用户详细信息
     *                     4.更新DB，保存globalVersion 以及用户的信息
     */
    public void onRepAllUsers(IMBuddy.IMAllUserRsp imAllUserRsp) {
        logger.i("contact#onRepAllUsers");
        int userId = imAllUserRsp.getUserId();
        int lastTime = imAllUserRsp.getLatestUpdateTime();
        // lastTime 需要保存嘛? 不保存了

        int count = imAllUserRsp.getUserListCount();
        logger.i("contact#user cnt:%d", count);
        if (count <= 0) {
            return;
        }

        int loginId = IMLoginManager.instance().getLoginId();
        if (userId != loginId) {
            logger.e("[fatal error] userId not equels loginId ,cause by onRepAllUsers");
            return;
        }

        List<IMBaseDefine.UserInfo> changeList = imAllUserRsp.getUserListList();
        ArrayList<UserEntity> needDb = new ArrayList<>();
        for (IMBaseDefine.UserInfo userInfo : changeList) {
            UserEntity entity = ProtoBuf2JavaBean.getUserEntity(userInfo);
            userMap.put(entity.getPeerId(), entity);
            needDb.add(entity);
        }

        dbInterface.batchInsertOrUpdateUser(needDb);
        triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
    }


    /**
     * yingmu change id from string to int
     *
     * @param getFriendListRsp 1.请求好友的信息,总的版本号version
     *                         2.匹配总的版本号，返回可能存在变更的
     *                         3.选取存在变更的，请求用户详细信息
     *                         4.更新DB，保存globalVersion 以及用户的信息
     */
    public void onRepFriendListUsers(IMBuddy.IMGetFriendListRsp getFriendListRsp) {
        logger.i("contact#onRepFriendListUsers");
        int userId = getFriendListRsp.getUserId();
        int lastTime = getFriendListRsp.getLatestUpdateTime();
        // lastTime 需要保存嘛? 不保存了

        int count = getFriendListRsp.getUserListCount();
        logger.i("contact#user cnt:%d", count);
        if (count <= 0) {
            return;
        }

        int loginId = IMLoginManager.instance().getLoginId();
        if (userId != loginId) {
            logger.e("[fatal error] userId not equels loginId ,cause by onRepAllUsers");
            return;
        }

        List<IMBaseDefine.UserInfo> changeList = getFriendListRsp.getUserListList();
        ArrayList<UserEntity> needDb = new ArrayList<>();
        for (IMBaseDefine.UserInfo userInfo : changeList) {
            UserEntity entity = ProtoBuf2JavaBean.getUserEntity(userInfo);
            userMap.put(entity.getPeerId(), entity);
            needDb.add(entity);
        }

        dbInterface.batchInsertOrUpdateUser(needDb);
        triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
    }


    public void updateRemake(UserEntity userEntity) {
        if (userMap.containsKey(userEntity.getPeerId())) {
            userMap.put(userEntity.getPeerId(), userEntity);
            dbInterface.insertOrUpdateUser(userEntity);
        }
    }

    public void deleteContact(UserEntity userEntity) {
        if (userMap.containsKey(userEntity.getPeerId())) {
            userMap.remove(userEntity.getPeerId());
            dbInterface.delUser(userEntity);
        }
        EventBus.getDefault().post(new OtherUserInfoUpdateEvent(USER_DELETE_INFO_OK, userEntity));
    }

    public UserEntity findContact(int buddyId) {
        if (buddyId > 0 && userMap.containsKey(buddyId)) {
            return userMap.get(buddyId);
        }
        return null;
    }

    /**
     * 搜索
     *
     * @param info
     */
    public void reqSearchUsers(String info) {
        logger.i("contact#contact#reqSearchUsers");
        if (TextUtils.isEmpty(info)) {
            logger.i("contact#contact#reqSearchUsers return,cause by null or empty");
            return;
        }
        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMSearchUsersReq imSearchUsersReq = IMBuddy.IMSearchUsersReq.newBuilder()
                .setUserId(loginId)
                .setSearchType(EMAIL_SEARCH)
                .setInfo(info)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_SEARCH_USER_REQUEST_VALUE;
        imSocketManager.sendRequest(imSearchUsersReq, sid, cid);
    }

    /**
     * 更改备注
     *
     * @param remake
     */
    public void reqModifyUserRemake(String remake, int id, Packetlistener packetlistener) {
        logger.i("contact#contact#reqModifyUserRemake");
        if (id == 0) {
            logger.i("contact#contact#reqModifyUserRemake return,cause by null or empty");
            return;
        }
        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMChangeFriendRemarkReq imChangeFriendRemarkReq = IMBuddy.IMChangeFriendRemarkReq.newBuilder()
                .setUserId(loginId)
                .setFuserId(id)
                .setRemark(remake)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_CHANGE_FRIEND_REMARK_REQUEST_VALUE;
        imSocketManager.sendRequest(imChangeFriendRemarkReq, sid, cid, packetlistener);
    }


    /**
     * 删除好友
     *
     * @param id
     */
    public void reqDeleteUser(int id, Packetlistener packetlistener) {
        logger.i("contact#contact#reqDeleteUser");
        if (id == 0) {
            logger.i("contact#contact#reqDeleteUser return,cause by null or empty");
            return;
        }
        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMDelFriendReq imDelFriendReq = IMBuddy.IMDelFriendReq.newBuilder()
                .setUserId(loginId)
                .setFuserId(id)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_DEL_FRIEND_REQUEST_VALUE;
        imSocketManager.sendRequest(imDelFriendReq, sid, cid, packetlistener);
    }


    /**
     * 更新用户头像
     *
     * @param url
     */
    public void reqChangeUsersHeader(String url) {
        logger.d("contact#contact#reqChangeUsersHeader%s", url);
        if (TextUtils.isEmpty(url)) {
            logger.d("contact#contact#reqChangeUsersHeader return,cause by null or empty");
            return;
        }
        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMChangeAvatarReq imChangeAvatarReq = IMBuddy.IMChangeAvatarReq.newBuilder()
                .setUserId(loginId)
                .setAvatarUrl(url)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_CHANGE_AVATAR_REQUEST_VALUE;
        imSocketManager.sendRequest(imChangeAvatarReq, sid, cid);
    }

    /**
     * 更新用户昵称和电话
     *
     * @param nick,phone
     */
    public void reqChangeUsersInfo(String nick, String phone) {
        logger.d("contact#contact#reqChangeUsersInfo----%s---%s", nick, phone);

        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMChangeUserinfoReq imChangeUserInfoReq = IMBuddy.IMChangeUserinfoReq.newBuilder()
                .setUserId(loginId)
                .setMobile(phone)
                .setNick(nick)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_CHANGE_USERINFO_REQUEST_VALUE;
        imSocketManager.sendRequest(imChangeUserInfoReq, sid, cid);
    }

    /**
     * 更新验证方式
     *
     * @param validate
     */
    public void reqChangeValidate(int validate,Packetlistener packetlistener) {
        logger.d("contact#contact#reqChangeValidate----%s", validate);

        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMChangeValidateReq imChangeValidateReq = IMBuddy.IMChangeValidateReq.newBuilder()
                .setUserId(loginId)
                .setValidate(validate)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_CHANGE_VALIDATE_REQUEST_VALUE;
        imSocketManager.sendRequest(imChangeValidateReq, sid, cid,packetlistener);
    }


    /**
     * 申请列表
     */
    public void reqApplyUsers() {
        logger.i("contact#contact#reqApplyUsers");

        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMGetApplyListReq getApplyListReq = IMBuddy.IMGetApplyListReq.newBuilder()
                .setUserId(loginId)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_GET_APPLY_LIST_REQUEST_VALUE;
        imSocketManager.sendRequest(getApplyListReq, sid, cid);
    }

    /**
     * //同意或者拒绝添加好友
     */
    public void reqApplyActionUsers(int fuserId) {
        logger.i("contact#contact#reqApplyActionUsers");

        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMAgreeFriendReq imAgreeFriendReq = IMBuddy.IMAgreeFriendReq.newBuilder()
                .setUserId(loginId)
                .setFuserId(fuserId)
                .setStatus(1)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_AGREE_ADD_FRIEND_REQUEST_VALUE;
        imSocketManager.sendRequest(imAgreeFriendReq, sid, cid);
    }


    /**
     * 添加好友
     *
     * @param friendsId
     */
    public void reqAddUsers(int friendsId, String alias) {
        logger.i("contact#contact#reqAddUsers");
        if (friendsId == 0) {
            logger.i("contact#contact#reqAddUsers return,cause by null or empty");
            return;
        }
        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMAddFriendReq addFriendReq = IMBuddy.IMAddFriendReq.newBuilder()
                .setUserId(loginId)
                .setFuserId(friendsId)
                .setAddFriendType(SEARCH_RES)
                .setNickName(alias)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_ADD_FRIEND_REQUEST_VALUE;
        imSocketManager.sendRequest(addFriendReq, sid, cid);
    }

    /**
     * 获取搜索用户
     *
     * @param searchUsersRsp
     */
    public void onRepSearchDetailUsers(IMBuddy.IMSearchUsersRsp searchUsersRsp) {
        if (searchUsersRsp.getUserInfoListList() == null || searchUsersRsp.getUserInfoListList().size() == 0) {
            ToastUtil.toastShortMessage("user not exit");
            return;
        }
        IMUIHelper.openUserProfileActivity(ctx, searchUsersRsp.getUserInfoListList().get(0));
    }


    /**
     * 获取好友申请列表
     *
     * @param getApplyListRsp
     */
    public void onRepApplyListUsers(IMBuddy.IMGetApplyListRsp getApplyListRsp) {
        if (getApplyListRsp == null || getApplyListRsp.getUserListList().size() == 0) {
            return;
        }
        int userId = getApplyListRsp.getUserId();
        int loginId = IMLoginManager.instance().getLoginId();
        if (userId != loginId) {
            logger.e("[fatal error] userId not equels loginId ,cause by onRepAllUsers");
            return;
        }

        List<IMBaseDefine.UserInfo> changeList = getApplyListRsp.getUserListList();
        ArrayList<UserEntity> needDb = new ArrayList<>();
        for (IMBaseDefine.UserInfo userInfo : changeList) {
            UserEntity entity = ProtoBuf2JavaBean.getUserEntity(userInfo);
            applyUserMap.put(entity.getPeerId(), entity);
            needDb.add(entity);
        }

        dbInterface.batchInsertOrUpdateApplyUser(needDb);
        EventBus.getDefault().postSticky(UserApplyInfoEvent.USER_APPLY_INFO_OK);
    }

    /**
     * 同意或者拒绝添加好友 返回
     *
     * @param imAgreeFriendRsp
     */
    public void onRepApplyActionUsers(IMBuddy.IMAgreeFriendRsp imAgreeFriendRsp) {
        if (imAgreeFriendRsp == null) {
            return;
        }
        int updateTime = dbInterface.getUserInfoLastTime();
        logger.d("contact#loadAllUserInfo req-updateTime:%d", updateTime);
        reqGetAllFriendsUsers(updateTime);
        EventBus.getDefault().postSticky(UserApplyInfoEvent.USER_APPLY_INFO_OK);
    }

    /**
     * 更新头像结果返回
     *
     * @param imChangeAvatarRsp
     */
    public void onRepChangeUserHeader(IMBuddy.IMChangeAvatarRsp imChangeAvatarRsp) {
        if (imChangeAvatarRsp == null) {
            EventBus.getDefault().postSticky(ChangeHeaderEvent.USER_CHANGE_HEADER_INFO_FAIL);
            return;
        }
        EventBus.getDefault().postSticky(ChangeHeaderEvent.USER_CHANGE_HEADER_INFO_OK);

    }

    /**
     * 更新用户昵称和电话结果返回
     *
     * @param imChangeUserinfoRsp
     */
    public void onRepChangeUserInfo(IMBuddy.IMChangeUserinfoRsp imChangeUserinfoRsp) {
        if (imChangeUserinfoRsp == null) {
            EventBus.getDefault().postSticky(ChangeUserInfoEvent.USER_CHANGE_INFO_INFO_FAIL);
            return;
        }
        EventBus.getDefault().postSticky(ChangeUserInfoEvent.USER_CHANGE_INFO_INFO_OK);

    }

    /**
     * 同意或者拒绝你的申请通知（广播）
     *
     * @param imAgreeFirendNotify
     */
    public void imChangeUserInfoBroadCast(IMBuddy.IMAgreeFirendNotify imAgreeFirendNotify) {
        if (imAgreeFirendNotify == null) {
            EventBus.getDefault().postSticky(ChangeUserInfoEvent.USER_CHANGE_INFO_INFO_FAIL);
            return;
        }
        Log.e("nxb-self",IMLoginManager.instance().getLoginId()+"");
        Log.e("nxb",imAgreeFirendNotify.getStatus()+"");
        Log.e("nxb",imAgreeFirendNotify.getFromUserId()+"");
        Log.e("nxb",imAgreeFirendNotify.getToUserId()+"");

    }


    /**
     * 获取添加用户
     *
     * @param imAddFriendRsp
     */
    public void onRepAddFriendsUsers(IMBuddy.IMAddFriendRsp imAddFriendRsp) {
        if (imAddFriendRsp == null) {
            return;
        }

    }


    /**
     * 请求用户详细信息
     *
     * @param userIds
     */
    public void reqGetDetaillUsers(ArrayList<Integer> userIds) {
        logger.i("contact#contact#reqGetDetaillUsers");
        if (null == userIds || userIds.size() <= 0) {
            logger.i("contact#contact#reqGetDetaillUsers return,cause by null or empty");
            return;
        }
        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMUsersInfoReq imUsersInfoReq = IMBuddy.IMUsersInfoReq.newBuilder()
                .setUserId(loginId)
                .addAllUserIdList(userIds)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_USER_INFO_REQUEST_VALUE;
        imSocketManager.sendRequest(imUsersInfoReq, sid, cid);
    }


    /**
     * 获取用户详细的信息
     *
     * @param imUsersInfoRsp
     */
    public void onRepDetailUsers(IMBuddy.IMUsersInfoRsp imUsersInfoRsp) {
        int loginId = imUsersInfoRsp.getUserId();
        boolean needEvent = false;
        List<IMBaseDefine.UserInfo> userInfoList = imUsersInfoRsp.getUserInfoListList();

        ArrayList<UserEntity> dbNeed = new ArrayList<>();
        for (IMBaseDefine.UserInfo userInfo : userInfoList) {
            UserEntity userEntity = ProtoBuf2JavaBean.getUserEntity(userInfo);
            int userId = userEntity.getPeerId();
            if (userMap.containsKey(userId) && userMap.get(userId).equals(userEntity)) {
                //没有必要通知更新
            } else {
                needEvent = true;
                userMap.put(userEntity.getPeerId(), userEntity);
                dbNeed.add(userEntity);
                if (userInfo.getUserId() == loginId) {
                    IMLoginManager.instance().setLoginInfo(userEntity);
                }
            }
        }
        // 负责userMap
        dbInterface.batchInsertOrUpdateUser(dbNeed);

        // 判断有没有必要进行推送
        if (needEvent) {
            triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
        }
    }


    public DepartmentEntity findDepartment(int deptId) {
        DepartmentEntity entity = departmentMap.get(deptId);
        return entity;
    }


    public List<DepartmentEntity> getDepartmentSortedList() {
        // todo eric efficiency
        List<DepartmentEntity> departmentList = new ArrayList<>(departmentMap.values());
        Collections.sort(departmentList, new Comparator<DepartmentEntity>() {
            @Override
            public int compare(DepartmentEntity entity1, DepartmentEntity entity2) {

                if (entity1.getPinyinElement().pinyin == null) {
                    PinYin.getPinYin(entity1.getDepartName(), entity1.getPinyinElement());
                }
                if (entity2.getPinyinElement().pinyin == null) {
                    PinYin.getPinYin(entity2.getDepartName(), entity2.getPinyinElement());
                }
                return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);

            }
        });
        return departmentList;
    }


    public List<UserEntity> getContactSortedList() {
        // todo eric efficiency
        List<UserEntity> contactList = new ArrayList<>(userMap.values());
        Collections.sort(contactList, new Comparator<UserEntity>() {
            @Override
            public int compare(UserEntity entity1, UserEntity entity2) {
                if (entity2.getPinyinElement().pinyin.startsWith("#")) {
                    return -1;
                } else if (entity1.getPinyinElement().pinyin.startsWith("#")) {
                    // todo eric guess: latter is > 0
                    return 1;
                } else {
                    if (entity1.getPinyinElement().pinyin == null) {
                        PinYin.getPinYin(entity1.getMainName(), entity1.getPinyinElement());
                    }
                    if (entity2.getPinyinElement().pinyin == null) {
                        PinYin.getPinYin(entity2.getMainName(), entity2.getPinyinElement());
                    }
                    return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
                }
            }
        });
        return contactList;
    }


    // 通讯录中的部门显示 需要根据优先级
    public List<UserEntity> getDepartmentTabSortedList() {
        // todo eric efficiency
        List<UserEntity> contactList = new ArrayList<>(userMap.values());
        Collections.sort(contactList, new Comparator<UserEntity>() {
            @Override
            public int compare(UserEntity entity1, UserEntity entity2) {
                DepartmentEntity dept1 = departmentMap.get(entity1.getDepartmentId());
                DepartmentEntity dept2 = departmentMap.get(entity2.getDepartmentId());

                if (entity1.getDepartmentId() == entity2.getDepartmentId()) {
                    // start compare
                    if (entity2.getPinyinElement().pinyin.startsWith("#")) {
                        return -1;
                    } else if (entity1.getPinyinElement().pinyin.startsWith("#")) {
                        // todo eric guess: latter is > 0
                        return 1;
                    } else {
                        if (entity1.getPinyinElement().pinyin == null) {
                            PinYin.getPinYin(entity1.getMainName(), entity1.getPinyinElement());
                        }
                        if (entity2.getPinyinElement().pinyin == null) {
                            PinYin.getPinYin(entity2.getMainName(), entity2.getPinyinElement());
                        }
                        return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
                    }
                    // end compare
                } else {
                    if (dept1 != null && dept2 != null && dept1.getDepartName() != null && dept2.getDepartName() != null) {
                        return dept1.getDepartName().compareToIgnoreCase(dept2.getDepartName());
                    } else
                        return 1;

                }
            }
        });
        return contactList;
    }


    // 确实要将对比的抽离出来 Collections
    public List<UserEntity> getSearchContactList(String key) {
        List<UserEntity> searchList = new ArrayList<>();
        for (Map.Entry<Integer, UserEntity> entry : userMap.entrySet()) {
            UserEntity user = entry.getValue();
            if (IMUIHelper.handleContactSearch(key, user)) {
                searchList.add(user);
            }
        }
        return searchList;
    }

    public List<DepartmentEntity> getSearchDepartList(String key) {
        List<DepartmentEntity> searchList = new ArrayList<>();
        for (Map.Entry<Integer, DepartmentEntity> entry : departmentMap.entrySet()) {
            DepartmentEntity dept = entry.getValue();
            if (IMUIHelper.handleDepartmentSearch(key, dept)) {
                searchList.add(dept);
            }
        }
        return searchList;
    }

    /**
     * ------------------------部门相关的协议 start------------------------------
     */

    // 更新的方式与userInfo一直，根据时间点
    public void reqGetDepartment(int lastUpdateTime) {
        logger.i("contact#reqGetDepartment");
        int userId = IMLoginManager.instance().getLoginId();

        IMBuddy.IMDepartmentReq imDepartmentReq = IMBuddy.IMDepartmentReq.newBuilder()
                .setUserId(userId)
                .setLatestUpdateTime(lastUpdateTime).build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_DEPARTMENT_REQUEST_VALUE;
        imSocketManager.sendRequest(imDepartmentReq, sid, cid);
    }

    public void onRepDepartment(IMBuddy.IMDepartmentRsp imDepartmentRsp) {
        logger.i("contact#onRepDepartment");
        int userId = imDepartmentRsp.getUserId();
        int lastTime = imDepartmentRsp.getLatestUpdateTime();

        int count = imDepartmentRsp.getDeptListCount();
        logger.i("contact#department cnt:%d", count);
        // 如果用户找不到depart 那么部门显示未知
        if (count <= 0) {
            return;
        }

        int loginId = IMLoginManager.instance().getLoginId();
        if (userId != loginId) {
            logger.e("[fatal error] userId not equels loginId ,cause by onRepDepartment");
            return;
        }
        List<IMBaseDefine.DepartInfo> changeList = imDepartmentRsp.getDeptListList();
        ArrayList<DepartmentEntity> needDb = new ArrayList<>();

        for (IMBaseDefine.DepartInfo departInfo : changeList) {
            DepartmentEntity entity = ProtoBuf2JavaBean.getDepartEntity(departInfo);
            departmentMap.put(entity.getDepartId(), entity);
            needDb.add(entity);
        }
        // 部门信息更新
        dbInterface.batchInsertOrUpdateDepart(needDb);
        triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
    }

    /**------------------------部门相关的协议 end------------------------------*/

    /**
     * -----------------------实体 get set 定义-----------------------------------
     */

    public Map<Integer, UserEntity> getUserMap() {
        return userMap;
    }

    public Map<Integer, UserEntity> getApplyUserMap() {
        return applyUserMap;
    }


    public Map<Integer, DepartmentEntity> getDepartmentMap() {
        return departmentMap;
    }

    public boolean isUserDataReady() {
        return userDataReady;
    }

}
