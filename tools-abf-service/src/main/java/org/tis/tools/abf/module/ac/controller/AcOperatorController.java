package org.tis.tools.abf.module.ac.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tis.tools.abf.module.ac.controller.request.AcOperatorAddRequest;
import org.tis.tools.abf.module.ac.controller.request.AcOperatorUpdateGrop;
import org.tis.tools.abf.module.ac.entity.AcOperator;
import org.tis.tools.abf.module.ac.service.IAcOperatorService;
import org.tis.tools.abf.module.jnl.annotation.OperateLog;
import org.tis.tools.abf.module.jnl.entity.enums.OperateType;
import org.tis.tools.core.web.controller.BaseController;
import org.tis.tools.core.web.vo.ResultVO;
import org.tis.tools.core.web.vo.SmartPage;

/**
 * acOperator的Controller类
 * 
 * @author Auto Generate Tools
 * @date 2018/04/23
 */
@RestController
@RequestMapping("/acOperators")
public class AcOperatorController extends BaseController<AcOperator>  {

    @Autowired
    private IAcOperatorService acOperatorService;

    /**
     * @param request
     *
     * @return 新增操作员
     *
     */
    @OperateLog(type = OperateType.ADD,desc = "新增操作员")
    @PostMapping
    public ResultVO add(@RequestBody @Validated AcOperatorAddRequest request) {
        acOperatorService.addAcOperator(request);
        AcOperator acOperator = new AcOperator();
        EntityWrapper<AcOperator> acOperatorEntityWrapper = new EntityWrapper<>();
        acOperatorEntityWrapper.eq(AcOperator.COLUMN_USER_ID,request.getUserId());
        AcOperator acOperator1 = acOperatorService.selectOne(acOperatorEntityWrapper);
        return ResultVO.success("新增成功！");
    }

    /**
     *
     * @param acOperator
     * @return 修改操作员
     */
    @OperateLog(type = OperateType.UPDATE,desc = "修改操作员")
    @PutMapping
    public ResultVO update(@RequestBody @Validated({AcOperatorUpdateGrop.class}) AcOperator acOperator) {
        AcOperator acOperatorQue  = acOperatorService.selectById(acOperator.getGuid());
        if (acOperatorQue == null) {
            return ResultVO.error("404", "找不到对应记录或已经被删除！");
        }
        acOperatorService.updateAcOperatorByCondition(acOperator);
        AcOperator acOperatorQue1  = acOperatorService.selectById(acOperator.getGuid());
        return ResultVO.success("修改成功！",acOperatorQue1);
    }

    /**
     *
     * @param id
     * @return 删除操作员结果
     */
    @OperateLog(type = OperateType.DELETE,desc = "删除操作员")
    @DeleteMapping("/{id}")
    public ResultVO delete(@PathVariable @NotBlank(message = "id不能为空") String id) {
        AcOperator acOperator  = acOperatorService.selectById(id);
        if (acOperator == null) {
            return ResultVO.error("404", "找不到对应记录或已经被删除！");
        }
        acOperatorService.deleteById(id);
        return ResultVO.success("删除成功");
    }

    /**
     *  根据ID查询操作员
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResultVO detail(@PathVariable @NotBlank(message = "id不能为空") String id) {
        EntityWrapper<AcOperator> acOperatorEntityWrapper = new EntityWrapper<>();
        acOperatorEntityWrapper.eq(AcOperator.COLUMN_USER_ID,id);
        AcOperator acOperator = acOperatorService.selectOne(acOperatorEntityWrapper);
        if (acOperator == null) {
            return ResultVO.error("404", "找不到对应记录或已经被删除！");
        }
        return ResultVO.success("查询成功", acOperator);
    }
    
    @PostMapping("/list")
    public ResultVO list(@RequestBody @Validated SmartPage<AcOperator> page) {
        return  ResultVO.success("查询成功", acOperatorService.selectPage(getPage(page), getCondition(page)));
    }
    
}

