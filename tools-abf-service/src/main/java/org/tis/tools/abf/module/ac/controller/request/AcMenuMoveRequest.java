package org.tis.tools.abf.module.ac.controller.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotBlank;
import org.tis.tools.model.web.RestRequest;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Created by chenchao
 * Created on 2018/6/15 10:13
 */
@Data
@EqualsAndHashCode(callSuper = false )
public class AcMenuMoveRequest extends RestRequest {

    @NotBlank(message = "targetGuid不能为空")
    private String targetGuid;

    @NotBlank(message = "moveGuid不能为空")
    private String moveGuid;

    @NotNull(message = "order不能为空")
    private BigDecimal order;

}
