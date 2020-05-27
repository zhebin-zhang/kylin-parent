package com.kylin.upms.biz.web;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.kylin.aop.annotation.OperationLogDetail;
import com.kylin.aop.enums.OperationType;
import com.kylin.aop.enums.OperationUnit;
import com.kylin.upms.biz.dto.UserDto;
import com.kylin.upms.biz.entity.Role;
import com.kylin.upms.biz.entity.User;
import com.kylin.upms.biz.entity.UserRole;
import com.kylin.upms.biz.mail.MailSendMethod;
import com.kylin.upms.biz.service.IRoleService;
import com.kylin.upms.biz.service.IUserRoleService;
import com.kylin.upms.biz.service.IUserService;
import com.kylin.upms.biz.vo.ResEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.kafka.common.network.Send;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Mht
 * @since 2019-09-15
 */
@Api("操作用户")
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    IUserService iUserService;
    @Autowired
    IRoleService iRoleService;
    @Autowired
    IUserRoleService iUserRoleService;


    Logger logger= LoggerFactory.getLogger(this.getClass());

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @ApiOperation("根据查询条件分页查询用户列表")
    @RequestMapping(method = RequestMethod.GET,value = "/page")
    @OperationLogDetail(operationType = OperationType.UPDATE,operationUnit = OperationUnit.USER)
    public ResEntity get(UserDto userDto){
        Page<User> page = new Page<User>(userDto.getPageNum(),userDto.getPageSize());
        User user  = new User();
        BeanUtils.copyProperties(userDto,user);
        EntityWrapper entityWrapper = new EntityWrapper(user);
        entityWrapper.like("username",user.getUsername());
        Page page1 = iUserService.selectPage(page, entityWrapper);
        return ResEntity.ok(page1);
    }

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public ResEntity add(UserDto userDto){
        User user  = new User();
        BeanUtils.copyProperties(userDto,user);
        //加密密码
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        boolean b = iUserService.insertOrUpdate(user);
        if (b){ return ResEntity.ok("添加成功"); }
        return ResEntity.error("添加失败");
    }

    @OperationLogDetail(operationType = OperationType.UPDATE,operationUnit = OperationUnit.USER)
    @RequestMapping(method = RequestMethod.DELETE)
    public ResEntity del(Integer id){
        boolean b = iUserService.deleteById(id);
        if (b){ return ResEntity.ok("删除成功"); }
        return ResEntity.error("删除失败");
    }

    //获取所有角色的列表
    @OperationLogDetail(operationType = OperationType.UPDATE,operationUnit = OperationUnit.USER)
    @RequestMapping(method = RequestMethod.GET,value = "/getRolesListByUsername")
    public ResEntity get(String username){
        List<Role> roleslist = iRoleService.getRoleByUserName(username);
        System.out.println(roleslist);
        return ResEntity.ok(roleslist);
    }

    @OperationLogDetail(operationType = OperationType.UPDATE,operationUnit = OperationUnit.USER)
    @RequestMapping(method = RequestMethod.GET,value = "/getByUsername")
    public boolean getUser(UserDto userDto){
        User user  = new User();
        BeanUtils.copyProperties(userDto,user);
        EntityWrapper entityWrapper = new EntityWrapper(user);
        entityWrapper.like("username", user.getUsername());
        User selectOne = iUserService.selectOne(entityWrapper);
        System.out.println("user:=====>>"+selectOne);
        if (selectOne==null){return true;}
        return false;
    }
    //获取单独用户的角色
    @RequestMapping(method = RequestMethod.GET,value = "/getUserRoleById")
    public ResEntity roleById(Integer id){
        EntityWrapper<UserRole> wrapper = new EntityWrapper<>();
        wrapper.eq("uid",id);
        List<UserRole> userRoles = iUserRoleService.selectList(wrapper);
        List<Integer> list=new ArrayList<>();
        userRoles.forEach((userRole)->list.add(userRole.getRid()));
        logger.info("单个用户所有的角色"+list);
        return  ResEntity.ok(list);
    }

    //用户赋予角色和角色的修改
    @RequestMapping(method = RequestMethod.GET,value = "/changeUserRoles")
    public ResEntity add(Integer id,Integer rids[]){

        logger.info("更改权限的用户id--》"+id);
        logger.info("需要改为的目标id--》"+rids);

        EntityWrapper<UserRole> wrapper = new EntityWrapper<>();
        wrapper.eq("uid",id);
        iUserRoleService.delete(wrapper);


        List<UserRole> userRoleList=new ArrayList<>();
       Arrays.asList(rids).forEach((rid)->userRoleList.add(new UserRole(id,rid)));
        boolean b = iUserRoleService.insertBatch(userRoleList);
        if (b){return ResEntity.ok("修改成功");}
        return  ResEntity.error("修改失败");
    }


    //高亮
    @CrossOrigin(origins = {"http://localhost:8080"})
    @RequestMapping(method = RequestMethod.GET,value = "/page1")
    public ResEntity getTwo(UserDto userDto){
        AggregatedPage<User> selectuser = iUserService.selectuser(userDto.getPageNum(), userDto.getUsername());
        System.out.println("77777777"+selectuser.getNumber()+"======"+selectuser.getTotalElements());
        System.out.println(selectuser);
        return ResEntity.ok("查询成功",selectuser);
    }

    //判断发送邮件，可以自定义的方式，主要的代码在mail中
    @RequestMapping(method = RequestMethod.GET,value = "reset")
    public ResEntity reset(Integer id)
    {
        User user = iUserService.selectById(id);
        if (user.getEmail()==null && user.getEmail().equals(""))
        {
            return ResEntity.error("未保留验证邮箱，重置失败");
        }
        MailSendMethod mailSendMethod = new MailSendMethod();
        mailSendMethod.faMail2(user);
        return ResEntity.ok("重置邮件发送成功");
    }

    //简便方法邮件发送需要用到的注入，
