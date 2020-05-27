package com.kylin.upms.biz.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 登陆失败监听
 *
 * @author Shaoj 3/2/2017.
 */
@Component
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    @Autowired
    private RedisTemplate redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFailureListener.class);

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent authenticationFailureBadCredentialsEvent) {
        String account = authenticationFailureBadCredentialsEvent.getAuthentication().getPrincipal().toString();
        logger.info("登录失败调用-----------------------------------------------------{}"+account);

        //不管每台是否超过5次输入错误第二天将其还原
        redisTemplate.opsForValue().increment(account+"_",1);
        Integer i = (Integer) redisTemplate.opsForValue().get(account + "_");
        if (i > 3 && i <= 5 && redisTemplate.hasKey(account+"_")){  //如果大于3次锁定60秒
            redisTemplate.opsForValue().set(account+"enabled",0,60,TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(account+"_",i,120,TimeUnit.SECONDS);//一天后失效
        }else if (i > 5 && redisTemplate.hasKey(account+"_")){//如果大于5次锁定120秒
            redisTemplate.opsForValue().set(account+"enabled",0,120,TimeUnit.SECONDS);//锁定一天时间
            redisTemplate.opsForValue().set(account+"_",i,120,TimeUnit.SECONDS);//一天后失效
        }


    }
}
