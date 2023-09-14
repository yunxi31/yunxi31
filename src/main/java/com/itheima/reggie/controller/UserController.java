package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.Utils.SMSUtils;
import com.itheima.reggie.Utils.ValidateCodeUtils;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /*
    * 发送手机验证码短信
    *
    * */

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        //获取手机号
        String phone = user.getPhone();
        //这里需要判断手机号是否为空
        if (StringUtils.isNotEmpty(phone)) {

            //生成随机四位验证码
//            Integer integer = ValidateCodeUtils.generateValidateCode(4);//这里转为toString，方便后续比对
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}", code);
//            //调用阿里云短信服务API完成发送短信
//            SMSUtils.sendMessage("reggie","",phone,code);

            //保存验证码，进行正确性校验  保存在session
            session.setAttribute(phone, code);

            return R.success("手机验证码短信发送成功");
        }


        return R.error("短信发送失败");

    }




    /*
    * 移动端用户登录
    * */
        @PostMapping("/login")
        public R<User> login (@RequestBody Map map, HttpSession session)   //这里泛型改为User
        { //这里返回得数据为json，不能是user了，user里面有phone，但不能接受所需要得code
            //这里提供了两种解决方式，一个是dto，另一个是map，利用键值对
        log.info(map.toString());

        //获取手机号
        String phone=map.get("phone").toString();
        //获取验证码
            String code = map.get("code").toString();
            //从session中获取保存得验证码进行比对
            Object codeInSession = session.getAttribute(phone);

            //进行验证码比对（页面提交得，和获取的比对）
            if (codeInSession!=null && codeInSession.equals(code)){
             //如果能够对比成功，说明登录成功


                //判断当前得手机号对应的用户是否为新用户，如果是，则完成新用户   在user表里查询手机号，要是没有就是新用户
                LambdaQueryWrapper<User>queryWrapper=new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getPhone,phone);

                User user = userService.getOne(queryWrapper);//手机号是唯一标识，所以用这个方法
            //开始判断当前手机号是空，则创建新用户
                if (user==null){
                user=new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
                return R.success(user);
            }


            return R.error("登陆失败");
        }


    }