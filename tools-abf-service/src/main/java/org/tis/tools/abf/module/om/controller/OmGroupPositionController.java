package org.tis.tools.abf.module.om.controller;

import org.springframework.validation.annotation.Validated;
import org.tis.tools.core.web.controller.BaseController;
import org.tis.tools.core.web.vo.SmartPage;
import org.tis.tools.abf.module.om.service.IOmGroupPositionService;
import org.tis.tools.abf.module.om.entity.OmGroupPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.hibernate.validator.constraints.NotBlank;
import org.tis.tools.model.common.ResultVO;

/**
 * omGroupPosition的Controller类
 * 
 * @author Auto Generate Tools
 * @date 2018/04/23
 */
@RestController
@RequestMapping("/omGroupPosition")
public class OmGroupPositionController extends BaseController<OmGroupPosition>  {

    @Autowired
    private IOmGroupPositionService omGroupPositionService;

    @PostMapping("/add")
    public ResultVO add(@RequestBody @Validated OmGroupPosition omGroupPosition) {
        omGroupPositionService.insert(omGroupPosition);
        return ResultVO.success("新增成功！");
    }
    
    @PutMapping
    public ResultVO update(@RequestBody @Validated OmGroupPosition omGroupPosition) {
        omGroupPositionService.updateById(omGroupPosition);
        return ResultVO.success("修改成功！");
    }
    
    @DeleteMapping("/{id}")
    public ResultVO delete(@PathVariable @NotBlank(message = "id不能为空") String id) {
        omGroupPositionService.deleteById(id);
        return ResultVO.success("删除成功");
    }
    
    @GetMapping("/{id}")
    public ResultVO detail(@PathVariable @NotBlank(message = "id不能为空") String id) {
        OmGroupPosition omGroupPosition = omGroupPositionService.selectById(id);
        if (omGroupPositionService == null) {
            return ResultVO.error("404", "找不到对应记录或已经被删除！");
        }
        return ResultVO.success("查询成功", omGroupPosition);
    }
    
    @PostMapping("/list")
    public ResultVO list(@RequestBody @Validated SmartPage<OmGroupPosition> page) {
        return  ResultVO.success("查询成功", omGroupPositionService.selectPage(getPage(page), getCondition(page)));
    }
    
}

