package com.atguigu.ssyx.payment.service.impl;

import com.atguigu.ssyx.model.order.PaymentInfo;
import com.atguigu.ssyx.payment.mapper.PaymentInfoMapper;
import com.atguigu.ssyx.payment.service.PaymentInfoService;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * ClassName: PaymentInfoServiceImpl
 * Package: com.atguigu.ssyx.payment.service.impl
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/8/22 20:51
 * @Version 1.0
 */
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper,PaymentInfo> implements  PaymentInfoService {
}
