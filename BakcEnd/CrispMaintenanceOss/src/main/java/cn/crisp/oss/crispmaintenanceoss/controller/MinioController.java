package cn.crisp.oss.crispmaintenanceoss.controller;

import cn.crisp.common.Constants;
import cn.crisp.common.R;
import cn.crisp.entity.User;
import cn.crisp.oss.crispmaintenanceoss.dto.MailDto;
import cn.crisp.oss.crispmaintenanceoss.service.MailService;
import cn.crisp.oss.crispmaintenanceoss.service.TokenService;
import cn.crisp.oss.crispmaintenanceoss.utils.IdUtils;
import cn.crisp.oss.crispmaintenanceoss.utils.RedisCache;
import cn.crisp.oss.crispmaintenanceoss.utils.ValidateCodeUtils;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@Slf4j
@RequestMapping("/oss")
public class MinioController {
    @Autowired
    private MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private MailService mailService;

    @Autowired
    private RedisCache redisCache;
    /**
     * 文件上传
     */
    @SneakyThrows
    @PostMapping("/upload")
    public R<String> uploadFile(HttpServletRequest request, @RequestParam("file") MultipartFile file){
        if (tokenService.getLoginUser(request) == null) return R.error("检查登录信息");

        if(file == null){
            return R.error("上传文件不能为空");
        }

        String originalFileName = file.getOriginalFilename();

        InputStream inputStream = file.getInputStream();

        String fileName = bucketName + System.currentTimeMillis() + originalFileName;
        //上传
        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(inputStream,file.getSize(),-1).contentType(file.getContentType()).build());
            inputStream.close();
        }catch (Exception e){
            log.info(e.getMessage());
            return R.error("上传失败");
        }
        return R.success(endpoint + "/" + bucketName + "/" + fileName);
    }

    /**
     *检查Email 格式（正则表达式）
     * @param content
     * @return
     */
    private boolean checkEmailFormat(String content){
        /*
         * " \w"：匹配字母、数字、下划线。等价于'[A-Za-z0-9_]'。
         * "|"  : 或的意思，就是二选一
         * "*" : 出现0次或者多次
         * "+" : 出现1次或者多次
         * "{n,m}" : 至少出现n个，最多出现m个
         * "$" : 以前面的字符结束
         */
        String REGEX="^\\w+((-\\w+)|(\\.\\w+))*@\\w+(\\.\\w{2,3}){1,3}$";
        Pattern p = Pattern.compile(REGEX);
        Matcher matcher=p.matcher(content);

        return matcher.matches();
    }


    /**
     * 发送邮件验证码
     */
    @PostMapping("/mail_code")
    public R<String> sendCode(HttpServletRequest request, @RequestBody MailDto mailDto) {
        User user = tokenService.getLoginUser(request).getUser();
        if (user == null) return R.error("登录信息错误，请检查登录信息");
        if (!checkEmailFormat(mailDto.getMail())) {
            return R.error("邮箱格式错误");
        }
        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        redisCache.setCacheObject(Constants.VALIDATE_MAIL_KEY + mailDto.getMail(), code, 5, TimeUnit.MINUTES);
        try {
            mailService.sendMail(mailDto.getMail(), "CrispMaintenanceSystem", code);
        }catch (Exception e) {
            e.printStackTrace();
            return R.error("发送失败，请检查邮箱信息");
        }
        return R.success("发送成功");
    }
}
