package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
/*
 * 分类管理
 * */
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    /*
    * 新增菜品分类
    * */
@PostMapping
    public R<String> saveCaipin(@RequestBody Category category){
    log.info("新增菜品种类：{}",category.toString());
    categoryService.save(category);
    return R.success("新增种类添加成功！");
    }

    /*员工信息
     * 分頁查詢實現
     * */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        log.info("page= {},pageSize= {},name= {}",page,pageSize);

        //构造分页构造器
        Page pageInfo=new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper();

        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort);

        //执行查询
        categoryService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    /*
    * 根据id删除分类
    * */
    @DeleteMapping
    public R<String> delete(Long ids){    //前端的锅，还不好改js
        log.info("删除分类，id为：{}",ids);
        categoryService.removeById(ids);   //先不讲别的，直接删，稍后完善
        return R.success("分类删除成功");
    }

    /*
    * 跟据id修改分类信息
    * */

    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息：{}",category);
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }


@GetMapping("/list")
public R<List<Category>> list(Category category){
        //条件构造器
    LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
    //添加条件
    queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());

    //添加排序条件
    queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

    List<Category> list=categoryService.list(queryWrapper);
    return R.success(list);
}


}
