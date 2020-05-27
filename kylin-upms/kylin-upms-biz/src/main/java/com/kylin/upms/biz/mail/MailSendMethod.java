package com.kylin.upms.biz.mail;

import com.kylin.upms.biz.entity.User;

/**
 * @Author: zhebin
 * @Date: 2019/9/27 16:22
 */
public class MailSendMethod {

    public void faMail2(User user1) {
        MailOperation operation = new MailOperation();
        String user = "949064839@qq.com";
        String password = "dbyhnluzckhzbdfi";
        String host = "smtp.qq.com";

        String from = "949064839@qq.com";
        String to = user1.getEmail();// 收件人 目标邮箱地址
        String subject = "重置密码服务";
        //邮箱内容
        StringBuffer sb = new StringBuffer();
        int yzm = (int) ((Math.random() * 9 + 1) * 100000);
        sb.append("<!DOCTYPE>" + "<div bgcolor='#f1fcfa'   style='border:1px solid #d9f4ee; font-size:14px; line-height:22px; color:#005aa0;padding-left:1px;padding-top:5px;   padding-bottom:5px;'><span style='font-weight:bold;'>温馨提示：</span>"
                + "<div style='width:950px;font-family:arial;'>欢迎使用小彩虹，您的验证码为：<br/><h2 style='color:green'>" + yzm + "</h2><br/>本邮件由系统自动发出，请勿回复。<br/>感谢您的使用。<br/>@阿哲的小彩虹</div>"
                + "</div>");
        try {
            String res = operation.sendMail(user, password, host, from, to,
                    subject, sb.toString());
            System.out.println(res);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}