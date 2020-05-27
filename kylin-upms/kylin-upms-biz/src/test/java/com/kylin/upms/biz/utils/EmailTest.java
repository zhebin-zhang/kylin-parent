package com.kylin.upms.biz.utils;

import com.kylin.upms.biz.entity.User;
import com.kylin.upms.biz.mail.MailSendMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: zhebin
 * @Date: 2019/9/27 16:28
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class EmailTest {

    @Test
    public void test()
    {
        MailSendMethod mailSendMethod = new MailSendMethod();
        mailSendMethod.faMail2(new User());
        System.out.println("测试完成");
    }
}
