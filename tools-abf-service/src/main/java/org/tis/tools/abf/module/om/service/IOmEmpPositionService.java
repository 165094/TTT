package org.tis.tools.abf.module.om.service;

import com.baomidou.mybatisplus.service.IService;
import org.tis.tools.abf.module.om.controller.request.OmEmpPositionRequest;
import org.tis.tools.abf.module.om.entity.OmEmpPosition;
import org.tis.tools.abf.module.om.exception.OrgManagementException;

/**
 * omEmpPosition的Service接口类
 * 
 * @author Auto Generate Tools
 * @date 2018/04/23
 */
public interface IOmEmpPositionService extends IService<OmEmpPosition>  {

    /**
     * 新增
     * @param omEmpPositionRequest
     * @throws OrgManagementException
     */
    void add(OmEmpPositionRequest omEmpPositionRequest)throws OrgManagementException;

    /**
     * 根据员工和岗位ID删除岗位员工
     * @param guidEmp
     * @param guidPosition
     */
    void deleteByEmpPositionId(String guidEmp,String guidPosition)throws OrgManagementException;

}

