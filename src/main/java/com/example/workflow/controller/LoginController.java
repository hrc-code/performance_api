package com.example.workflow.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.workflow.common.R;
import com.example.workflow.model.bean.CheckCode;
import com.example.workflow.model.entity.Employee;
import com.example.workflow.model.entity.LoginDto;
import com.example.workflow.service.EmployeeService;
import com.example.workflow.utils.JwtHelper;
import com.example.workflow.utils.VerifyCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LoginController {
    private final EmployeeService employeeService;

    /**
     * 获取验证码图片
     */
    @GetMapping("/getVerifyCode")
    public void getVerifyCode(HttpServletResponse response, HttpSession session) throws IOException {

        try (OutputStream os = response.getOutputStream()) {

            int width = 200;

            int height = 69;

            BufferedImage verifyImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            //生成对应宽高的初始图片
            //单独的一个类方法，出于代码复用考虑，进行了封装。
            String randomText = VerifyCode.drawRandomText(width, height, verifyImg);

            //功能是生成验证码字符并加上噪点，干扰线，返回值为验证码字符
            CheckCode checkCode = new CheckCode(randomText, 60);

            session.setAttribute("verifyCode", checkCode);

            // 必须设置响应内容类型为图片，否则前台不识别
            response.setContentType("image/png");
            // 输出图片流
            ImageIO.write(verifyImg, "png", os);
            os.flush();
        }
        // 关闭流
    }

    /**
     * 登陆接口
     */
    @PostMapping("/login")
    public R<Object> login(@RequestBody LoginDto dto, HttpSession session) {

       CheckCode checkCode = (CheckCode) session.getAttribute("verifyCode");

        if (Objects.isNull(checkCode) || checkCode.isExpired()) {
            return R.error("验证码已过期，请点击重新生成！");
        }
        if (!checkCode.getCode().equalsIgnoreCase(dto.getVerifyCode())) {
            return R.error("验证码错误，请重新输入！");
        }


        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getNum, dto.getUsername()).eq(Employee::getPassword, dto.getPassword());

        Employee employee = employeeService.getOne(wrapper);

        if(employee == null) {
            return  R.error("用户名或密码错误");
        }else if (employee.getState() == 0){
           return  R.error("账号已被封禁");
        }
        Map<String, String> payload = new HashMap<>(30);
        payload.put("id", String.valueOf(employee.getId()));
        payload.put("num", employee.getNum());
        payload.put("roleId", String.valueOf(employee.getRoleId()));

        Long timestamp = JwtHelper.generateExpireDate(JwtHelper.EXPIRE_TIME);
        String token = JwtHelper.createToken(timestamp, payload);
        Map<String, Object> map = new HashMap<>(30);
        map.put("token", token);
        map.put("userInfo", employee);
        // 给这个的目的是让前端每次请求时校验是否过期token，如果还差一分钟就过期就应该马上更新token
        map.put("timestamp", timestamp);
        session.setAttribute(token, timestamp);

        return R.success(map);
    }

    /**
     * 测试时使用的登陆接口
     */
    @PostMapping("/login/test")
    public R<Object> test() {
        Employee employee = JSON.parseObject("  {\n" +
                "        \"is_change_pwd\": 1,\n" +
                "        \"id\": 6,\n" +
                "        \"num\": \"10004\",\n" +
                "        \"role_id\": 1,\n" +
                "        \"password\": \"123456\",\n" +
                "        \"state\": 1,\n" +
                "        \"name\": \"开发人员\",\n" +
                "        \"phone_num1\": \"14578524245\",\n" +
                "        \"phone_num2\": \"14578524245\",\n" +
                "        \"email\": \"test@test.com\",\n" +
                "        \"id_num\": \"123456789\",\n" +
                "        \"birthday\": \"1238-10-17\",\n" +
                "        \"address\": \"123456789\",\n" +
                "        \"remark\": \" \",\n" +
                "        \"create_time\": \"2024-03-31 01:05:29\",\n" +
                "        \"update_time\": \"2024-03-31 01:05:29\",\n" +
                "        \"create_user\": null,\n" +
                "        \"update_user\": null\n" +
                "    }", Employee.class);
        Map<String, String> payload = new HashMap<>(30);
        payload.put("id", String.valueOf(employee.getId()));
        payload.put("num", employee.getNum());
        payload.put("roleId", String.valueOf(employee.getRoleId()));

        long timestamp = JwtHelper.generateExpireDate(JwtHelper.EXPIRE_TIME);
        String token = JwtHelper.createToken(timestamp, payload);
        Map<String, Object> map = new HashMap<>(30);
        map.put("token", token);
        map.put("userInfo", employee);
        // 给这个的目的是让前端每次请求时校验是否过期token，如果还差一分钟就过期就应该马上更新token
        map.put("timestamp", timestamp);

        return R.success(map);
    }

    /** 退出接口*/
    @GetMapping("/logout")
    public R<Object> logout(HttpSession session) {
        // 使session失效
        session.invalidate();
        return R.success();
    }
}

