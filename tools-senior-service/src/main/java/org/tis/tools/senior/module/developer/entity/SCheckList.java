package org.tis.tools.senior.module.developer.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import org.tis.tools.core.entity.enums.CommonEnumDeserializer;
import org.tis.tools.senior.module.developer.entity.enums.CheckErrorType;
import org.tis.tools.senior.module.developer.entity.enums.CommitType;
import org.tis.tools.senior.module.developer.entity.enums.ConfirmStatus;

import java.io.Serializable;

/**
 * sCheckList核对异常的代码清单，包括申请清单和发版清单
 * 
 * @author Auto Generate Tools
 * @date 2018/07/10
 */
@Data
@TableName("s_check_list")
public class SCheckList implements Serializable {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * 模型名称
     */
    public static final String NAME = "核查代码清单";

    /**
     * guid对应表字段
     */
    public static final String COLUMN_GUID = "guid";

    /**
     * guidCheck对应表字段
     */
    public static final String COLUMN_GUID_CHECK = "guid_check";

    /**
     * errorType对应表字段
     */
    public static final String COLUMN_ERROR_TYPE = "error_type";

    /**
     * programName对应表字段
     */
    public static final String COLUMN_PROGRAM_NAME = "program_name";

    /**
     * fullPath对应表字段
     */
    public static final String COLUMN_FULL_PATH = "full_path";

    /**
     * partOfProject对应表字段
     */
    public static final String COLUMN_PART_OF_PROJECT = "part_of_project";

    /**
     * commitType对应表字段
     */
    public static final String COLUMN_COMMIT_TYPE = "commit_type";

    /**
     * confirmStatus对应表字段
     */
    public static final String COLUMN_CONFIRM_STATUS = "confirm_status";

    /**
     * guidDelivery对应表字段
     */
    public static final String COLUMN_GUID_DELIVERY = "guid_delivery";

    /**
     * guidDeliveryList对应表字段
     */
    public static final String COLUMN_GUID_DELIVERY_LIST = "guid_delivery_list";

    /**
     * patchType对应表字段
     */
    public static final String COLUMN_PATCH_TYPE = "patch_type";

    /**
     * deployWhere对应表字段
     */
    public static final String COLUMN_DEPLOY_WHERE = "deploy_where";

    /**
     * guid逻辑名
     */
    public static final String NAME_GUID = "数据id";

    /**
     * guidCheck逻辑名
     */
    public static final String NAME_GUID_CHECK = "核对GUID";

    /**
     * errorType逻辑名
     */
    public static final String NAME_ERROR_TYPE = "错误类型";

    /**
     * programName逻辑名
     */
    public static final String NAME_PROGRAM_NAME = "程序名称";

    /**
     * fullPath逻辑名
     */
    public static final String NAME_FULL_PATH = "代码全路径";

    /**
     * partOfProject逻辑名
     */
    public static final String NAME_PART_OF_PROJECT = "代码所在工程";

    /**
     * commitType逻辑名
     */
    public static final String NAME_COMMIT_TYPE = "操作类型";

    /**
     * confirmStatus逻辑名
     */
    public static final String NAME_CONFIRM_STATUS = "确认状态";

    /**
     * guidDelivery逻辑名
     */
    public static final String NAME_GUID_DELIVERY = "投产申请GUID";

    /**
     * guidDeliveryList逻辑名
     */
    public static final String NAME_GUID_DELIVERY_LIST = "投产清单GUID";

    /**
     * patchType逻辑名
     */
    public static final String NAME_PATCH_TYPE = "补丁类型";

    /**
     * deployWhere逻辑名
     */
    public static final String NAME_DEPLOY_WHERE = "部署到";

    /**
     * 数据id:唯一标示某条数据（自增长）
     */
    @TableId
    private Integer guid;

    /**
     * 核对GUID:唯一标示某条数据（自增长）
     */
    private Integer guidCheck;

    /**
     * 错误类型:D 申请清单异常
     * M 合并清单异常
     */
    @JSONField(deserializeUsing = CommonEnumDeserializer.class)
    private CheckErrorType errorType;

    /**
     * 程序名称:冗余设计
     */
    private String programName;

    /**
     * 代码全路径
     */
    private String fullPath;

    /**
     * 代码所在工程:记录该代码所在的工程名称（s_project.project_name）
     * 冗余设计
     */
    private String partOfProject;

    /**
     * 操作类型:A  新增 Add
     * U  修改 Update
     * D  删除 Delete
     */
    @JSONField(deserializeUsing = CommonEnumDeserializer.class)
    private CommitType commitType;

    /**
     * 确认状态:合并后的代码都需要开发人员进行确认
     * 0 待确认
     * 1 确认
     * 2 有异议（代码合并有问题，需要线下手工处理）
     */
    @JSONField(deserializeUsing = CommonEnumDeserializer.class)
    private ConfirmStatus confirmStatus;

    /**
     * 投产申请GUID:对应投产申请GUID
     */
    private Integer guidDelivery;

    /**
     * 投产清单GUID:
     */
    private Integer guidDeliveryList;

    /**
     * 补丁类型:JAR 输出为jar包
     * ECD 输出为ecd包
     * EPD 输出为epd
     * CFG 作为配置文件
     * DBV 作为数据库脚本（SQL、DDL等数据库版本脚本）
     */
    private String patchType;

    /**
     * 部署到
     */
    private String deployWhere;

}

