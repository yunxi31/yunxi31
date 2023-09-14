package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
/*
* 自定义的元数据对象处理器
* */
//公共字段，自动填充，统一对这些字段进行处理，避免了重复代码
@Component
@Slf4j
public class MyMetaObjecthandler implements MetaObjectHandler {
   /*
   * 插入操作，自动填充
   * */
    @Override
    public void insertFill(MetaObject metaObject) {  //报接口500错,这个是因为没在实体类employee中加入@TableField注解
        log.info("公共字段自动填充[insert]。。。");
        log.info(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getCurrentId());//  暂时处理不了，先写死，有问题，以后的·表都设置为空了
        metaObject.setValue("updateUser",BaseContext.getCurrentId());
    }
/*
* 更新操作，自动填充
* */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充[update]。。。");
        log.info(metaObject.toString());

        long id=Thread.currentThread().getId();
        log.info("线程id为：{}",id);

        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("updateUser",BaseContext.getCurrentId());
    }
}
