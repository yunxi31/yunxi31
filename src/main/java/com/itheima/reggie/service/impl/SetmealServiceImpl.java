package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

   @Autowired
    private SetmealDishService setmealDishService;
    /*
    * 新增套餐，同时保存和菜品之间得关联关系
    * */
   @Transactional
    public void saveWitDish(SetmealDto setmealDto) {
    //保存套餐得基本信息，操作setmeal,执行insert
    this.save(setmealDto);

       List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes(); //这里存在一个问题，setmealdish里存档的只有dishId,没有setmealId的值，所以需要处理，遍历该集合元素
       setmealDishes.stream().map((item)->{
           item.setSetmealId(setmealDto.getId());
           return item;
       }).collect(Collectors.toList());



       //保存套餐和菜品得关联信息，操作setmeal_dish，执行insert操作
      setmealDishService.saveBatch(setmealDishes);
    }

    @Transactional
    public void removeWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (1,2,3) and status=1
       //查询套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);  //获取状态，进行等值比较

        int count = this.count(queryWrapper);
        if (count>0){
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        //如果可以删除，先删除套餐表中的数据-setmeal
        this.removeByIds(ids);

        //删除关系表中的数据  --setmeal_dish  要操作这个表，得注入setmealDishservice
//       setmealDishService.removeByIds();   //这个不能调用,当前套餐得id并不是关系表主键值
    LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
    lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);


    setmealDishService.remove(lambdaQueryWrapper);

   }
}
