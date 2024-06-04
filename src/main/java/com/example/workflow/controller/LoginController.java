package com.example.workflow.controller;

import cn.hutool.http.server.HttpServerRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.workflow.bean.CheckCode;
import com.example.workflow.common.R;
import com.example.workflow.entity.Employee;
import com.example.workflow.entity.LoginDto;
import com.example.workflow.service.EmployeeService;
import com.example.workflow.utils.JWTHelper;
import com.example.workflow.utils.VerifyCode;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
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

@RestController
@RequiredArgsConstructor
public class LoginController {
    private final EmployeeService employeeService;

    /**
     * 二维码接口
     * @param session
     * @param response
     */
    /* 获取验证码图片*/
    @GetMapping("/getVerifyCode")
    public void getVerifyCode(HttpServletResponse response,HttpSession session) {

        try {

            int width=200;

            int height=69;

            BufferedImage verifyImg=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

            //生成对应宽高的初始图片
            //单独的一个类方法，出于代码复用考虑，进行了封装。
            String randomText = VerifyCode.drawRandomText(width,height,verifyImg);

            //功能是生成验证码字符并加上噪点，干扰线，返回值为验证码字符
            CheckCode checkCode = new CheckCode(randomText,60);

            session.setAttribute("verifyCode", checkCode);
            System.out.println(session);

            response.setContentType("image/png");//必须设置响应内容类型为图片，否则前台不识别

            OutputStream os = response.getOutputStream(); //获取文件输出流

            ImageIO.write(verifyImg,"png",os);//输出图片流

            os.flush();

            os.close();//关闭流

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    /**
     * 登陆接口
     *
     * @param dto
     * @param session
     */
    @PostMapping("/login")
    public R<Object> login(@RequestBody LoginDto dto, HttpSession session) {

       CheckCode checkCode = (CheckCode) session.getAttribute("verifyCode");

      if(checkCode.isExpired()) return R.error("验证码已过期，请点击重新生成！");
      if (!checkCode.getCode().equalsIgnoreCase(dto.getVerifyCode())) return R.error("验证码错误，请重新输入！");


      LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
       wrapper.eq(Employee::getNum, dto.getUsername()).eq(Employee::getPassword, dto.getPassword());

        Employee employee = employeeService.getOne(wrapper);

        if(employee == null) {
            return  R.error("用户名或密码错误");
        }else if (employee.getState() == 0){
           return  R.error("账号已被封禁");
        }
        Map<String, String> payload = new HashMap<>();
        payload.put("id", String.valueOf(employee.getId()));
        payload.put("num", employee.getNum());
        payload.put("roleId", String.valueOf(employee.getRoleId()));

        Long timestamp = JWTHelper.generateExpireDate(JWTHelper.EXPIRE_TIME);
        String token = JWTHelper.createToken(timestamp, payload);
        Map <String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("userInfo", employee);
        map.put("timestamp", timestamp);//给这个的目的是让前端每次请求时校验是否过期token，如果还差一分钟就过期就应该马上更新token
        session.setAttribute(token, timestamp);

        return R.success(map);
    }

    /** 退出接口*/
    @GetMapping("/logout")
    public R<Object> logout(HttpSession session, HttpServerRequest request) {
        //获取请求头中的令牌(token)
        String token = request.getHeader("Authorization");
        //判断令牌是否存在，如果不存在，返回错误结果(未登陆)
        if (StringUtils.hasLength(token)) {
            token = token.replace("Bearer ","");
            session.removeAttribute(token);
        }
        return R.success();
    }
}

