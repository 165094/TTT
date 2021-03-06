package org.tis.tools.senior.module.developer.controller.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.tis.tools.core.entity.enums.CommonEnumDeserializer;
import org.tis.tools.model.web.RestRequest;
import org.tis.tools.senior.module.developer.entity.enums.BranchType;

import javax.validation.constraints.NotNull;

/**
 * description:
 *
 * @author zhaoch
 * @date 2018/7/30
 **/
@Data
public class WorkItemAddBranchRequest  extends RestRequest {

    @NotNull(message = "分支的类型不能为空！")
    @JSONField(deserializeUsing = CommonEnumDeserializer.class)
    private BranchType branchType;

    @NotBlank(message = "分支说明不能为空")
    private String branchFor;

}
