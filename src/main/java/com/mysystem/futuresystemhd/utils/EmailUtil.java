package com.mysystem.futuresystemhd.utils;

import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.mysystem.futuresystemhd.constant.UserConstant.*;

@Slf4j
@Component
public class EmailUtil {

    @Resource
    private JavaMailSender javaMailSender;

    /**
     * 发送邮件的邮箱
     */
    @Value("${spring.mail.username}")
    private String sendUserEmail;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送邮箱验证码
     * @param emailReceiver 接收者
     * @param function 作用(登录，注册或者绑定)
     * @return
     */
    public String sendSimpleMail(String emailReceiver,String function){

        //构建邮件对象
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        //设置邮件发送者
        simpleMailMessage.setFrom(sendUserEmail);

        //设置邮件接收者
        simpleMailMessage.setTo(emailReceiver);

        //设置邮件主题
        simpleMailMessage.setSubject(function + "验证码");

        //设置邮件正文
        //邮箱验证码
        String RandomNumber = CaptchaUtil.RandomNumber(EMAIL_CAPTCHA_SIZE);
        String text = "您的验证码为: " + RandomNumber + " 请勿泄露给他人";
        simpleMailMessage.setText(text);

        //发送邮件
        try {
            javaMailSender.send(simpleMailMessage);
            log.info("发送给" + emailReceiver + "邮件发送成功");
            return RandomNumber;
        } catch (MailException e) {
            log.error("发送给" + RandomNumber + "的邮件失败");
            throw new BusinessException(ErrorCode.SEND_EMAIL_ERROR);
        }
    }

    public String sendRegisterEmail(String sendUserEmail){
        String captchaCode = sendEmail(sendUserEmail, "注册", EMAIL_USER_CAPTCHA_REGISTER);
        return captchaCode;
    }

    public String sendLoginEmail(String sendUserEmail){
        String captchaCode = sendEmail(sendUserEmail, "登录", EMAIL_USER_CAPTCHA_LOGIN);
        return captchaCode;
    }

    public String sendBindEmail(String sendUserEmail){
        String captchaCode = sendEmail(sendUserEmail, "绑定", EMAIL_USER_CAPTCHA_BIND);
        return captchaCode;
    }


    /**
     * 发送验证码（防刷）
     * @param email
     * @param function
     * @return
     */
    public String sendEmail(String email,String function,String redisKey){

        try {
            //防刷
            String EmailCode = stringRedisTemplate.opsForValue().get(redisKey + email);
            if(StringUtils.isNotEmpty(EmailCode)){
                //获取验证码发送时间毫秒值
                long timeDateLong = Long.parseLong(EmailCode.split("_")[1]);
                if(System.currentTimeMillis() - timeDateLong < 60000){
                    //验证码60秒内无法发送
                    log.error(email + "验证码发送失败");
                    throw new BusinessException(ErrorCode.SEND_EMAIL_ERROR,"获取验证码太过频繁");
                }
            }

            //获取验证码
            String emailCode = sendSimpleMail(email,function);
            String dateEmailData = emailCode + "_" + System.currentTimeMillis();

            stringRedisTemplate.opsForValue().set(redisKey + email,dateEmailData,10, TimeUnit.MINUTES);
            log.info("{} 验证码发送成功",email);
            return emailCode;
        } catch (NumberFormatException e) {
            log.error("验证码发送失败",e);
            throw new BusinessException(ErrorCode.SEND_EMAIL_ERROR);
        }
    }
}
