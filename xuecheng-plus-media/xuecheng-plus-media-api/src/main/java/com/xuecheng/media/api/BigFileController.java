package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author jxchen
 * @version 1.0
 * @description 上传分块视频及合并
 * @date 2023/9/18 21:06
 */
@Api(value = "大文件上传接口", tags = "大文件上传接口")
@RestController
public class BigFileController {

    @Autowired
    MediaFileService mediaFileService;

    @ApiOperation(value = "文件上传前检查是否已于数据库/minio存在")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkFile(@RequestParam("fileMd5") String fileMd5) throws Exception {
        RestResponse<Boolean> booleanRestResponse = mediaFileService.checkFile(fileMd5);
        return booleanRestResponse;
    }

    @ApiOperation(value = "分块文件上传前检查是否已于数据库/minio存在")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkChunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) throws Exception {
        RestResponse<Boolean> booleanRestResponse = mediaFileService.checkChunk(fileMd5, chunk);
        return booleanRestResponse;
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadChunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) throws Exception {
        //创建临时文件
        File tempFile = File.createTempFile("minio", ".temp");
        file.transferTo(tempFile);
        //获得文件路径
        String localFilePath = tempFile.getAbsolutePath();
        RestResponse restResponse = mediaFileService.uploadChunk(fileMd5, chunk, localFilePath);
        return restResponse;
    }

    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergeChunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) throws Exception {
        //默认机构id
        Long companyId = 1232141425L;
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileName);
        uploadFileParamsDto.setFileType("001002");
        uploadFileParamsDto.setTags("视频文件");
        RestResponse restResponse = mediaFileService.mergeChunk(companyId, fileMd5, chunkTotal, uploadFileParamsDto);
        return restResponse;
    }


}
