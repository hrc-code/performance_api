package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.OkrKey;
import com.example.workflow.mapper.OkrKeyMapper;
import com.example.workflow.service.OkrKeyService;
import org.springframework.stereotype.Service;

@Service
public class OkrKeyServiceImpl extends ServiceImpl<OkrKeyMapper, OkrKey> implements OkrKeyService {
}
