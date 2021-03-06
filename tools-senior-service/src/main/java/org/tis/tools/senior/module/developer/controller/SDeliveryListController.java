package org.tis.tools.senior.module.developer.controller;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tis.tools.senior.module.core.web.util.ShiroUtil;
import org.tis.tools.core.web.controller.BaseController;
import org.tis.tools.model.common.ResultVO;
import org.tis.tools.core.web.vo.SmartPage;
import org.tis.tools.senior.module.developer.controller.request.DeliveryListAndDeliveryAddRequest;
import org.tis.tools.senior.module.developer.controller.request.DeliveryListSuperadditionRequest;
import org.tis.tools.senior.module.developer.entity.SDeliveryList;
import org.tis.tools.senior.module.developer.entity.SSvnAccount;
import org.tis.tools.senior.module.developer.service.ISDeliveryListService;
import org.tmatesoft.svn.core.SVNException;

/**
 * sDeliveryList的Controller类
 * 
 * @author Auto Generate Tools
 * @date 2018/06/20
 */
@RestController
@RequestMapping("/sDeliveryList")
public class SDeliveryListController extends BaseController<SDeliveryList>  {

    @Autowired
    private ISDeliveryListService sDeliveryListService;


    @PostMapping("/add")
    public ResultVO add(@RequestBody @Validated SDeliveryList sDeliveryList) {
        sDeliveryListService.insert(sDeliveryList);
        return ResultVO.success("新增成功！");
    }
    
    @PutMapping
    public ResultVO update(@RequestBody @Validated SDeliveryList sDeliveryList) {
        sDeliveryListService.updateById(sDeliveryList);
        return ResultVO.success("修改成功！");
    }
    
    @DeleteMapping("/{id}")
    public ResultVO delete(@PathVariable @NotBlank(message = "id不能为空") String id) {
        sDeliveryListService.deleteById(id);
        return ResultVO.success("删除成功");
    }
    
    @GetMapping("/{id}")
    public ResultVO detail(@PathVariable @NotBlank(message = "id不能为空") String id) {
        SDeliveryList sDeliveryList = sDeliveryListService.selectById(id);
        if (sDeliveryListService == null) {
            return ResultVO.error("404", "找不到对应记录或已经被删除！");
        }
        return ResultVO.success("查询成功", sDeliveryList);
    }
    
    @PostMapping("/list")
    public ResultVO list(@RequestBody @Validated SmartPage<SDeliveryList> page) {
        return  ResultVO.success("查询成功", sDeliveryListService.selectPage(getPage(page), getCondition(page)));
    }

    /**
     * 整理提交历史展示
     *
     * @param branchGuid 分支guid
     * @return
     */
    @GetMapping("/{branchGuid}/history")
    public ResultVO assembleDelivery(@PathVariable @NotBlank(message = "分支guid不能为空") String branchGuid) throws SVNException {
        return ResultVO.success("查询成功",sDeliveryListService.assembleDelivery(branchGuid));
    }

    /**
     *
     * 添加投放申请以及投产代码清单
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/deliveryAndDeliveryList")
    public ResultVO addDelivery(@RequestBody @Validated DeliveryListAndDeliveryAddRequest request) throws Exception {

        SSvnAccount sSvnAccount = ShiroUtil.getUser();
        return ResultVO.success("添加成功",sDeliveryListService.addDeliveryList(request,sSvnAccount.getUserId()));
    }

    /**
     * 追加投放申请
     * @return
     */
    @PostMapping("/superadditionDeliverylist")
    public ResultVO superadditionDeliverylist(@RequestBody @Validated DeliveryListSuperadditionRequest request) throws SVNException {

        return ResultVO.success("添加成功",sDeliveryListService.addToDeliveryList(request));
    }

    
}

