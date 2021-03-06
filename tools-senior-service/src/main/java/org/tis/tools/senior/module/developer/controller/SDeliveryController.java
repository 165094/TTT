package org.tis.tools.senior.module.developer.controller;

import com.baomidou.mybatisplus.plugins.Page;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tis.tools.core.web.controller.BaseController;
import org.tis.tools.core.web.vo.SmartPage;
import org.tis.tools.model.common.ResultVO;
import org.tis.tools.senior.module.core.web.util.ShiroUtil;
import org.tis.tools.senior.module.developer.controller.request.DeliveredNewProfilesRequest;
import org.tis.tools.senior.module.developer.controller.request.DeliveryOutExeclRequest;
import org.tis.tools.senior.module.developer.controller.request.MergeDeliveryRequest;
import org.tis.tools.senior.module.developer.controller.request.SDeliveryUpdateRequest;
import org.tis.tools.senior.module.developer.entity.SDelivery;
import org.tis.tools.senior.module.developer.entity.SSvnAccount;
import org.tis.tools.senior.module.developer.entity.vo.DeliveryWorkitemDetail;
import org.tis.tools.senior.module.developer.exception.DeveloperException;
import org.tis.tools.senior.module.developer.service.ISDeliveryService;
import org.tmatesoft.svn.core.SVNException;

import javax.validation.constraints.NotNull;
import java.text.ParseException;


/**
 * sDelivery的Controller类
 * 
 * @author Auto Generate Tools
 * @date 2018/06/20
 */
@RestController
@RequestMapping("/deliveries")
@Validated
public class SDeliveryController extends BaseController<SDelivery>  {

    @Autowired
    private ISDeliveryService sDeliveryService;

    @PostMapping
    public ResultVO add(@RequestBody @Validated SDelivery sDelivery) {
        sDeliveryService.insert(sDelivery);
        return ResultVO.success("新增成功！");
    }
    
    @PutMapping
    public ResultVO update(@RequestBody @Validated SDelivery sDelivery) {
        sDeliveryService.updateById(sDelivery);
        return ResultVO.success("修改成功！");
    }
    
    @GetMapping("/{guid}")
    public ResultVO detail(@PathVariable @NotBlank(message = "投放申请的guid不能为空") String guid) {
        SDelivery sDelivery = sDeliveryService.selectById(guid);
        if (sDeliveryService == null) {
            return ResultVO.error("404", "找不到对应记录或已经被删除！");
        }
        return ResultVO.success("查询成功", sDelivery);
    }

    /**
     * 查询登录用户的所有投放申请
     * @param page
     * @return
     */
    @PostMapping("/list")
    public ResultVO list(@RequestBody @Validated SmartPage<DeliveryWorkitemDetail> page) {

        SSvnAccount svnAccount = ShiroUtil.getUser();
        if(svnAccount == null){
            throw new DeveloperException("尚未登录，请登录后再试！");
        }
        Page<DeliveryWorkitemDetail> deliveryDetailPage = new Page<DeliveryWorkitemDetail>
                (page.getPage().getCurrent(), page.getPage().getSize(),
                        page.getPage().getOrderByField(), page.getPage().getAsc());

        return  ResultVO.success("查询成功", sDeliveryService.getDeliveryAll(deliveryDetailPage,getWrapper(page.getCondition()),svnAccount));
    }

    /**
     * 获取合并投放清单信息
     * @param request
     * @return
     */
    @PostMapping("/merge/info")
    public ResultVO mergeInfo(@RequestBody @Validated MergeDeliveryRequest request) {
        return ResultVO.success(sDeliveryService.getMergeInfo(request, ShiroUtil.getUser().getUserId()));
    }


    /**
     * 合并投放（开发人员使用）
     * 1、选择多条“投放申请”，合并为一个新的投放申请；
     * 2、合并的投放申请都是成功的；
     * 3、合并的投放申请都来自同一个运行环境；
     * @return
     */
    @PostMapping("/merge")
    public ResultVO mergeDelivery(@RequestBody @Validated MergeDeliveryRequest request) {
        sDeliveryService.mergeDelivery(request, ShiroUtil.getUser().getUserId());
        return ResultVO.success("申请合并投放成功！");
    }

    @PutMapping("/{id}/merge")
    public ResultVO merge(@PathVariable @NotBlank(message = "id不能为空") String id) {
        sDeliveryService.merge(id);
        return ResultVO.success("确认已合并！");
    }

    /**
     * 查询一条投放申请中的工程名
     * @param guidDelivery
     * @return
     */
    @GetMapping("/{guidDelivery}/projectName")
    public ResultVO projectName(@PathVariable @NotBlank(message = "id不能为空") String guidDelivery){

        return ResultVO.success("查询成功",sDeliveryService.selectDeliveryProName(guidDelivery));
    }

    /**
     * 查询工作项所要追加的投放申请集合
     * @param workitemGuid
     * @return
     */
    @GetMapping("/{workitemGuid}/addTo")
    public ResultVO addToDelivery(@PathVariable @NotNull(message = "工作项id不能为空")Integer workitemGuid) throws ParseException {

        return ResultVO.success("查询成功！", sDeliveryService.selectAddToDelivery(workitemGuid));
    }

    /**
     * 删除投放申请及投放的代码
     * @param guidDelivery
     * @return
     */
    @DeleteMapping("/{guidDelivery}")
    public ResultVO deleteDeliveryAndDeliveryList(@PathVariable @NotNull(message = "投放申请id不能为空")
                                                              Integer guidDelivery) throws SVNException {

        sDeliveryService.deleteDeliveryAndDeliveryList(guidDelivery);
        return ResultVO.success("删除成功");
    }

    /**
     * 根据投放申请guid查询所投放的代码集合
     * @return
     */
    @GetMapping("/{guidDelivery}/deliveryLists")
    public ResultVO deliveryListDetail(@PathVariable @NotNull(message = "投放申请id不能为空")Integer guidDelivery){
        return ResultVO.success("查询成功",sDeliveryService.selectDeliveryListByGuidDelivery(guidDelivery));
    }

    /**
     * 查询导出的投放申请
     * @param request
     * @return
     */
    @PostMapping("/excels")
    public ResultVO selectOutDelivery(@RequestBody @Validated DeliveryOutExeclRequest request){

        return ResultVO.success("查询成功",sDeliveryService.selectDeliveryOutExecl(request));
    }

    /**
     * 修改投放的时间及打包窗口
     * @return
     */
    @PutMapping("/deliveryTimePackTime")
    public ResultVO updateDeliveryTimePackTime(@RequestBody @Validated SDeliveryUpdateRequest request) throws ParseException {

        sDeliveryService.updateDelivery(request);
        return ResultVO.success("修改成功");
    }

    /**
     * 查询运行环境的详情及时间窗口的验证
     * @return
     */
    @GetMapping("/{guidDelivery}/profileDateilVerify")
    public ResultVO profileDateilVerify(@PathVariable @NotNull(message = "投放申请id不能为空")Integer guidDelivery) throws ParseException {

        return ResultVO.success("查询成功",sDeliveryService.selectProfileDeteilVerify(guidDelivery));
    }

    /**
     * 将投放申请投放到新环境中
     * @return
     */
    @PostMapping("/newProfiles")
    public ResultVO newProfiles(@RequestBody @Validated DeliveredNewProfilesRequest request){
        return ResultVO.success("添加成功！",sDeliveryService.deliveredNewProfiles(request));
    }
}

