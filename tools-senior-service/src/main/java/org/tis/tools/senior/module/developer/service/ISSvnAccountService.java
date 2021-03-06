package org.tis.tools.senior.module.developer.service;

import com.baomidou.mybatisplus.service.IService;
import org.tis.tools.senior.module.developer.entity.SSvnAccount;

import java.util.List;

/**
 * sSvnAccount的Service接口类
 * 
 * @author Auto Generate Tools
 * @date 2018/06/20
 */
public interface ISSvnAccountService extends IService<SSvnAccount>  {

    /**
     * 获取所有用户
     * @return
     */
    List<SSvnAccount> selectAllAccount();

}

