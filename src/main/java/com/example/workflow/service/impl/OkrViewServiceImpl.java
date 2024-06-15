package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.OkrViewMapper;
import com.example.workflow.model.entity.OkrView;
import com.example.workflow.service.OkrViewService;
import org.springframework.stereotype.Service;

@Service
public class OkrViewServiceImpl extends ServiceImpl<OkrViewMapper,OkrView> implements OkrViewService {
}
