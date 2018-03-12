package org.tis.tools.starter.multidatasource.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tis.tools.starter.multidatasource.DataSourceContextHolder;
import org.tis.tools.starter.multidatasource.DynamicDataSource;
import org.tis.tools.starter.multidatasource.annotion.DataSource;
import org.tis.tools.starter.mybatisplus.config.DruidProperties;

import java.lang.reflect.Method;

/**
 * 多数据源切换的aop
 *
 * @author fengshuonan
 * @author Shiyunlai
 * @since 2017年3月5日 上午10:22:16
 */
@Aspect
@Component
@ConditionalOnProperty(prefix = "tools", name = "multi-datasource-open", havingValue = "true")
public class MultiSourceExAop implements Ordered {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DruidProperties druidProperties;

    @Pointcut(value = "@annotation(org.tis.tools.starter.multidatasource.annotion.DataSource)")
    private void cut() {

    }

    /**
     * 根据注解中指明的名称，选择数据源
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("cut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        Signature signature = point.getSignature();
        MethodSignature methodSignature = null;
        if (!(signature instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        methodSignature = (MethodSignature) signature;

        Object target = point.getTarget();
        Method currentMethod = target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());

        DataSource datasource = currentMethod.getAnnotation(DataSource.class);
        if (datasource != null) {
            if( !DataSourceContextHolder.contains(datasource.name()) ){
                throw new IllegalArgumentException("执行"+currentMethod.toString()+"方式时，找不到指定的数据源"+datasource.name());
            }
            log.debug("使用指定数据源：" + datasource.name());
            DataSourceContextHolder.setDataSourceType(datasource.name());
        } else {
            log.debug("使用默认数据源：" + druidProperties.getDatasourceName());
            DataSourceContextHolder.setDataSourceType(druidProperties.getDatasourceName());
        }

        try {
            return point.proceed();
        } finally {
            log.debug("清空数据源信息！");
            DataSourceContextHolder.clearDataSourceType();
        }
    }


    /**
     * aop的顺序要早于spring的事务
     */
    @Override
    public int getOrder() {
        return -1;
    }

}
