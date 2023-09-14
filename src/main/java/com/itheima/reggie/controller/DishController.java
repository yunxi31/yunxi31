package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
@Autowired
    private DishService dishService;
@Autowired
private DishFlavorService dishFlavorService;
@Autowired
private CategoryService categoryService;
@PostMapping
public R<String> save(@RequestBody DishDto dishDto){
    log.info(dishDto.toString());
   dishService.saveWithFlavor(dishDto);
   return R.success("新增菜品成功");
}


/*
*菜品信息分页查询                       没看懂
* */
@GetMapping("/page")
public R<Page> page(int page, int pageSize,String name){
    log.info("page= {},pageSize= {},name= {}",page,pageSize,name);

    //构造分页构造器
    Page<Dish> pageInfo=new Page(page,pageSize);
    Page<DishDto> dishDtoPage=new Page<>();
    //构造条件构造器
    LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper();
    //添加过滤条件
    queryWrapper.like(name !=null,Dish::getName,name);
    //添加排序条件
    queryWrapper.orderByAsc(Dish::getUpdateTime);

    //执行分页查询
    dishService.page(pageInfo,queryWrapper);

    //对象拷贝            用这是干啥得啊？
   BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
    List<Dish> records=pageInfo.getRecords();
    List<DishDto> list=records.stream().map((item)->{
        DishDto dishDto=new DishDto();
        BeanUtils.copyProperties(item,dishDto);
        Long categoryId= item.getCategoryId();//分类id
        //根据id查询分类对象
        Category  category= categoryService.getById(categoryId);


        if(category!=null) {
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
        }

        return dishDto;

    }).collect(Collectors.toList());
    dishDtoPage.setRecords(list);



   return R.success(dishDtoPage);
}

/*
* 根据id查询菜品信息和对应的口味信息
* */
@GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){  //URL中的{xxx}占位符可以通过@PathVariable(“xxx”)绑定到操作方法的入参中


    DishDto dishDto = dishService.getByIdWithFlavor(id);

    return R.success(dishDto);
    }

/*
* 更新菜品信息            这个有bug
* */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){    //@RequestBody主要用来接收前端传递给后端的json字符串中的数据的(请求体中的数据的)；
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }
/*
* 跟据条件查询对应得菜品数据
* */
//@GetMapping ("/list")       //这个需要扩展一下，DishDto扩展自Dish
//    public R<List<Dish>> list(Dish dish){  //查询的信息很多，因此返回一个list集合；同时传一个long型够用，但传Dish类型更合适,通用性更强
//    //构造查询条件
//    LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
//    queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//    //添加查询状态，1表示得是菜品在售
//    queryWrapper.eq(Dish::getStatus,1);  //查询状态为1得就行了
//    // 添加排序条件
//    queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//    List<Dish> list = dishService.list(queryWrapper);
//
//    List<DishDto>
//
//    return R.success(list);
//    }

    @GetMapping ("/list")       //这个需要扩展一下，DishDto扩展自Dish
    public R<List<DishDto>> list(Dish dish){  //查询的信息很多，因此返回一个list集合；同时传一个long型够用，但传Dish类型更合适,通用性更强
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        //添加查询状态，1表示得是菜品在售
        queryWrapper.eq(Dish::getStatus,1);  //查询状态为1得就行了
        // 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);



        List<DishDto> dishDtoList=list.stream().map((item)->{
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId= item.getCategoryId();//分类id
            //根据id查询分类对象
            Category  category= categoryService.getById(categoryId);


            if(category!=null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            Long dishId = item.getId(); //口味id ，item是菜表
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);

            //相当于 select *from dish_flavor where dish_id=?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);

            dishDto.setFlavors(dishFlavorList);

            return dishDto;

        }).collect(Collectors.toList());


        return R.success(dishDtoList);
    }

}
