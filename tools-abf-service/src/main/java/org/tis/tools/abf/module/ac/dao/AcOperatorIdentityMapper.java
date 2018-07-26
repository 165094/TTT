package org.tis.tools.abf.module.ac.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.tis.tools.abf.module.ac.entity.AcOperatorIdentity;

/**
 * acOperatorIdentity的Mapper类
 * 
 * @author Auto Generate Tools
 * @date 2018/04/23
 */
public interface AcOperatorIdentityMapper extends BaseMapper<AcOperatorIdentity>  {

    void setDefaultIdentity(@Param("guid") String guid , @Param("guidOperator") String guidOperator);

}