//    @Autowired
//    JavaMailSender javaMailSender;
    //简便的发送邮件的方法
//    @RequestMapping(method = RequestMethod.GET,value = "reset2")
//    public  ResEntity reset2(Integer id)
//    {
//        //获取对应的对象
//        User user = iUserService.selectById(id);
//        //加密密码,重置为123456
//        user.setPassword(new BCryptPasswordEncoder().encode("123456"));
//        iUserService.insertOrUpdate(user);
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom("949064839@qq.com");
//        message.setTo("949064839@qq.com");
//        message.setSubject("测试邮件了偶~~");
//        message.setText("Hellow Word");
//        //javaMailSender.send(message);
//        new java.util.Timer().schedule(new TimerTask(){
//            @Override
//            public void run() {
//                //这里写延迟后要运行的代码
//                System.out.println("可以进来吗？？？？？？？？？？？？");
//                user.setPassword(new BCryptPasswordEncoder().encode(""));
//                iUserService.insertOrUpdate(user);
//                //如果只要这个延迟一次，用cancel方法取消掉．
//                this.cancel();
//            }}, 500);
//        return ResEntity.ok("重置成功");
//    }

    @Autowired
    PasswordEncoder endcoder;
    @Autowired
    RedisTemplate redisTemplate;
    @RequestMapping(method = RequestMethod.GET,value = "/resetPassword")
    public ResEntity editPass(Integer id) throws IOException, MessagingException {
        User user = iUserService.selectById(id);
        User user2 = new User();
        user2.setPassword(endcoder.encode("666666"));
        String email = user.getEmail();
        if (null!=email&& !"".equals(email))
        {
            EntityWrapper entityWrapper = new EntityWrapper();
            entityWrapper.eq("username",user.getUsername());
            sendEntity(email,user.getUsername());
            iUserService.update(user2,entityWrapper);
            redisTemplate.opsForValue().set(user.getUsername()+"_","666666",90, TimeUnit.SECONDS);
            return ResEntity.ok("重置成功");

        }else {
            return ResEntity.error("重置失败！");
        }

    }


    public void sendEntity(String uemail,String username) throws IOException, MessagingException {

            // 创建Properties 类用于记录邮箱的一些属性
            Properties props = new Properties();
            // 表示SMTP发送邮件，必须进行身份验证
            props.put("mail.smtp.auth", "true");
            //此处填写SMTP服务器
            props.put("mail.smtp.host", "smtp.qq.com");
            //端口号，QQ邮箱给出了两个端口，但是另一个我一直使用不了，所以就给出这一个587
            props.put("mail.smtp.port", "587");
            // 此处填写你的账号
            props.put("mail.user", "949064839@qq.com");
            // 此处的密码就是前面说的16位STMP口令
            props.put("mail.password", "dbyhnluzckhzbdfi");
            // 构建授权信息，用于进行SMTP进行身份验证
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    // 用户名、密码
                    String userName = props.getProperty("mail.user");
                    String password = props.getProperty("mail.password");
                    return new PasswordAuthentication(userName, password);
                }
            };
            // 使用环境属性和授权信息，创建邮件会话
            Session mailSession = Session.getInstance(props, authenticator);
            // 创建邮件消息
            MimeMessage message = new MimeMessage(mailSession);
            // 设置发件人
            InternetAddress form = new InternetAddress(
                    props.getProperty("mail.user"));
            message.setFrom(form);
            // 设置收件人的邮箱
            InternetAddress to = new InternetAddress(uemail);
            message.setRecipient(Message.RecipientType.TO, to);
            // 设置邮件标题
            message.setSubject("快速开发邮件");
            // 设置邮件的内容体
            message.setContent("尊敬的客户："+username+"您的初始化密码为：666666;温馨提示：为了防止有人的不良行为，请您尽快修改密码^_^;天下游戏祝您生活愉快！！", "text/html;charset=UTF-8");
            // 最后当然就是发送邮件啦
            Transport.send(message);
    }
}
