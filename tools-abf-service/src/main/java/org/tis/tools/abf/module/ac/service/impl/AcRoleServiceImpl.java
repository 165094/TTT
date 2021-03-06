package org.tis.tools.abf.module.ac.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tis.tools.abf.module.ac.dao.AcRoleMapper;
import org.tis.tools.abf.module.ac.entity.AcOperatorRole;
import org.tis.tools.abf.module.ac.entity.AcRole;
import org.tis.tools.abf.module.ac.entity.AcRoleFunc;
import org.tis.tools.abf.module.ac.exception.AcExceptionCodes;
import org.tis.tools.abf.module.ac.exception.AcRoleManagementException;
import org.tis.tools.abf.module.ac.service.IAcOperatorRoleService;
import org.tis.tools.abf.module.ac.service.IAcRoleFuncService;
import org.tis.tools.abf.module.ac.service.IAcRoleService;
import org.tis.tools.abf.module.common.entity.Tree;
import org.tis.tools.abf.module.common.entity.enums.YON;
import org.tis.tools.abf.module.common.entity.vo.TreeDetail;
import org.tis.tools.core.exception.i18.ExceptionCodes;
import org.tis.tools.core.utils.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.tis.tools.core.utils.BasicUtil.wrap;

/**
 * acRole的Service接口实现类
 * 
 * @author Auto Generate Tools
 * @date 2018/04/23
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AcRoleServiceImpl extends ServiceImpl<AcRoleMapper, AcRole> implements IAcRoleService {

    @Autowired
    IAcRoleFuncService acRoleFuncService;
    @Autowired
    IAcRoleService acRoleService;

    @Autowired
    private IAcOperatorRoleService acOperatorRoleService;


    /**
     * <pre>
     * 根据条件查询角色
     * </pre>
     *
     * @param acRole
     *
     * @return 匹配的角色
     *
     * @throws AcRoleManagementException
     */
    @Override
    public AcRole queryByCondition(AcRole acRole) throws AcRoleManagementException{
        EntityWrapper<AcRole> acRoleEntityWrapper = new EntityWrapper<>();
        if(StringUtils.isNotBlank(acRole.getRoleCode())){
            acRoleEntityWrapper.eq(AcRole.COLUMN_ROLE_CODE, acRole.getRoleCode());
        }
        if(StringUtils.isNotBlank(acRole.getRoleName())){
            acRoleEntityWrapper.eq(AcRole.COLUMN_ROLE_NAME, acRole.getRoleName());
        }
        try {
            AcRole acRole1 = selectOne(acRoleEntityWrapper);
            return acRole1;
        }catch (Exception e){
            throw new AcRoleManagementException(ExceptionCodes.FAILURE_WHEN_QUERY,wrap("ACROLE",e));
        }

    }

    /**
     * <pre>
     * 新建一个角色
     *
     *
     * </pre>
     *
     * @param roleCode
     *            角色代码
     * @param roleName
     *            角色名称
     * @param enabled
     *            是否启用
     * @param roleDesc
     *            角色描述
     * @return 新建角色信息结果
     * @throws AcRoleManagementException
     */
    @Override
    public boolean createAcRole(String roleCode, String roleName, YON enabled, String roleDesc,String roleGroup) throws
            AcRoleManagementException {
        AcRole acRole = new AcRole();
        acRole.setRoleCode(roleCode);
        acRole.setRoleName(roleName);
        acRole.setEnabled(YON.YES);
        acRole.setRoleDesc(roleDesc);
        acRole.setRoleGroup(roleGroup);
        try {
            boolean bole = insert(acRole);
            return bole;
        }catch (Exception e){
            e.printStackTrace();
            throw new AcRoleManagementException(ExceptionCodes.FAILURE_WHEN_INSERT,
                    wrap(e.getMessage())
            );
        }

    }
    /**
     * <pre>
     * 根据Guid修改一个角色
     *
     *
     * </pre>
     *
     * @param acRole
     *
     * @return 修改角色信息结果
     * @throws AcRoleManagementException
     */
    @Override
    public boolean updateAcRole(AcRole acRole) throws AcRoleManagementException {
        EntityWrapper<AcRole> acRoleEntityWrapper = new EntityWrapper<>();
        acRoleEntityWrapper.eq(AcRole.COLUMN_ROLE_CODE,acRole.getRoleCode());
        try {
            boolean bolen = update(acRole,acRoleEntityWrapper);
            return bolen;
        }catch (Exception e){
            throw new AcRoleManagementException(ExceptionCodes.FAILURE_WHEN_UPDATE,wrap("ACROLE",e));
        }

    }
    /**
     * <pre>
     * 根据roleCode删除一个角色
     *
     *
     * </pre>
     *
     * @param id
     *
     * @return 删除结果
     * @throws AcRoleManagementException
     */
    @Override
    public boolean deleteByRoleCode(String id) throws AcRoleManagementException {

        try{

            //删除掉权限集(角色)功能对应关系数据
            Wrapper<AcRoleFunc> wrapper = new EntityWrapper<AcRoleFunc>();
            wrapper.eq(AcRoleFunc.COLUMN_GUID_ROLE,id);
            List<AcRoleFunc> list = acRoleFuncService.selectList(wrapper);
            if (0 != list.size()){
                acRoleFuncService.delete(wrapper);
            }

            //删除操作员与权限集（角色）对应关系数据
            Wrapper<AcOperatorRole> wrapperOper = new EntityWrapper<AcOperatorRole>();
            wrapperOper.eq(AcOperatorRole.COLUMN_GUID_ROLE,id);
            List<AcOperatorRole> listRole = acOperatorRoleService.selectList(wrapperOper);
            if (0 != listRole.size()){
                acOperatorRoleService.delete(wrapperOper);
            }

            //删除掉角色
            boolean bolen = deleteById(id);
            return bolen;
        }catch (Exception e ){
            throw new AcRoleManagementException(AcExceptionCodes.FAILURE_WHEN_DELETE_AC_ROLE,wrap(e.getMessage()));
        }

    }


    /**
     * 查询角色树
     *
     * @return
     * @throws AcRoleManagementException
     */
    @Override
    public List<TreeDetail> queryRoleTree() throws AcRoleManagementException {

        //查询角色中有几种分组
        List<String> distinctGroupList = this.baseMapper.queryDistinctRoleGroup();
        //为了避免查询回来的数组还有重复值
        Set<String> set = new HashSet<String>(distinctGroupList);
        distinctGroupList.clear();
        distinctGroupList.addAll(set);

        List<TreeDetail> list = new ArrayList<TreeDetail>();
        for(String groupName : distinctGroupList){
            if (StringUtil.isNotNull(groupName)){
                    TreeDetail treeDetail = new TreeDetail();
                    treeDetail.setLabel(groupName);

                    //查询该分组下对应的角色
                    Wrapper<AcRole> wrapper = new EntityWrapper<AcRole>();
                    wrapper.eq(AcRole.COLUMN_ROLE_GROUP,groupName);

                    List<Tree> treeList = new ArrayList<Tree>();

                    List<AcRole> roleList = selectList(wrapper);
                    //将查询出来的角色放到列表中
                    for (AcRole acRole : roleList){
                        if (null != acRole){
                            Tree tree = new Tree();
                            tree.setGuid(acRole.getGuid());
                            tree.setLabel(acRole.getRoleName());
                            treeList.add(tree);
                        }
                    }

                    treeDetail.setChildren(treeList);
                    list.add(treeDetail);
            }
        }
        return list;
    }
}

