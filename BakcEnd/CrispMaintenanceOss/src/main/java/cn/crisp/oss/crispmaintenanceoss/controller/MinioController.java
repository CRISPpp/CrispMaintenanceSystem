package cn.crisp.oss.crispmaintenanceoss.controller;

import cn.crisp.common.R;
import cn.crisp.oss.crispmaintenanceoss.dto.MailDto;
import cn.crisp.oss.crispmaintenanceoss.service.MailService;
import cn.crisp.oss.crispmaintenanceoss.service.TokenService;
import cn.crisp.oss.crispmaintenanceoss.utils.IdUtils;
import cn.crisp.oss.crispmaintenanceoss.utils.ValidateCodeUtils;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

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
    /**
     * 文件上传
     */
    @SneakyThrows
    @PostMapping("/upload")
    public R<String> uploadFile(HttpServletRequest request, @RequestBody MultipartFile file){
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
     * 发送邮件验证码
     */
    @PostMapping("/mail_code")
    public R<String> sendCode(HttpServletRequest request, @RequestBody MailDto mailDto) {
        if (tokenService.getLoginUser(request) == null) return R.error("检查登录信息");
        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        try {
            mailService.sendMail(mailDto.getMail(), "CrispMaintenanceSystem", code);
        }catch (Exception e) {
            e.printStackTrace();
            return R.error("发送失败，请检查邮箱信息");
        }
        return R.success("发送成功");
    }
}
