package org.tis.tools.abf.module.om.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.tis.tools.abf.module.ac.entity.AcApp;
import org.tis.tools.abf.module.ac.entity.AcRole;
import org.tis.tools.abf.module.ac.service.IAcAppService;
import org.tis.tools.abf.module.common.entity.Tree;
import org.tis.tools.abf.module.common.entity.enums.YON;
import org.tis.tools.abf.module.common.entity.vo.TreeDetail;
import org.tis.tools.abf.module.om.controller.request.OmPositionRequest;
import org.tis.tools.abf.module.om.entity.*;
import org.tis.tools.abf.module.om.entity.enums.OmGroupStatus;
import org.tis.tools.abf.module.om.entity.enums.OmGroupType;
import org.tis.tools.abf.module.om.entity.enums.OmPositionType;
import org.tis.tools.abf.module.om.entity.vo.OmPositionDetail;
import org.tis.tools.abf.module.om.exception.OMExceptionCodes;
import org.tis.tools.abf.module.om.exception.OrgManagementException;
import org.tis.tools.abf.module.om.service.*;
import org.springframework.transaction.annotation.Transactional;
import org.tis.tools.abf.module.om.dao.OmGroupMapper;
import org.tis.tools.abf.module.sys.service.ISysSeqnoService;
import org.tis.tools.core.utils.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.tis.tools.core.utils.BasicUtil.wrap;

