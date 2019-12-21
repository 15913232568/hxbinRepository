package com.ygsoft.lwh.entity;


import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class MySqlVO implements Serializable {

    private static final long serialVersionUID = 8019746576904509855L;

    private List<MySqlVO> childs;

    //事项编号
    private String eventNo;

    //事项名称
    private String eventName;

    //父类id
    private String pid;

    //父类名称
    private String pName;

    //treeCode
    private String treeCode;

    //treeName
    private String treeName;

    //部门id
    private String departId;

    //部门名称
    private String departName;

    //本级是否能取号:1是0否
    private Integer isGetNo;

    //状态:0启用1禁用
    private Integer status;

    //取号前缀
    private String numPrefix;

    //是否填报：1是0否
    private Integer isTb;

    //是否叶子节点:1是0否
    private Integer isLeaf;

    //特殊标记  bltz并联投资   bjlq咨询、补交、领取
    private String specialMark;

    private String cityReservation; //市预约对应字段

    //热度
    private Integer heat;

    //排序
    private Integer sortKey;

    //上午开始办理时间
    private String amBegin;

    //上午结束办理时间
    private String amEnd;

    //下午开始办理时间
    private String pmBegin;

    //下午结束办理时间
    private String pmEnd;

    //创建时间
    private Date createTime;

    //修改时间
    private Date updateTime;

    //事项类型 0非即办件 1即办件
    private Integer isImmediately;

    //是否网厅事项 0非网厅事项 1网厅事项
    private Integer isNetEvent;

    //事项类型(企业or个人)
    private Integer eventType;

    //更新备注
    private String upRemark;

    //是否遵循节假日规则 1是0否
    private Integer isFollowHoliday;

    //是否可以周六办理
    private Integer canSaturdays;

    //是否可以周日办理
    private Integer canSundays;

    //待删除的平台列表
    private List<String> delPlatformIds;

    //平台列表
    private List<String> platformIds;

    //以下字段只用于页面传值使用，不存入数据库

    //事项取号规则列表
    //private List<JwEventLimitPO> limitPOList;
    private List<String> limitPOList;

    //修改时候  删除规则列表的id
    private List<String> delLimitIds;

    //原事项名
    private String oldEventName;

    //是否有孙子
    private boolean isAllChildrenLeaf;

    //是否当日可以取号（受节假日控制） 1是0否
    private Integer isCanTakeNum;

    //事项填报类型
    //private List<JwTbEventTbTypePO> jwTbEventTbTypePOList;
    private List<String> jwTbEventTbTypePOList;

    private String gid;

    private String zaazId;

    private String platform;

    private String office;


}
