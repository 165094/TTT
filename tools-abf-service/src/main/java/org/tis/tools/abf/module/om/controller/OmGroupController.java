package org.tis.tools.abf.module.om.controller;

import com.baomidou.mybatisplus.plugins.Page;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tis.tools.abf.module.ac.entity.AcApp;
import org.tis.tools.abf.module.jnl.annotation.OperateLog;
import org.tis.tools.abf.module.jnl.entity.enums.OperateType;
import org.tis.tools.abf.module.om.controller.request.*;
import org.tis.tools.abf.module.om.entity.OmEmployee;
import org.tis.tools.abf.module.om.entity.OmGroup;
import org.tis.tools.abf.module.om.entity.OmPosition;
import org.tis.tools.abf.module.om.entity.enums.OmGroupType;
import org.tis.tools.abf.module.om.service.IOmEmpGroupService;
import org.tis.tools.abf.module.om.service.IOmGroupService;
import org.tis.tools.abf.module.om.service.IOmPositionService;
import org.tis.tools.core.exception.ToolsRuntimeException;
import org.tis.tools.core.web.controller.BaseController;
import org.tis.tools.model.common.ResultVO;
import org.tis.tools.core.web.vo.SmartPage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.groups.Default;
import java.util.List;

/**
 * omGroup的Controller类
 * 
 * @author ljhua
 * @date 2018/04/23
 */
@RestController
@RequestMapping("/omGroups")
public class OmGroupController extends BaseController<OmGroup>  {

    @Autowired
    private IOmGroupService omGroupService;

    @Autowired
    private IOmEmpGroupService omEmpGroupService;

    @Autowired
    private IOmPositionService omPositionService;

    /**
     * 条件查询工作组列表
     *
     * @param groupCode
     * @return
     */
    @GetMapping("/{groupCode}")
    public ResultVO detail(@PathVariable @NotBlank(message = "groupCode不能为空") String groupCode) {
        OmGroup omGroup = omGroupService.selectGroupByCode(groupCode);
        if (omGroup == null) {
            return ResultVO.error("404", "找不到对应记录或已经被删除！");
        }
        return ResultVO.success("查询成功!", omGroup);
    }

    /**
     * 查询所有工作组列表
     *
     * @return
     */
    @PostMapping("/list")
    public ResultVO list(@RequestBody @Validated SmartPage<OmGroup> page) {
        return  ResultVO.success("查询成功！", omGroupService.selectPage(getPage(page), getCondition(page)));
    }

    /**
     * 新增根工作组
     *
     * @param groupAddRequest
     * @return
     */
    @OperateLog(type = OperateType.ADD, desc = "新增根工作组")
    @PostMapping(value = "/root")
    public ResultVO addRoot(@RequestBody @Validated({OmGroupAddRequest.Root.class,Default.class}) OmGroupAddRequest groupAddRequest) {
        omGroupService.createGroup(groupAddRequest.getGroupType(),groupAddRequest.getGroupName(),groupAddRequest.getGuidOrg(),null);

        return ResultVO.success("新增成功！");
    }

    /**
     * 新增子工作组
     *
     * @param groupAddRequest
     * @return
     */
    @OperateLog(type = OperateType.ADD, desc = "新增子工作组")
    @PostMapping(value = "/child")
    public ResultVO addChild(@RequestBody @Validated(OmGroupAddRequest.Child.class) OmGroupAddRequest groupAddRequest) {
        omGroupService.createGroup(groupAddRequest.getGroupType(),groupAddRequest.getGroupName(),groupAddRequest.getGuidOrg(),groupAddRequest.getGuidParents());

        return ResultVO.success("新增成功！");
    }

    /**
     * 删除工作组
     *
     * @param groupCode
     * @return
     */
    @OperateLog(type = OperateType.DELETE, desc = "删除工作组")
    @DeleteMapping(value = "/{groupCode}")
    public ResultVO delete( @PathVariable @NotBlank(message = "groupCode不能为空") String groupCode) {
        omGroupService.deleteGroup(groupCode);
        return ResultVO.success("删除成功！");
    }

