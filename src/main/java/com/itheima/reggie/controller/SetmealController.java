package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController  {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;
    /*
    * 新增套餐
    * */

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){//因为提交得是json形式得数据
        log.info("套餐信息：{}",setmealDto);

        setmealService.save(setmealDto);


        return R.success("新增套餐成功");
}


/*
* 分页查询
* */
    @GetMapping("/page")
    public R<Page>page(int page,int pageSize,String name){
        //分页构造器对象
        Page<Setmeal> pageInfo=new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage=new Page<>();


        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();

        //添加查询条件，跟据name进行模糊查询
        queryWrapper.like(name!=null,Setmeal::getName,name);
        //添加排序条件，跟据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

         setmealService.page(pageInfo, queryWrapper);

        //上述dtopage对象只是创建出来，还没赋值，这里使用对象拷贝

        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal> records=pageInfo.getRecords();

        List<SetmealDto> list=records.stream().map((item)->{
            SetmealDto setmealDto=new SetmealDto();
            //分类id
            Long categoryId= item.getCategoryId();
            //跟据分类id查询分类对象
            Category category = categoryService.getById(categoryId);

            if (category!=null){
                //分类名称
                String categoryName=category.getName();
                setmealDto.setCategoryName(categoryName);  //这一步就是把steamDto里的名字也改了
            }
            return setmealDto;
        }).collect(Collectors.toList());

//        List<SetmealDto> list=NULL 这个上面已经去获取了
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }



    /*
    * 删除套餐
    * */

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);

        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }


    /*
    *
    * 跟据条件查找套餐数据
    * */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){  //这个得去掉，前端穿的是键值对数据，而不是json数据
        LambdaQueryWrapper<Setmeal>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getId,setmeal.getId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
    //加个排序条件
    queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }






}
