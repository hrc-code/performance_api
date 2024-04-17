package com.example.workflow.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.Router;
import com.example.workflow.mapper.IndexMapper;
import com.example.workflow.service.IndexService;
import org.springframework.stereotype.Service;

@Service
public class IndexServiceImpl extends ServiceImpl<IndexMapper, Router> implements IndexService {

}