    /**
     * 生成工作组代码
     *
     * @param groupType
     * @return
     */
    @OperateLog(type = OperateType.ADD, desc = "生成工作组编号")
    @PostMapping("/{groupType}/initGroupCode")
    public ResultVO initGroupCode(@PathVariable @NotBlank(message = "groupType不能为空") OmGroupType groupType) {
        omGroupService.genGroupCode(groupType.getValue());
        return ResultVO.success("生成工作组编号成功！");
    }


    /**
     * 启用---注销工作组
     *
     * @param groupCode
     * @return
     */
    @OperateLog(type = OperateType.UPDATE, desc = "更改工作组状态")
    @PutMapping(value = "/{groupCode}/enable")
    public ResultVO enableGroup(@PathVariable @NotBlank(message = "groupCode不能为空") String groupCode) {
        omGroupService.cancelGroup(groupCode);
        return ResultVO.success("修改成功!");
    }

    /**
     * 启用---重启工作组
     *
     * @param groupCode
     * @param reenableChild
     * @return
     */
    @OperateLog(type = OperateType.UPDATE, desc = "更改工作组状态")
    @PutMapping(value = "/{groupCode}/reenable/{reenableChild}")
    public ResultVO reenableGroup(@PathVariable @NotBlank(message = "groupCode不能为空") String groupCode,
                                  @PathVariable @NotBlank(message = "groupCode不能为空") boolean reenableChild) {
        omGroupService.reenableGroup(groupCode,reenableChild);
        return ResultVO.success("修改成功!");
    }

    /**
     * 更新修改工作组
     *
     * @param omGroupUpdateRequest
     * @return
     */
    @OperateLog(type = OperateType.UPDATE, desc = "更新工作组") // 操作对象的关键值的键值名
    @PutMapping
    public ResultVO updateGroup(@RequestBody @Validated OmGroupUpdateRequest omGroupUpdateRequest) {
        OmGroup og = new OmGroup();
        og.setGroupCode(omGroupUpdateRequest.getGroupCode());
        og.setGroupName(omGroupUpdateRequest.getGroupName());
        og.setGroupDesc(omGroupUpdateRequest.getGroupDesc());
        og.setGuidOrg(omGroupUpdateRequest.getGuidOrg());

        omGroupService.updateGroup(og);
        return ResultVO.success("修改成功!");
    }

    /**
     * 生成下级岗位列表
     *
     * @param model
     * @param content
     * @param age
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/position")
    public ResultVO loadPosition(@RequestBody String content, String age, HttpServletRequest request,
                               HttpServletResponse response) {
//        try {
//            JSONObject jsonObj = JSONObject.parseObject(content);
//            String groupCode = jsonObj.getString("groupCode");
//            //TODO
//        } catch (ToolsRuntimeException e) {// TODO
//            AjaxUtils.ajaxJsonErrorMessage(response, e.getCode(), e.getMessage());
//            e.printStackTrace();
//        } catch (Exception e) {
//            AjaxUtils.ajaxJsonErrorMessage(response, "SYS_0001", e.getMessage());
//        }
        return null;
    }

    /**
     * 查询在此工作组的员工
     *
     * @param groupCode
     * @param page
     * @return
     */
    @PostMapping(value = "/{groupCode}/emp")
    public ResultVO loadempin(@PathVariable @NotBlank(message = "groupCode不能为空") String groupCode,
                              @RequestBody @Validated SmartPage<OmEmployee> page) {
        Page<OmEmployee> omEmployeePage = new Page<OmEmployee>(page.getPage().getCurrent(), page.getPage().getSize(),
                page.getPage().getOrderByField(), page.getPage().getAsc());
        Page<OmEmployee> empList = omGroupService.selectEmployee(groupCode, omEmployeePage);

        return ResultVO.success("查询成功!",empList);
    }

