package com.qiguliuxing.dts.db.service;

import com.qiguliuxing.dts.db.dao.DtsIdCartMapper;
import com.qiguliuxing.dts.db.domain.DtsIdCart;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("dtsIdCartService")
public class DtsIdCartService {
    @Resource
    private DtsIdCartMapper dtsIdCartMapper;

    public int add(DtsIdCart dtsIdCart) {
        return dtsIdCartMapper.add(dtsIdCart);
    }
}
