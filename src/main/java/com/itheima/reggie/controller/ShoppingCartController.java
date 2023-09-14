package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /*
    * 添加购物车
    * */

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
       log.info("购物车数据：{}",shoppingCart);
        //设置用户id，指定当前是哪个用户得购物车数据
        Long currentId=BaseContext.getCurrentId();  //就是取得用户id
        shoppingCart.setUserId(currentId);    //导入购物车中

        //查询当前菜品或者套餐是否在购物车中
        Long dishId=shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);


        if (dishId!=null){
            //添加到购物车得是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);

        }else {
            //添加到购物车得是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //其对应sql select * from shopping_cart where user_id=? and dish_id/setmeal_id=?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);


        if (cartServiceOne !=null){
            //如果已经存在，就在原来数量基础上+1     这是一个更新操作
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number+1);
            shoppingCartService.updateById(cartServiceOne);
        }else {
            //如果不存在，则添加到购物车，数量默认就是1
            shoppingCart.setNumber(1);   //页面提交得时候并没有把number传过来，所以需要手动设置一下
            shoppingCartService.save(shoppingCart);
            cartServiceOne=shoppingCart;
        }

       return R.success(cartServiceOne);
    }



    /*
    * 查看购物车
    * */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车。。。");
        LambdaQueryWrapper<ShoppingCart>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);  //跟据升序排列

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    /*
    * 清空购物车
    * */

    @DeleteMapping("/clean")
    public R<String> clean(){
        //sql: delete from shopping_cart where user_id=?
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");
    }
}