    /**
     * 加载不在此工作组的人员列表(同属同一机构)
     *
     * @param guidOrg
     * @param groupCode
     * @return
     */
    @GetMapping(value = "/{groupCode}/empNotIn/{guidOrg}")
    public ResultVO loadEmpNotIn(@PathVariable @NotBlank(message = "guidOrg不能为空") String guidOrg,
                                 @PathVariable @NotBlank(message = "groupCode不能为空") String groupCode) {

        List<OmEmployee> omEmpList = omGroupService.selectEmpNotInGroup(guidOrg, groupCode);
        return ResultVO.success("查询成功！",omEmpList);
    }

    /**
     * 新添人员
     *
     * @param omGroupAddEmpRequest
     * @return
     */
    @OperateLog(type = OperateType.UPDATE, desc = "工作组新添人员")
    @PostMapping(value = "/empGroup")
    public ResultVO addEmpGroup(@RequestBody @Validated OmGroupAddEmpRequest omGroupAddEmpRequest) {

        omEmpGroupService.insertGroupEmp(omGroupAddEmpRequest.getGroupCode(), omGroupAddEmpRequest.getGuidEmp());
        return ResultVO.success("新增成功！");
    }

    /**
     * 删除人员-工作组关联
     *
     * @param groupCode
     * @param guidEmp
     * @return
     */
    @OperateLog(type = OperateType.DELETE, desc = "为工作组删除员工")
    @DeleteMapping(value = "/{groupCode}/empGroup/{guidEmp}")
    public ResultVO deleteEmpGroup(@PathVariable @NotBlank(message = "groupCode不能为空")String groupCode,
                                   @PathVariable @NotBlank(message = "guidEmp不能为空")String guidEmp) {

        omEmpGroupService.deleteGroupEmp(groupCode,guidEmp);
        return ResultVO.success("删除成功！");
    }

    /**
     * 加载下级岗位列表
     *
     * @param groupCode
     * @return
     */
    @PostMapping(value = "/{groupCode}/positionIn")
    public ResultVO loadPositionIn(@PathVariable @NotBlank(message = "groupCode不能为空") String groupCode,
                                   @RequestBody @Validated SmartPage<OmPosition> page) {

        Page<OmPosition> omPositionPage = new Page<OmPosition>(page.getPage().getCurrent(), page.getPage().getSize(),
                page.getPage().getOrderByField(), page.getPage().getAsc());
        Page<OmPosition> pageList = omGroupService.selectPositionInGroup(groupCode,omPositionPage);
        return ResultVO.success("查询成功！",pageList);
    }

    /**
     * 加载下级岗位列表，不分页
     *
     * @param groupCode
     * @return
     */
    @GetMapping(value = "/{groupCode}/positionNotPage")
    public ResultVO loadPositionNotPage(@PathVariable @NotBlank(message = "groupCode不能为空") String groupCode) {

        List<OmPosition> omList = omGroupService.selectPositionInGroupNotPage(groupCode);
        return ResultVO.success("查询成功！",omList);
    }

    /**
     * 加载不在此工作组的岗位列表(同属同一机构)
     *
     * @param groupCode
     * @param page
     * @return
     */
    @PostMapping(value = "/{groupCode}/availablePosition")
    public ResultVO loadpositionNotin(@PathVariable @NotBlank(message = "groupCode不能为空") String groupCode,
                                      @RequestBody @Validated SmartPage<OmPosition> page) {

        Page<OmPosition> omPositionPage = new Page<OmPosition>(page.getPage().getCurrent(), page.getPage().getSize(),
                page.getPage().getOrderByField(), page.getPage().getAsc());

        Page<OmPosition> list = omGroupService.selectPositionNotInGroup(groupCode, omPositionPage);
        return ResultVO.success("查询成功！",list);
    }

