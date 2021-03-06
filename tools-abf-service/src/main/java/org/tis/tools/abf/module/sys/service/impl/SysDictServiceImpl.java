package org.tis.tools.abf.module.sys.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tis.tools.abf.module.sys.controller.request.SysDictDefaultValueRequest;
import org.tis.tools.abf.module.sys.dao.SysDictMapper;
import org.tis.tools.abf.module.sys.entity.SysDict;
import org.tis.tools.abf.module.sys.entity.SysDictItem;
import org.tis.tools.abf.module.sys.entity.vo.SysDictDetail;
import org.tis.tools.abf.module.sys.exception.SYSExceptionCodes;
import org.tis.tools.abf.module.sys.exception.SysManagementException;
import org.tis.tools.abf.module.sys.service.ISysDictItemService;
import org.tis.tools.abf.module.sys.service.ISysDictService;
import org.tis.tools.core.exception.i18.ExceptionCodes;

import java.util.ArrayList;
import java.util.List;

import static org.tis.tools.core.utils.BasicUtil.wrap;
/**
 * sysDict的Service接口实现类
 *
 * @author Auto Generate Tools
 * @date 2018/04/23
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysDictServiceImpl  extends ServiceImpl<SysDictMapper,SysDict> implements ISysDictService {
    @Autowired
    private ISysDictItemService iSysDictItemService;
    /**
     * @param sysDict
     * @return
     * @throws SysManagementException
     * */
    @Override
    public SysDict addDict(SysDict sysDict) throws SysManagementException {
        if(StringUtils.isNotBlank(sysDict.getGuidParents())){

            SysDict sysDictNew = selectById(sysDict.getGuidParents());
            if (sysDictNew == null){
                throw new SysManagementException(ExceptionCodes.NOT_FOUND_WHEN_QUERY,
                        wrap(SYSExceptionCodes.NOT_FOUND_SYS_DICT_WITH_GUID,sysDict.getGuidParents())
                );
            }
        }
        insert(sysDict);
        return sysDict;
    }
    /**
     * @param id
     * @return
     * @throws SysManagementException
     * */
    @Override
    public SysDict queryDictDetail(String id) throws SysManagementException {
        return null;
    }
    /**
     * @param id
     * @return
     * @throws SysManagementException
     * */
    @Override
    public List<SysDict> queryDict(String id) throws SysManagementException {
        EntityWrapper<SysDict> wrapper = new EntityWrapper<>();
        wrapper.eq(SysDict.COLUMN_DICT_KEY,id);
        return selectList(wrapper);
    }
    /**
     * @param sysDict
     * @return
     * @throws SysManagementException
     * */
    @Override
    public SysDict editSysDict(SysDict sysDict) throws SysManagementException {
        EntityWrapper<SysDict> wrapper = new EntityWrapper<>();
        wrapper.eq(SysDict.COLUMN_GUID,sysDict.getGuid());
        if(selectOne(wrapper) == null){
            throw new SysManagementException(
                    ExceptionCodes.NOT_FOUND_WHEN_QUERY,
                    wrap(SysDictItem.COLUMN_GUID,sysDict.getGuid()),sysDict.getGuid());
        }
        update(sysDict,wrapper);
        return sysDict;
    }
    /**
     * @param id
     * @return
     * @throws SysManagementException
     * */
    @Override
    public SysDict deleteDict(String id) throws SysManagementException {

        //删除该节点对应的子节点数据
        deleteAllChild(id);

        return null;
    }

    public void deleteAllChild(String id) throws SysManagementException{

        Wrapper<SysDict> wrapper = new EntityWrapper<SysDict>();
        wrapper.eq(SysDict.COLUMN_GUID_PARENTS,id);
        try {
            //获取子列表
            List<SysDict> list = selectList(wrapper);

            if (0 == list.size() || null == list){
                deleteById(id);
                deleteItem(id);
            }else {
                for (SysDict sysDict : list){
                    if (sysDict != null){
                        deleteAllChild(sysDict.getGuid());
                        deleteItem(id);
                    }
                }
                deleteById(id);
                deleteItem(id);
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new SysManagementException(SYSExceptionCodes.FAILURE_WHEN_DELETE_SYS_DICT,wrap(e.getMessage()));
        }
    }

    public void deleteItem(String id) throws SysManagementException{
        try {
            Wrapper<SysDictItem> wrapper = new EntityWrapper<SysDictItem>();
            wrapper.eq(SysDictItem.COLUMN_GUID_DICT,id);
            List<SysDictItem> list = iSysDictItemService.selectList(wrapper);
            if (0 != list.size()){
               iSysDictItemService.delete(wrapper);
            }
        }catch (Exception e){
            throw new SysManagementException(SYSExceptionCodes.FAILURE_WHEN_DELETE_SYS_DICT_ITEM,wrap(e.getMessage()));
        }
    }


    @Override
    public Page<SysDict> querySysDicts(Page<SysDict> page, Wrapper<SysDict> wrapper){
        return selectPage(page,wrapper);
    }

    /**
     * 查询所有的父业务字典,并不分页
     *
     * @return
     * @throws SysManagementException
     */
    @Override
    public List<SysDict> queryParentList() throws SysManagementException {

        Wrapper<SysDict> wrapper = new EntityWrapper<SysDict>();
        wrapper.ne(SysDict.COLUMN_GUID_PARENTS,"");
        List<SysDict> listSon = selectList(wrapper);


        Wrapper<SysDict> wrapper1 = new EntityWrapper<SysDict>();
        List<SysDict> listAll = selectList(wrapper1);

        List<SysDict> listParent = new ArrayList<SysDict>();

        for (SysDict sysDictAll :listAll){
            String GuidAll = sysDictAll.getGuid();
            Boolean  isexist = false;
            for (SysDict sysDictSon : listSon){
                if (GuidAll.equals(sysDictSon.getGuid())){
                    isexist = true;
                    break;
                }
            }
            if (!isexist){
                listParent.add(sysDictAll);
            }
        }
        return listParent;
    }

    /**
     * @param  id
     * @return
     * @throws SysManagementException
     * */
    @Override
    public SysDict queryOneSysDictByGuid(String id) throws SysManagementException {
        EntityWrapper<SysDict> wrapper = new EntityWrapper<>();
        wrapper.eq(SysDict.COLUMN_GUID,id);
        return selectOne(wrapper);
    }

    /**
     * @param  sysDict
     * @return
     * @throws SysManagementException
     * */
    @Override
    public List<SysDict> dictKeyQuery(SysDict sysDict) throws SysManagementException {
        EntityWrapper<SysDict> wrapper = new EntityWrapper<>();
        if(sysDict.getDictKey()!= null && sysDict.getDictKey()!= "" ){
            wrapper.eq(SysDict.COLUMN_DICT_KEY,sysDict.getDictKey());
        }
        if(sysDict.getDictName()!=null && sysDict.getDictName()!= ""){
            wrapper.eq(SysDict.COLUMN_DICT_NAME,sysDict.getDictName());
        }
        List<SysDict> list = selectList(wrapper);
        return list;
    }
    /**
     * @param  id
     * @return
     * @throws SysManagementException
     * */
    @Override
    public SysDict querySysDictByGuid(String id) throws SysManagementException {
        EntityWrapper<SysDict> wrapper = new EntityWrapper<>();
        wrapper.eq(SysDict.COLUMN_GUID,id);
        SysDict sysDict = selectOne(wrapper);
        return sysDict;
    }

    /**
     * 查询业务字典的树结构
     */
    @Override
    public SysDictDetail queryDictTree(String id) throws SysManagementException {

            SysDictDetail sysDictDetail = new SysDictDetail();

        try {
            //查询该业务字典
            SysDict sysDictOne = selectById(id);

            List<Object> list = new ArrayList<Object>();

            //查询业务字典对应的子业务字典
            Wrapper<SysDict> wrapper = new EntityWrapper<SysDict>();
            wrapper.eq(SysDict.COLUMN_GUID_PARENTS,id);

            List<SysDict> listDict = selectList(wrapper);

            //查询业务字典的字典项
            Wrapper<SysDictItem> wrapperItem = new EntityWrapper<SysDictItem>();
            wrapperItem.eq(SysDictItem.COLUMN_GUID_DICT,id);

            List<SysDictItem> listDictItem = iSysDictItemService.selectList(wrapperItem);

            //如果listDict和listDictItem非空,则将其内容set到
            for (SysDict sysDict: listDict) {
                list.add(sysDict);
            }
            for (SysDictItem sysDictItem: listDictItem) {
                list.add(sysDictItem);
            }

            sysDictDetail.setGuid(sysDictOne.getGuid());
            sysDictDetail.setDictKey(sysDictOne.getDictKey());
            sysDictDetail.setDictName(sysDictOne.getDictName());
            sysDictDetail.setChildren(list);
        }catch (Exception e){
            e.printStackTrace();
            throw new SysManagementException(SYSExceptionCodes.NOT_FOUND_SYS_DICT_ITEM_WITH_GUID,
                    wrap(e.getMessage())
                    );
        }
        return sysDictDetail;
    }


    @Override
    public SysDict setDefaultValue(SysDictDefaultValueRequest sysDictDefaultValueRequest) throws SysManagementException {

        SysDict sysDictQuery = selectById(sysDictDefaultValueRequest.getGuid());

        //判断被设置为默认字典项的子业务字典或字典项是否存在
        //判断的标志位
        boolean isexist = false;

        //查询字典项为子业务字典
        Wrapper<SysDict> wrapperDict = new EntityWrapper<SysDict>();
        wrapperDict.eq(SysDict.COLUMN_GUID_PARENTS,sysDictDefaultValueRequest.getGuid());
        List<SysDict> listDict = selectList(wrapperDict);

        //查询字典项为字典项
        Wrapper<SysDictItem> wrapperDictItem = new EntityWrapper<SysDictItem>();
        wrapperDictItem.eq(SysDictItem.COLUMN_GUID_DICT,sysDictDefaultValueRequest.getGuid());
        List<SysDictItem> listDictItem = iSysDictItemService.selectList(wrapperDictItem);

        for (SysDict sysDict :listDict){
            if (sysDictDefaultValueRequest.getDefaultValue().equals(sysDict.getGuid())){
                isexist = true;
                break;
            }
        }
        for (SysDictItem sysDictItem :listDictItem){
            if (sysDictDefaultValueRequest.getDefaultValue().equals(sysDictItem.getGuid())){
                isexist = true;
                break;
            }
        }

        if (isexist == true){
            //设置默认值
            sysDictQuery.setDefaultValue(sysDictDefaultValueRequest.getDefaultValue());
            updateById(sysDictQuery);
        }else{
            //抛出异常
            throw new SysManagementException(SYSExceptionCodes.NOT_FOUND_SYS_DICT_ITEM_WITH_GUID,wrap("找不到默认值对应的字典项",sysDictDefaultValueRequest.getDefaultValue()));
        }

        return sysDictQuery;
    }
}
