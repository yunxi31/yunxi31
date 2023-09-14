package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmpController {
    @Autowired
    private EmpService empService;

    /*
    * 员工登录
    * */
@PostMapping("/login")
    public R<Employee> login(HttpServletRequest request,@RequestBody Employee employee){
    //1.将页面提交的密码password进行md5加密处理
    String password=employee.getPassword();     //获取密码从emp表中，并存储在password变量中
    password=DigestUtils.md5DigestAsHex(password.getBytes()); //套用工具类DigestUtils中的md5DigsetAsHex(),里面传入password,其中把password拆分存储于byte数组，然后将调用方法得到的数据存入新的password中，以达到MD5加密
    //2.根据页面提交的用户名username查询数据库
    LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();     //难，难，难，方法没见过
    queryWrapper.eq(Employee::getUsername,employee.getUsername());
    Employee emp=empService.getOne(queryWrapper);
    //3.如果没有查询到则返回登录失败结果
    if (emp==null) {
        return R.error("登录失败");
    }

    //4.密码对比，如果不一致，则返回失败结果
    if (!emp.getPassword().equals(password)) {
        return R.error("登录失败，请检查密码是否正确");
    }
    //5.查看员工状态，如果为已禁用状态，则返回员工已禁用结果
    if(emp.getStatus()==0) {    //汗，变量名错了，把emp写成了employee,然后报500错误
        return R.error("该员工账号已禁用");
    }
    //6.登陆成功，将员工id存入session并返回登录成功结果
    request.getSession().setAttribute("employee",emp.getId());
    return R.success(emp);   //有点迷，不是要返回成功结果吗，不应该是输出“登陆成功”吗？   刚才查源码，里面只能传obj对象，所以要传emp,这个和R<>中返回类型一致
    }



    /*
    * 用户退出登录
    * */
@PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
    //清理session中保存的当前登录员工的id
    request.getSession().removeAttribute("employee");
    return R.success("退出成功");
}
/*
* 新增用户
* */
@PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
       log.info("新增员工，员工信息：{}",employee.toString());
       //設置初始密碼為123456,需要进行MD5加密
    employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//    employee.setCreateTime(LocalDateTime.now());
//    employee.setUpdateTime(LocalDateTime.now());
//    //获取当前登录用户的id
//    Long empId=(Long) request.getSession().getAttribute("employee");
//    employee.setCreateUser(empId);
//    employee.setUpdateUser(empId);

    empService.save(employee);

        return R.success("新增员工成功");
    }

/*员工信息
* 分頁查詢實現
* */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page= {},pageSize= {},name= {}",page,pageSize,name);

        //构造分页构造器
        Page pageInfo=new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper();

        //添加过滤条件
        queryWrapper.like(StringUtils.hasText(name),Employee::getName,name);

        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        empService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }
    /*
    *根据id修改员工信息
    * */
    @PutMapping
 public R<String> update (HttpServletRequest request,@RequestBody Employee  employee){
     log.info(employee.toString());
//     long id=Thread.currentThread().getId();
//     log.info("线程id为：{}",id);
//     Long empId=(Long)request.getSession().getAttribute("employee");
//     employee.setUpdateTime(LocalDateTime.now());
//     employee.setUpdateUser(empId);
     empService.updateById(employee);
     return R.success("员工信息修改成功");
    }

    /*
    * 根据id查询员工信息
    * */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息。。。");
        Employee employee=empService.getById(id);
        if(employee!=null) {
            return R.success(employee);
        }
        return R.error("没有查询到对应的员工信息");
    }
}

