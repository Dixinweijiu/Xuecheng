package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    //存储普通文件
    @Value("${minio.bucket.files}")
    private String bucket_mediaFiles;

    //存储视频文件
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Autowired
    MediaFileService currentProxy;

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    /**
     * @description 根据扩展名获得mimeType
     * @param extension 扩展名
     * @return mimeType
     */
    private String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 通用mimeType，字节流
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * @description 上传文件到Minio
     * @param localFilePath 本地文件路径
     * @param bucket 桶名称
     * @param objectName 对象名称
     * @param mimeType mimeType
     * @return 上传结果
     */
    private boolean addMediaFilesToMinio (String localFilePath, String bucket, String objectName, String mimeType) {
        UploadObjectArgs uploadObjectArgs = null;
        try {
            uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .filename(localFilePath)
                    .object(objectName)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("上传文件成功，bucket:{}，objectName:{}", bucket, objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错，bucket:{}，objectName:{}，错误信息:{}", bucket, objectName, e.getMessage());
        }
        return false;
    }

    /**
     * @description 根据当天日期得到桶内默认存储文件夹路径
     * @return 存储文件夹路径
     */
    private String getDefaultFolderPath() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String folder = simpleDateFormat.format(new Date()).replace("-", "/") + "/";
        return folder;
    }

    /**
     * @description 根据文件计算得到md5值
     * @param file 文件
     * @return md5
     */
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)){
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @description 将文件信息保存到数据库
     * @param fileMd5 文件md5
     * @param uploadFileParamsDto 文件上传参数信息类
     * @param companyId 机构id
     * @param bucket 桶名称
     * @param objectName 对象名称（包括路径）
     * @return com.xuecheng.media.model.po.MediaFiles
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(String fileMd5, UploadFileParamsDto uploadFileParamsDto, Long companyId, String bucket,
                                         String objectName) {
        MediaFiles checkMediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (checkMediaFiles == null) {
            MediaFiles mediaFiles = new MediaFiles();
            //拷贝基本信息并补全信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            //保存文件信息到数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.debug("保存文件信息到数据库失败，错误信息：{}", mediaFiles.toString());
                return null;
            }
            log.debug("保存文件信息到数据库成功，文件信息：{}", mediaFiles.toString());
            return mediaFiles;
        }
        return checkMediaFiles;
    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {

        //得到文件名
        String fileName = uploadFileParamsDto.getFilename();
        //得到扩展名
        String extension = fileName.substring(fileName.lastIndexOf("."));
        //得到mimeType
        String mimeType = getMimeType(extension);
        //得到文件路径
        String defaultFolderPath = getDefaultFolderPath();
        //得到md5值作为对象名，和路径进行拼接
        String fileMd5 = getFileMd5(new File(localFilePath));
        String objectName = defaultFolderPath + fileMd5 + extension;
        //将文件上传到minio
        boolean result = addMediaFilesToMinio(localFilePath, bucket_mediaFiles, objectName, mimeType);
        if (!result) {
            XueChengPlusException.cast("上传文件失败");
        }
        //将文件信息保存到数据库，获得入库文件信息
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(fileMd5, uploadFileParamsDto, companyId, bucket_mediaFiles, objectName);
        if (mediaFiles == null) {
            XueChengPlusException.cast("保存文件上传信息到数据库失败");
        }
        //准备返回对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);

        return uploadFileResultDto;
    }
}
