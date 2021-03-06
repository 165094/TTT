package org.tis.tools.senior.module.developer.controller.request;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.tis.tools.model.web.RestRequest;

/**
 * description:
 *
 * @author zhaoch
 * @date 2018/7/30
 **/
@Data
public class ProfileAddBranchRequest extends RestRequest {

    @NotBlank(message = "分支路径不能为空！")
    private String fullPath;

    @NotBlank(message = "分支说明不能为空")
    private String branchFor;

}
