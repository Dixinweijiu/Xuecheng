package com.xuecheng.content.feignClient;

import com.xuecheng.content.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author jxchen
 * @version 1.0
 * @description 媒资管理服务远程调用接口
 * @date 2023/10/23 19:35
 */
@FeignClient(value = "media-api", configuration = MultipartSupportConfig.class)
public interface MediaServiceClient {

    /**
     * @description 远程调用uploadFile
     * @param upload 上传内容
     * @param objectName 对象名称
     * @return java.lang.String
     */
    String uploadFile(@RequestPart("filedata") MultipartFile upload,
                      @RequestParam(value = "objectName", required = false) String objectName);

}
