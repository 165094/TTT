package org.tis.tools.abf.module.ac.controller;

import org.springframework.validation.annotation.Validated;
import org.tis.tools.core.web.controller.BaseController;
import org.tis.tools.core.web.vo.SmartPage;
import org.tis.tools.abf.module.ac.entity.AcDatascope;
import org.springframework.beans.factory.annotation.Autowired;
import org.tis.tools.abf.module.ac.service.IAcDatascopeService;
import org.springframework.web.bind.annotation.*;
import org.hibernate.validator.constraints.NotBlank;
import org.tis.tools.model.common.ResultVO;

/**
 * acDatascope的Controller类
 * 
 * @author Auto Generate Tools
 * @date 2018/04/23
 */
@RestController
@RequestMapping("/acDatascope")
public class AcDatascopeController extends BaseController<AcDatascope>  {

    @Autowired
    private IAcDatascopeService acDatascopeService;

    @PostMapping("/add")
    public ResultVO add(@RequestBody @Validated AcDatascope acDatascope) {
        acDatascopeService.insert(acDatascope);
        return ResultVO.success("新增成功！");
    }
    
    @PutMapping
    public ResultVO update(@RequestBody @Validated AcDatascope acDatascope) {
        acDatascopeService.updateById(acDatascope);
        return ResultVO.success("修改成功！");
    }
    
    @DeleteMapping("/{id}")
    public ResultVO delete(@PathVariable @NotBlank(message = "id不能为空") String id) {
        acDatascopeService.deleteById(id);
        return ResultVO.success("删除成功");
    }
    
    @GetMapping("/{id}")
    public ResultVO detail(@PathVariable @NotBlank(message = "id不能为空") String id) {
        AcDatascope acDatascope = acDatascopeService.selectById(id);
        if (acDatascopeService == null) {
            return ResultVO.error("404", "找不到对应记录或已经被删除！");
        }
        return ResultVO.success("查询成功", acDatascope);
    }
    
    @PostMapping("/list")
    public ResultVO list(@RequestBody @Validated SmartPage<AcDatascope> page) {
        return  ResultVO.success("查询成功", acDatascopeService.selectPage(getPage(page), getCondition(page)));
    }
    
}

