package com.example.workflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.workflow.common.R;
import com.example.workflow.mapper.RouterMapper;
import com.example.workflow.model.entity.RoleRouter;
import com.example.workflow.model.entity.Router;
import com.example.workflow.model.entity.Temporary;
import com.example.workflow.service.IndexService;
import com.example.workflow.service.RoleRouterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/Index")
public class IndexController {
    @Autowired
    private IndexService IndexService;
    @Autowired
    private RoleRouterService roleRouterService;
    @Autowired
    private RouterMapper routerMapper;

    @PostMapping("/getPermission")
    public R<List<Router>> getPermission(@RequestBody JSONObject obj){

        LambdaQueryWrapper<RoleRouter> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(RoleRouter::getRoleId,obj.getString("roleId"))
                .eq(RoleRouter::getState,1);
        List<RoleRouter> routerList= roleRouterService.list(queryWrapper);
        if (!CollectionUtils.isEmpty(routerList)) {
            Set<String> routerSet = routerList.stream().map(RoleRouter::getRouterId).collect(Collectors.toSet());
            LambdaQueryWrapper<Router> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Router::getId, routerSet);
            List<Router> routers = routerMapper.selectList(wrapper);

            // 筛选出全部的父路由
            List<Router> parentRouterSet = routers.stream().filter(router -> router.getType().equals(1)).collect(Collectors.toList());

            for (Router router : parentRouterSet) {
                Integer id = Integer.valueOf(router.getId());
                // 筛选出它的子路由
                List<Router> childrenRouterSet = routers.stream().filter(router1 -> router1.getParentId().equals(id)).collect(Collectors.toList());
                router.setChildren(childrenRouterSet);
            }
            return R.success(parentRouterSet);

        } else {
            return R.success(new ArrayList<>(0));
        }

    }

    @PostMapping("/getCaptchaCode")
    public R<String> getCaptchaCode(){
        String[] nums = {"q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "a", "s", "d", "f", "g", "h", "j", "k", "l", "z", "x", "c", "v", "b", "n", "m", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        // 初始化 拼接字符串
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            //每次生成一个0 - 35 之间的 number 作为随机获取验证码的下标
            int p = random.nextInt(36);
            //拼接验证码  随机抽取大小写字母和数字
            str.append(nums[p]);
        }
        return R.success(str.toString());
    }

    @PostMapping("/login")
    public R<Temporary> login(){
        Temporary obj=new Temporary();
        obj.setSys_token("d33a7fdf547d2a086a96f4d38253cbc9");
        obj.setAdmin_nick_name("绩效专员");
        obj.setAdmin_id(1);
        obj.setAvatar("https://osstest.eetop.com/bewt365/578d0d88e7ad2f9ae99f10eee8e08d9c.jpg");

        return R.success(obj);
    }
}
