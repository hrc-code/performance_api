package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.ButtonMapper;
import com.example.workflow.model.entity.Button;
import com.example.workflow.service.ButtonService;
import org.springframework.stereotype.Service;

/**
* @author hrc
* @description 针对表【button(按钮表)】的数据库操作Service实现
* @createDate 2024-03-29 01:00:07
*/
@Service
public class ButtonServiceImpl extends ServiceImpl<ButtonMapper, Button>
    implements ButtonService{

}