    /**
     * 新添岗位
     *
     * @param omGroupPositionRequest
     * @return
     */
    @OperateLog(type = OperateType.ADD,desc = "为工作组新增岗位")
    @PostMapping(value = "/position")
    public ResultVO addGroupPosition(@RequestBody @Validated OmGroupPositionRequest omGroupPositionRequest) {


        omGroupService.insertGroupPosition(omGroupPositionRequest.getGroupCode(), omGroupPositionRequest.getOmPositionRequest());

        return ResultVO.success("新增成功！");
    }

    /**
     * 删除岗位
     *
     * @param positionGuid
     * @return
     */
    @OperateLog(type = OperateType.DELETE,desc = "为工作组删除岗位")
    @DeleteMapping(value = "/{groupCode}/position/{positionGuid}")
    public ResultVO deleteGroupPosition(@PathVariable @NotBlank(message = "groupCode不能为空") String groupCode,
                                        @PathVariable @NotBlank(message = "positionGuid不能为空") String positionGuid) {
        omGroupService.deleteGroupPosition(groupCode,positionGuid);
        return ResultVO.success("删除成功！");
    }


    /**
     * 查询工作组下的应用
     *
     * @param groupCode
     * @param page
     */
    @PostMapping(value = "/{groupCode}/app")
    public ResultVO selectApp(@PathVariable @NotBlank(message = "groupCode不能为空") String groupCode,
                           @RequestBody @Validated SmartPage<AcApp> page) throws ToolsRuntimeException{

        Page<AcApp> acAppPage = new Page<AcApp>(page.getPage().getCurrent(), page.getPage().getSize(),
                page.getPage().getOrderByField(), page.getPage().getAsc());

        Page<AcApp> list = omGroupService.selectApp(groupCode,acAppPage);
        return ResultVO.success("查询成功！",list);
    }

    /**
     * 查询可以为工作组添加的应用
     *
     * @param groupCode
     * @return
     * @throws ToolsRuntimeException
     */
    @GetMapping(value = "/{groupCode}/availableApp")
    public ResultVO selectNotInApp(@PathVariable @NotBlank(message = "groupCode不能为空") String groupCode) throws ToolsRuntimeException{

        List<AcApp> list = omGroupService.selectAppNotInGroup(groupCode);
        return ResultVO.success("查询成功！",list);
    }

    /**
     * 新增工作组-应用记录
     *
     * @param omGroupAddAppRequest
     * @return
     * @throws ToolsRuntimeException
     */
    @OperateLog(type = OperateType.ADD, desc = "为工作组添加应用") // 操作对象的关键值的键值名
    @PostMapping(value = "/app")
    public ResultVO addGroupApp(@RequestBody @Validated OmGroupAddAppRequest omGroupAddAppRequest) throws ToolsRuntimeException {
        omGroupService.addGroupApp(omGroupAddAppRequest.getAppGuidList(), omGroupAddAppRequest.getGroupCode());

        return ResultVO.success("新增成功！");
    }

    /**
     * 删除工作组-应用记录
     *
     * @param guidApp
     * @param groupCode
     * @return
     * @throws ToolsRuntimeException
     */
    @OperateLog(type = OperateType.DELETE, desc = "为工作组删除应用")
    @DeleteMapping(value = "/{groupCode}/app/{guidApp}")
    public ResultVO deleteGroupApp(@PathVariable @NotBlank(message = "guidApp不能为空")String guidApp,
                                   @PathVariable @NotBlank(message = "groupCode不能为空")String groupCode) throws ToolsRuntimeException{
        omGroupService.deleteGroupApp(groupCode, guidApp);
        return ResultVO.success("删除成功");
    }

    /**
     * 查询工作组树
     *
     * @param guid
     * @return
     */
    @GetMapping(value = "/{guid}/tree")
    public ResultVO selectGroupTree(@PathVariable @NotBlank(message = "guid不能为空")String guid){

        if (!"null".equals(guid)) {
            OmGroup omGroup = omGroupService.selectById(guid);
            if (omGroup == null) {
                return ResultVO.error("404", "找不到对应记录或已经被删除！");
            }
        }

        return ResultVO.success("查询成功",omGroupService.selectGroupTree(guid));
    }


}