/**
 * omGroup的Service接口实现类
 * 
 * @author ljhua
 * @date 2018/04/23
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OmGroupServiceImpl extends ServiceImpl<OmGroupMapper, OmGroup> implements IOmGroupService {

    @Autowired
    IOmOrgService omOrgService;
    @Autowired
    IOmEmpGroupService omEmpGroupService;
    @Autowired
    IOmEmployeeService omEmployeeService;
    @Autowired
    IOmGroupPositionService omGroupPositionService;
    @Autowired
    IOmPositionService omPositionService;
    @Autowired
    IOmGroupAppService omGroupAppService;
    @Autowired
    ISysSeqnoService seqnoRService;
    @Autowired
    IAcAppService acAppService;

    /**
     *
     * @param groupType
     * @return
     * @throws OrgManagementException
     */
    @Override
    public String genGroupCode(String groupType) throws OrgManagementException {
        if(groupType == null) {
            throw new OrgManagementException(OMExceptionCodes.LAKE_PARMS_FOR_GEN_GROUPCODE,new Object[]{"groupType"}) ;
        }
        /**
         * <pre>
         * 工作组代码规则：
         * 1.共10位+GROUP前缀；
         * 2.组成结构： 工作组类别(两位) + 序号(八位)
         * 序号：全行范围内职务数量顺序排号
         * </pre>
         */
        StringBuffer sb = new StringBuffer() ;
        //翻译类型-NORMAL=01 PROJECT=02 AFFAIR=03
        String type = "";
        if(groupType.equals("normal")){
            type = "01";
        }else if(groupType.equals("project")){
            type = "02";
        }else if(groupType.equals("affair")){
            type = "03";
        }else{
            throw new OrgManagementException(OMExceptionCodes.LAKE_PARMS_FOR_GEN_GROUPCODE,new Object[]{"groupType"}) ;
        }
        // 开始生成
        sb.append(type) ;
//		sb.append(toSeqNO(sequenceService.getNextSeqNo(BOSHGenGroupCode.class.getName()))) ;//五位机构顺序号
        sb.append(toSeqNO(seqnoRService.getNextSequence("GROUP_CODE", "工作组代码序号")));
        return "GROUP"+sb.toString();
    }

    @Override
    public OmGroup selectGroupByCode(String groupCode){

        OmGroup og = queryGroupByCode(groupCode);
        return og;
    }

    @Override
    public void createGroup(OmGroupType groupType, String groupName, String guidOrg, String parentGroupCode) throws OrgManagementException {
        //创建修改工作组序列对象
        OmGroup omGroup = new OmGroup();

        EntityWrapper<OmOrg> omOrgEntityWrapper = new EntityWrapper<>();
        omOrgEntityWrapper.eq(OmOrg.COLUMN_GUID, guidOrg);
        List<OmOrg> orgList = omOrgService.selectList(omOrgEntityWrapper);
        if (orgList.size() != 1) {
            throw new OrgManagementException(OMExceptionCodes.ORGANIZATION_NOT_EXIST_BY_GUID_ORG, wrap(guidOrg));
        }
        OmOrg org = orgList.get(0);
        OmGroup og = new OmGroup();
        //补充信息
        og.setGroupCode(genGroupCode(groupType.getValue()));
        og.setGroupStatus(OmGroupStatus.RUNNING);
        // 新增节点都先算叶子节点 Y
        og.setIsleaf(YON.YES);
        //开启时间
        og.setStartDate(new Date());
        // 新增时子节点数为0
        og.setSubCount(new BigDecimal(0));
        //TODO
        og.setUpdator("");
        //收集入参
        og.setGroupName(groupName);
        og.setGuidOrg(guidOrg);
        og.setGroupType(groupType);
        if(StringUtil.isEmpty(parentGroupCode)){
            // 补充工作组层次，根节点层次为 0
            og.setGroupLevel(new BigDecimal(0));
            // 补充父机构，根节点没有父机构
            og.setGuidParents(null);
            // 设置工作组序列,根工作组直接用guid
            og.setGroupSeq(og.getGuid());
            this.baseMapper.insert(og);

            omGroup.setGroupSeq(og.getGuid());
        }else{
            //新建子工作组
            //先获取父工作组信息
            EntityWrapper<OmGroup> omGroupEntityWrapper = new EntityWrapper<>();
            omGroupEntityWrapper.eq(OmGroup.COLUMN_GUID, parentGroupCode);
            List<OmGroup> ogList = this.baseMapper.selectList(omGroupEntityWrapper);
            if (ogList.size() != 1) {
                throw new OrgManagementException(OMExceptionCodes.GROUP_NOT_EXIST_BY_GROUP_CODE, wrap(parentGroupCode));
            }
            OmGroup parentOg = ogList.get(0);
            //更新父工作组信息
            parentOg.setIsleaf(YON.NO);
            int count = parentOg.getSubCount().intValue() + 1;
            parentOg.setSubCount(new BigDecimal(count));
            parentOg.setLastupdate(new Date());
            // 补充工作组层次
            int level = parentOg.getGroupLevel().intValue() + 1;
            og.setGroupLevel(new BigDecimal(level));
            // 补充父机构，根节点没有父机构
            og.setGuidParents(parentOg.getGuid());
            // 设置工作组序列,根工作组直接用guid
            og.setGroupSeq(parentOg.getGroupSeq() + "." + og.getGuid());
            this.baseMapper.insert(og);

            this.baseMapper.update(parentOg,omGroupEntityWrapper);

            //设置工作组序列
            omGroup.setGroupSeq(parentOg.getGroupSeq() + "." + og.getGuid());


        }
        EntityWrapper<OmGroup> omGroupEntity = new EntityWrapper<>();
        omGroupEntity.eq(OmGroup.COLUMN_GROUP_CODE, og.getGroupCode());

        List<OmGroup> omGroupList = this.baseMapper.selectList(omGroupEntity);
        if (omGroupList.size() != 1) {
            throw new OrgManagementException(OMExceptionCodes.GROUP_NOT_EXIST_BY_GROUP_CODE, wrap(og.getGroupCode()));
        }

        this.baseMapper.update(omGroup,omGroupEntity);
    }

    @Override
    public Page<OmGroup> queryAllGroup(Page<OmGroup> page) throws OrgManagementException {
        return selectPage(page,null);
    }

    @Override
    public OmGroup copyGroup(String fromGroupCode, String toOrgCode, String toParentGroupCode) throws OrgManagementException {
        return null;
    }

    @Override
    public OmGroup copyGroupDeep(String fromGroupCode, String toOrgCode, String toParentGroupCode, boolean copyChild, boolean copyApp, boolean copyPosition, boolean copyEmployee) throws OrgManagementException {
        return null;
    }

    @Override
    public OmGroup moveGroup(String groupCode, String fromParentGroupCode, String toParentGroupCode) throws OrgManagementException {
        return null;
    }

    @Override
    public void updateGroup(OmGroup newOmGroup) throws OrgManagementException {
        EntityWrapper<OmGroup> omGroupEntityWrapper = new EntityWrapper<>();
        omGroupEntityWrapper.eq(OmGroup.COLUMN_GROUP_CODE,newOmGroup.getGroupCode());
        this.baseMapper.update(newOmGroup,omGroupEntityWrapper);
    }

    @Override
    public void deleteGroup(String groupCode) throws OrgManagementException {
        //校验状态
        OmGroup og = queryGroupByCode(groupCode);
        if (OmGroupStatus.RUNNING.equals(og.getGroupStatus())) {
            throw new OrgManagementException(OMExceptionCodes.FAILURE_DELETE_RERUNNING_GROUP);
        }
        EntityWrapper<OmGroup> omGroupEntityWrapper = new EntityWrapper<>();
        omGroupEntityWrapper.like(OmGroup.COLUMN_GROUP_SEQ, og.getGroupSeq());

        this.baseMapper.delete(omGroupEntityWrapper);
        //删除后修改工作组结束时间
        OmGroup omGroup = new OmGroup();
        omGroup.setGroupCode(groupCode);
        omGroup.setEndDate(new Date());
        updateGroup(omGroup);
    }

    @Override
    public void cancelGroup(String groupCode) throws OrgManagementException {
        OmGroup og = queryGroupByCode(groupCode);
        EntityWrapper<OmGroup> omGroupEntityWrapper = new EntityWrapper<>();
        omGroupEntityWrapper.like(OmGroup.COLUMN_GROUP_SEQ, og.getGroupSeq());
        omGroupEntityWrapper.notIn(OmGroup.COLUMN_GROUP_CODE,groupCode);
        List<OmGroup> ogList = this.baseMapper.selectList(omGroupEntityWrapper);

        if(ogList.size() > 0){
            for (OmGroup omGroup : ogList) {
                if (omGroup.getGroupStatus().equals(OmGroupStatus.RUNNING)) {
                    throw new OrgManagementException(OMExceptionCodes.GROUP_CHILDS_IS_RUNNING,wrap(groupCode));
                }
            }
        }
        og.setGroupStatus(OmGroupStatus.ANCEL);

        updateGroup(og);
    }

    @Override
    public void reenableGroup(String groupCode, boolean reenableChild) throws OrgManagementException {
        //调用方法中已经有参数检验
        OmGroup og = queryGroupByCode(groupCode);
        if(og.getGroupStatus().equals(OmGroupStatus.RUNNING)){
            throw new OrgManagementException(OMExceptionCodes.GROUP_CHILDS_IS_RUNNING,wrap(og.getGroupName()));
        }else{
            og.setGroupStatus(OmGroupStatus.RUNNING);
            this.baseMapper.updateById(og);
            if(reenableChild){
                EntityWrapper<OmGroup> omGroupEntityWrapper = new EntityWrapper<>();
                omGroupEntityWrapper.like(OmGroup.COLUMN_GROUP_SEQ,og.getGuid());
                OmGroup omGroup = new OmGroup();
                omGroup.setGroupStatus(OmGroupStatus.RUNNING);
                this.baseMapper.update(omGroup,omGroupEntityWrapper);
            }
        }

    }

    @Override
    public OmGroup queryGroupByCode(String groupCode) {
        EntityWrapper<OmGroup> omGroupEntityWrapper = new EntityWrapper<>();
        omGroupEntityWrapper.eq(OmGroup.COLUMN_GROUP_CODE, groupCode);
        List<OmGroup> ogList = this.baseMapper.selectList(omGroupEntityWrapper);
        if (ogList.size() != 1) {
            throw new OrgManagementException(OMExceptionCodes.GROUP_NOT_EXIST_BY_GROUP_CODE, wrap(groupCode));
        }
        OmGroup og = ogList.get(0);
        return og;
    }

    @Override
    public Page<OmGroup> selectRootGroup(Page<OmGroup> page) {
        EntityWrapper<OmGroup> omGroupEntityWrapper = new EntityWrapper<>();
        omGroupEntityWrapper.isNull(OmGroup.COLUMN_GUID_PARENTS);
        return selectPage(page, omGroupEntityWrapper);
    }

    @Override
    public Page<OmGroup> selectChildGroup(String groupCode, Page<OmGroup> page) {
        OmGroup og = queryGroupByCode(groupCode);
        EntityWrapper<OmGroup> omGroupEntityWrapper = new EntityWrapper<>();
        omGroupEntityWrapper.eq(OmGroup.COLUMN_GUID_PARENTS, og.getGuid());
        return selectPage(page, omGroupEntityWrapper);
    }

    @Override
    public List<OmPositionDetail> selectPosition(String groupCode) {
        return null;
    }

    @Override
    public Page<OmEmployee> selectEmployee(String groupCode, Page<OmEmployee> page) {
        OmGroup og = queryGroupByCode(groupCode);
        EntityWrapper<OmEmpGroup> omEmpGroupEntityWrapper = new EntityWrapper<>();
        omEmpGroupEntityWrapper.eq(OmEmpGroup.COLUMN_GUID_GROUP, og.getGuid());
        List<OmEmpGroup> oegList = omEmpGroupService.selectList(omEmpGroupEntityWrapper);
        Page<OmEmployee> empList = new Page<>();
        if (oegList.size() == 0) {
            return empList;
        } else {
            List<String> guidList = new ArrayList<>();
            for (OmEmpGroup oeg : oegList) {
                guidList.add(oeg.getGuidEmp());
            }
            EntityWrapper<OmEmployee> omEmployeeEntityWrapper = new EntityWrapper<>();
            omEmployeeEntityWrapper.in(OmEmployee.COLUMN_GUID, guidList);
            return omEmployeeService.selectPage(page,omEmployeeEntityWrapper);
        }
    }

    @Override
    public List<OmEmployee> selectEmpNotInGroup(String guidOrg, String groupCode) {
        return this.baseMapper.selectOrgEmpNotInGroup(guidOrg,groupCode);
    }

    @Override
    public Page<OmPosition> selectPositionInGroup(String groupCode,Page<OmPosition> page) {
        //查询方法中自带参数校验
        OmGroup og = queryGroupByCode(groupCode);
        EntityWrapper<OmGroupPosition> omGroupPositionEntityWrapper = new EntityWrapper<>();
        omGroupPositionEntityWrapper.eq(OmGroupPosition.COLUMN_GUID_GROUP, og.getGuid());
        Page<OmPosition> opPage = new Page<>();
        List<OmGroupPosition> ogpList = omGroupPositionService.selectList(omGroupPositionEntityWrapper);
        if (ogpList.isEmpty()) {
            return opPage;
        }
        List<String> posGuidList = new ArrayList<>();
        for (OmGroupPosition ogp : ogpList) {
            posGuidList.add(ogp.getGuidPosition());
        }
        EntityWrapper<OmPosition> omPositionEntityWrapper = new EntityWrapper<>();
        omPositionEntityWrapper.in(OmPosition.COLUMN_GUID, posGuidList);
        return omPositionService.selectPage(page, omPositionEntityWrapper);
    }

    @Override
    public List<OmPosition> selectPositionInGroupNotPage(String groupCode) {
        //查询方法中自带参数校验
        OmGroup og = queryGroupByCode(groupCode);
        EntityWrapper<OmGroupPosition> omGroupPositionEntityWrapper = new EntityWrapper<>();
        omGroupPositionEntityWrapper.eq(OmGroupPosition.COLUMN_GUID_GROUP, og.getGuid());
        List<OmPosition> opList = new ArrayList<>();
        List<OmGroupPosition> ogpList = omGroupPositionService.selectList(omGroupPositionEntityWrapper);
        if (ogpList.isEmpty()) {
            return opList;
        }
        List<String> posGuidList = new ArrayList<>();
        for (OmGroupPosition ogp : ogpList) {
            posGuidList.add(ogp.getGuidPosition());
        }
        EntityWrapper<OmPosition> omPositionEntityWrapper = new EntityWrapper<>();
        omPositionEntityWrapper.in(OmPosition.COLUMN_GUID, posGuidList);
        return omPositionService.selectList(omPositionEntityWrapper);
    }

    @Override
    public Page<OmPosition> selectPositionNotInGroup(String groupCode, Page<OmPosition> page) {
        OmGroup og = queryGroupByCode(groupCode);

        //查询方法中自带参数校验
        EntityWrapper<OmGroupPosition> omGroupPositionEntityWrapper = new EntityWrapper<>();
        omGroupPositionEntityWrapper.eq(OmGroupPosition.COLUMN_GUID_GROUP, og.getGuid());
        List<OmGroupPosition> ogpList = omGroupPositionService.selectList(omGroupPositionEntityWrapper);
        if (ogpList.isEmpty()) {
            return new Page<>();
        }
        List<String> posGuidList = new ArrayList<>();
        for (OmGroupPosition ogp : ogpList) {
            posGuidList.add(ogp.getGuidPosition());
        }

        EntityWrapper<OmPosition> omPositionEntityWrapper = new EntityWrapper<>();
        omPositionEntityWrapper.eq(OmPosition.COLUMN_GUID_ORG, og.getGuidOrg());
        omPositionEntityWrapper.eq(OmPosition.COLUMN_POSITION_TYPE, "01");
        omPositionEntityWrapper.notIn(OmPosition.COLUMN_GUID, posGuidList);

        return omPositionService.selectPage(page, omPositionEntityWrapper);
    }

    @Override
    public List<AcRole> selectRole(String groupCode) {
        return null;
    }

    @Override
    public Page<OmGroup> selectAllchild(String groupCode, Page<OmGroup> page) {
        OmGroup parentog = queryGroupByCode(groupCode);
        EntityWrapper<OmGroup> omGroupEntityWrapper = new EntityWrapper<>();
        omGroupEntityWrapper.eq(OmGroup.COLUMN_GUID_PARENTS, parentog.getGuid());
        return selectPage(page,omGroupEntityWrapper);
    }

    @Override
    public void insertGroupPosition(String groupCode, OmPositionRequest omPositionRequest) {
        omPositionRequest.setPositionType(OmPositionType.WORKINGGROUP);
        if(null == omPositionRequest.getGuidParents() || "" == omPositionRequest.getGuidParents()){
            //添加根岗位
            omPositionService.addRoot(omPositionRequest);
        }else{
            //添加子岗位
            omPositionService.addChild(omPositionRequest);
        }

        String positionGuid= selectPositionGuidByCode(omPositionRequest.getPositionCode());
        OmGroup og = queryGroupByCode(groupCode);
        if(og == null){
            throw new OrgManagementException(OMExceptionCodes.GROUP_NOT_EXIST_BY_GROUP_CODE, wrap(groupCode));
        }
        OmGroupPosition ogp = new OmGroupPosition();
        ogp.setGuidGroup(og.getGuid());
        ogp.setGuidPosition(positionGuid);
        omGroupPositionService.insert(ogp);
    }

    @Override
    public void deleteGroupPosition(String groupCode, String positionGuid) {
        OmGroup omGroup = queryGroupByCode(groupCode);
        EntityWrapper<OmGroupPosition> omGroupPositionEntityWrapper = new EntityWrapper<>();
        omGroupPositionEntityWrapper.eq(OmGroupPosition.COLUMN_GUID_POSITION,positionGuid);
        omGroupPositionEntityWrapper.eq(OmGroupPosition.COLUMN_GUID_GROUP,omGroup.getGuid());
        omGroupPositionService.delete(omGroupPositionEntityWrapper);
    }

    @Override
    public String selectPositionGuidByCode(String positionCode) {
        EntityWrapper<OmPosition> omPositionEntityWrapper = new EntityWrapper<>();
        omPositionEntityWrapper.eq(OmPosition.COLUMN_POSITION_CODE,positionCode);
        List<OmPosition> opList = omPositionService.selectList(omPositionEntityWrapper);
        if (opList.size() != 1) {
            throw new OrgManagementException(OMExceptionCodes.POSITANIZATION_NOT_EXIST_BY_POSIT_CODE, wrap(positionCode));
        }
        OmPosition parentOg = opList.get(0);

        return parentOg.getGuid();
    }

    @Override
    public Page<OmGroup> selectByGroupName(String groupName,Page<OmGroup> page) {
        EntityWrapper<OmGroup> omGroupEntityWrapper = new EntityWrapper<>();
        omGroupEntityWrapper.eq(OmGroup.COLUMN_GROUP_NAME, groupName);
        Page<OmGroup> list =  selectPage(page, omGroupEntityWrapper);
        return list;
    }

    @Override
    public Page<AcApp> selectApp(String groupCode, Page<AcApp> page) {
        OmGroup og = queryGroupByCode(groupCode);
        EntityWrapper<OmGroupApp> OmGroupAppEntityWrapper = new EntityWrapper<>();
        OmGroupAppEntityWrapper.eq(OmGroupApp.COLUMN_GUID_GROUP, og.getGuid());
        List<OmGroupApp> ogpList = omGroupAppService.selectList(OmGroupAppEntityWrapper);
        if (ogpList.size() == 0) {
            return new Page<AcApp>();
        }
        List<String> guidList = new ArrayList<>();
        for (OmGroupApp ogp : ogpList) {
            guidList.add(ogp.getGuidApp());
        }
        EntityWrapper<AcApp> AcAppEntityWrapper = new EntityWrapper<>();
        AcAppEntityWrapper.in(AcApp.COLUMN_GUID, guidList);
        Page<AcApp> acApp = acAppService.selectPage(page,AcAppEntityWrapper);
        return acApp;
    }

    @Override
    public List<AcApp> selectAppNotInGroup(String groupCode) {

         return this.baseMapper.selectAppNotInGroup(groupCode);
    }

    @Override
    public void addGroupApp(List<String> appGuidList, String groupCode) {
        List<OmGroupApp> ogpList = new ArrayList<>();
        OmGroup og = queryGroupByCode(groupCode);
        if (og == null) {
            throw new OrgManagementException(OMExceptionCodes.GROUP_NOT_EXIST_BY_GROUP_CODE, wrap(groupCode));
        }
        EntityWrapper<OmGroupApp> omEmpGroupEntityWrapper = new EntityWrapper<>();
        omEmpGroupEntityWrapper.eq(OmGroupApp.COLUMN_GUID_GROUP, og.getGuid());
        omEmpGroupEntityWrapper.in(OmGroupApp.COLUMN_GUID_APP, appGuidList);
        List<OmGroupApp> oegList = omGroupAppService.selectList(omEmpGroupEntityWrapper);
        if (oegList.size() > 0) {
            throw new OrgManagementException(OMExceptionCodes.IS_EXIST_BY_GROUP_CREAT_APP, wrap(groupCode));
        }

        for (String guidApp : appGuidList){
            OmGroupApp ogp = new OmGroupApp();
            ogp.setGuidApp(guidApp);
            ogp.setGuidGroup(og.getGuid());
            ogpList.add(ogp);
        }
        omGroupAppService.insertBatch(ogpList);
    }

    @Override
    public void deleteGroupApp(String groupCode,String guidApp) {
        OmGroup omGroup = queryGroupByCode(groupCode);
        EntityWrapper<OmGroupApp> omGroupAppEntityWrapper = new EntityWrapper<>();
        omGroupAppEntityWrapper.eq(OmGroupApp.COLUMN_GUID_APP, guidApp);
        omGroupAppEntityWrapper.eq(OmGroupApp.COLUMN_GUID_GROUP, omGroup.getGuid());
        omGroupAppService.delete(omGroupAppEntityWrapper);
    }

    @Override
    public TreeDetail selectGroupTree(String guid) {

        TreeDetail treeDetail = new TreeDetail();

        try {
            //创建子机构的list
            List<Tree> list = new ArrayList<Tree>();


            if("null".equals(guid)){
                Wrapper<OmGroup> wrapper = new EntityWrapper<OmGroup>();
                wrapper.isNull(OmGroup.COLUMN_GUID_PARENTS);

                List<OmGroup> queryList = selectList(wrapper);

                for (OmGroup omGroupQuery: queryList) {
                    Tree tree = new Tree();
                    tree.setGuid(omGroupQuery.getGuid());
                    tree.setCode(omGroupQuery.getGroupCode());
                    tree.setLabel(omGroupQuery.getGroupName());
                    tree.setIsleaf(omGroupQuery.getIsleaf());
                    list.add(tree);
                }

            }else {
                OmGroup omGroup = selectById(guid);

                //查询子机构字典
                Wrapper<OmGroup> wrapper = new EntityWrapper<OmGroup>();
                wrapper.eq(OmGroup.COLUMN_GUID_PARENTS,guid);

                List<OmGroup> queryList = selectList(wrapper);

                for (OmGroup omGroupQuery: queryList) {
                    Tree treeSon = new Tree();
                    treeSon.setGuid(omGroupQuery.getGuid());
                    treeSon.setCode(omGroupQuery.getGroupCode());
                    treeSon.setLabel(omGroupQuery.getGroupName());
                    treeSon.setIsleaf(omGroupQuery.getIsleaf());
                    list.add(treeSon);
                }

                //收集查询出来的结果
                treeDetail.setGuid(omGroup.getGuid());
                treeDetail.setCode(omGroup.getGroupCode());
                treeDetail.setLabel(omGroup.getGroupName());
                treeDetail.setIsleaf(omGroup.getIsleaf());
            }
            treeDetail.setChildren(list);
        }catch (Exception e){
            e.printStackTrace();
            throw new OrgManagementException(OMExceptionCodes.FAILURE_WHEN_QUERY_GROUP_TREE,wrap(e.getMessage()));
        }

        return treeDetail;
    }


    /**
     * 把当前工作组数量转为8位字符串，不足部分以0左补齐
     * @param totalOrgCount
     * @return
     */
    private Object toSeqNO(long totalOrgCount) {

        String t = String.valueOf(totalOrgCount) ;

        return org.tis.tools.core.utils.StringUtil.leftPad(t, 8, '0');

    }
}

