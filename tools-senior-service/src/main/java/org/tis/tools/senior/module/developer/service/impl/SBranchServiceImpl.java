package org.tis.tools.senior.module.developer.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tis.tools.senior.module.developer.dao.SBranchMapper;
import org.tis.tools.senior.module.developer.entity.SBranch;
import org.tis.tools.senior.module.developer.entity.SBranchMapping;
import org.tis.tools.senior.module.developer.entity.SDelivery;
import org.tis.tools.senior.module.developer.entity.enums.BranchForWhat;
import org.tis.tools.senior.module.developer.entity.enums.DeliveryResult;
import org.tis.tools.senior.module.developer.exception.DeveloperException;
import org.tis.tools.senior.module.developer.service.ISBranchMappingService;
import org.tis.tools.senior.module.developer.service.ISBranchService;
import org.tis.tools.senior.module.developer.service.ISDeliveryService;
import org.tis.tools.senior.module.developer.service.ISSvnKitService;
import org.tis.tools.senior.module.developer.util.DeveloperUtils;
import org.tmatesoft.svn.core.SVNException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * sBranch的Service接口实现类
 * 
 * @author Auto Generate Tools
 * @date 2018/06/20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SBranchServiceImpl extends ServiceImpl<SBranchMapper, SBranch> implements ISBranchService {

    @Autowired
    private ISBranchMappingService branchMappingService;

    @Autowired
    private ISSvnKitService svnKitService;

    @Autowired
    private ISDeliveryService deliveryService;

    @Override
    public void deleteBranchAndMapping(Integer guidBranch) {

        EntityWrapper<SBranchMapping> branchMappingEntityWrapper = new EntityWrapper<>();
        branchMappingEntityWrapper.eq(SBranchMapping.COLUMN_GUID_BRANCH,guidBranch);
        List<SBranchMapping> sBranchMappings = branchMappingService.selectList(branchMappingEntityWrapper);
        if(sBranchMappings.size() > 0){
            SBranchMapping sbm = sBranchMappings.get(0);

            EntityWrapper<SDelivery> deliveryEntityWrapper = new EntityWrapper<>();
            deliveryEntityWrapper.eq(SDelivery.COLUMN_GUID_WORKITEM,sbm.getGuidOfWhats());
            deliveryEntityWrapper.eq(SDelivery.COLUMN_DELIVERY_RESULT,DeliveryResult.APPLYING);
            if(deliveryService.selectList(deliveryEntityWrapper).size() > 0){
                throw new DeveloperException("不能删除分支，此分支关联的工作项有对应的投放申请在申请中！");
            }
            EntityWrapper<SDelivery> deliveryEntityWrapper1 = new EntityWrapper<>();
            deliveryEntityWrapper1.eq(SDelivery.COLUMN_GUID_PROFILES,sbm.getGuidOfWhats());
            deliveryEntityWrapper1.eq(SDelivery.COLUMN_DELIVERY_RESULT,DeliveryResult.APPLYING);
            if(deliveryService.selectList(deliveryEntityWrapper1).size() > 0){
                throw new DeveloperException("不能删除分支，此分支关联的运行环境有对应的投放申请在申请中！");
            }

            //删除对应的第三张关联表
            branchMappingService.deleteById(sbm.getGuid());
        }
        //删除分支guid对应的分支记录
        deleteById(guidBranch);
    }

    @Override
    public List<SBranch> selectNotAllotBranch() {
        List<SBranchMapping> sBranchMappings = branchMappingService.selectList(null);
        List<Integer> branchGuid = new ArrayList<>();
        for (SBranchMapping branchMapping:sBranchMappings){
            branchGuid.add(branchMapping.getGuidBranch());
        }
        if(branchGuid.size() == 0){
            throw new DeveloperException("没有查询到未被指配的分支");
        }
        EntityWrapper<SBranch> branchEntityWrapper = new EntityWrapper<>();
        branchEntityWrapper.notIn(SBranch.COLUMN_GUID,branchGuid);
        return selectList(branchEntityWrapper);
    }

    @Override
    public List<Map> selectListByForWhatIds(BranchForWhat forWhat, Collection ids) {
        String s = DeveloperUtils.inExpression(ids);
        return this.baseMapper.selectListByForWhatIds(forWhat.getValue().toString(), s);
    }

    @Override
    public void recordBranchTempRevision(Integer guidBranch) throws SVNException {
        SBranch sBranch = selectById(guidBranch);
        int lastRevision = svnKitService.getLastRevision(sBranch.getFullPath());
        sBranch.setCurrVersion(lastRevision);
        updateById(sBranch);
    }

    @Override
    public void recordBranchTempRevision(List<Integer> guidBranchs) throws SVNException {
        EntityWrapper<SBranch> branchEntityWrapper = new EntityWrapper<>();
        branchEntityWrapper.in(SBranch.COLUMN_GUID, guidBranchs);
        List<SBranch> sBranches = selectList(branchEntityWrapper);
        for (SBranch sBranch : sBranches) {
            int lastRevision = svnKitService.getLastRevision(sBranch.getFullPath());
            sBranch.setCurrVersion(lastRevision);
        }
        updateBatchById(sBranches);
    }

    @Override
    public void revertBranchRevision(Integer guidBranch) throws SVNException {
        SBranch sBranch = selectById(guidBranch);
        sBranch.setCurrVersion(sBranch.getLastVersion());
        updateById(sBranch);
    }

    @Override
    public void revertBranchRevision(List<Integer> guidBranchs) {
        EntityWrapper<SBranch> branchEntityWrapper = new EntityWrapper<>();
        branchEntityWrapper.in(SBranch.COLUMN_GUID, guidBranchs);
        List<SBranch> sBranches = selectList(branchEntityWrapper);
        for (SBranch sBranch : sBranches) {
            sBranch.setCurrVersion(sBranch.getLastVersion());
        }
        updateBatchById(sBranches);
    }

    @Override
    public void syncBranchRevision(Integer guidBranch) {
        SBranch sBranch = selectById(guidBranch);
        sBranch.setLastVersion(sBranch.getCurrVersion());
        updateById(sBranch);
    }

    @Override
    public void syncBranchRevision(List<Integer> guidBranchs) {
        EntityWrapper<SBranch> branchEntityWrapper = new EntityWrapper<>();
        branchEntityWrapper.in(SBranch.COLUMN_GUID, guidBranchs);
        List<SBranch> sBranches = selectList(branchEntityWrapper);
        for (SBranch sBranch : sBranches) {
            sBranch.setLastVersion(sBranch.getCurrVersion());
        }
        updateBatchById(sBranches);
    }

    @Override
    public Integer verificationUrl(String svnUrl) throws SVNException {
        return svnKitService.getLastRevision(svnUrl);
    }
}

