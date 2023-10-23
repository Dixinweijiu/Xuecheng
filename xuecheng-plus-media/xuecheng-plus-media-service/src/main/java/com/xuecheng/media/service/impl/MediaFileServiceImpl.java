package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Autowired
    MediaFileService currentProxy;

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    //存储普通文件
    @Value("${minio.bucket.files}")
    private String bucket_mediaFiles;
    //存储视频文件
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

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
     * @param extension 扩展名
     * @return mimeType
     * @description 根据扩展名获得mimeType
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
     * @param localFilePath 本地文件路径
     * @param bucket        桶名称
     * @param objectName    对象名称
     * @param mimeType      mimeType
     * @return 上传结果
     * @description 上传文件到Minio
     */
    public boolean addMediaFilesToMinio(String localFilePath, String bucket, String objectName, String mimeType) {
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
     * @param mediaId 媒资id
     * @return com.xuecheng.media.model.po.MediaFiles
     * @description 根据媒资id获取媒资对象
     */
    @Override
    public MediaFiles getFileById(String mediaId) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
        return mediaFiles;
    }

    /**
     * @return 存储文件夹路径
     * @description 根据当天日期得到桶内默认存储文件夹路径
     */
    private String getDefaultFolderPath() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String folder = simpleDateFormat.format(new Date()).replace("-", "/") + "/";
        return folder;
    }

    /**
     * @param file 文件
     * @return md5
     * @description 根据文件计算得到md5值
     */
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param fileMd5             文件md5
     * @param uploadFileParamsDto 文件上传参数信息类
     * @param companyId           机构id
     * @param bucket              桶名称
     * @param objectName          对象名称（包括路径）
     * @return com.xuecheng.media.model.po.MediaFiles
     * @description 将文件信息保存到数据库
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

            //记录待处理任务
            addWaitingTask(mediaFiles);

            return mediaFiles;
        }
        return checkMediaFiles;
    }

    /**
     * @description 添加待处理任务
     * @param mediaFiles 媒体文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles) {
        //获得文件的mimeType
        String filename = mediaFiles.getFilename();
        //获得扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //利用方法获得mimeType
        String mimeType = getMimeType(extension);
        //判断是否是avi
        if (mimeType.equals("video/x-msvideo")) {
            //写入视频处理任务
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);
            mediaProcess.setUrl(null);
            mediaProcessMapper.insert(mediaProcess);
        }
    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath,
                                          String objectName) {

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
        //获取存储的对象名
        if (StringUtils.isEmpty(objectName)){
            objectName = defaultFolderPath + fileMd5 + extension;
        }
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

    /**
     * @param fileMd5 Md5
     * @return 是否存在
     * @description 文件上传前检查是否已于数据库/minio存在
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //先查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            //文件所在桶
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();
            //再查询minio
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build();
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream != null) {
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResponse.success(false);
    }

    /**
     * @param fileMd5 md5
     * @return 路径
     * @description 由md5得到minio中的目录路径
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/chunk/";
    }

    /**
     * @param fileMd5    Md5
     * @param chunkIndex 分块序号
     * @return 是否存在
     * @description 分块文件上传前检查是否已于数据库/minio存在
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //根据md5得到路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //再查询minio
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkFileFolderPath + chunkIndex)
                .build();
        InputStream fileInputStream = null;
        try {
            fileInputStream = minioClient.getObject(getObjectArgs);
            if (fileInputStream != null) {
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.success(false);
    }

    /**
     * @param fileMd5            md5
     * @param chunk              分块序号
     * @param localChunkFilePath 分块文件本地路径
     * @return 响应
     * @description 上传分块文件
     */
    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        //分块文件路径
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;
        //得到mimeType
        String mimeType = getMimeType(null);
        //将分块文件上传到Minio
        boolean b = addMediaFilesToMinio(localChunkFilePath, bucket_video, chunkFilePath, mimeType);
        if (!b) {
            //上传失败
            return RestResponse.validfail(false, "上传分块文件失败");
        }
        //上传成功
        return RestResponse.success(true);
    }

    /**
     * @param fileMd5 md5
     * @param fileExt 文件扩展名
     * @return 文件对象目录及名
     * @description 根据md5得到文件名
     */
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }


    /**
     * @param bucket     桶
     * @param objectName 对象名称
     * @return 下载后的文件
     * @description 从minio下载文件
     */
    public File downloadFileFromMinIO(String bucket, String objectName) {
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile = File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream, outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal          分块文件总数
     * @description 清除分块文件
     */
    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal) {

        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("video").objects(deleteObjects).build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(r -> {
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清除分块文件失败,objectname:{}", deleteError.objectName(), e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清除分块文件失败,chunkFileFolderPath:{}", chunkFileFolderPath, e);
        }
    }


    /**
     * @param companyId           机构id
     * @param fileMd5             md5
     * @param chunkTotal          分块总数
     * @param uploadFileParamsDto 文件上传信息
     * @return 响应
     * @description 合并分块
     */
    @Override
    public RestResponse mergeChunk(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //分块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //找到分块文件
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucket_video)
                        .object(chunkFileFolderPath.concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());
        //源文件名
        String fileName = uploadFileParamsDto.getFilename();
        //扩展名
        String extension = fileName.substring(fileName.lastIndexOf("."));
        //合并得到文件的objectName
        String objectName = getFilePathByMd5(fileMd5, extension);
        //Minio合并参数
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(objectName)
                .sources(sources)
                .build();
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错，bucket:{}，objectName:{}，错误信息:{}", bucket_video, objectName, e.getMessage());
            return RestResponse.validfail(false, "合并文件异常");
        }

        //校验合并后的md5和源文件的是否一致，一致才算上传成功
        File minioFile = downloadFileFromMinIO(bucket_video, objectName);
        if (minioFile == null) {
            log.debug("下载合并后文件失败，mergeFilePath:{}", objectName);
            return RestResponse.validfail(false, "下载合并后文件失败");
        }

        try (InputStream newFileInputStream = Files.newInputStream(minioFile.toPath())) {
            //minio上文件的md5值
            String mergeMd5 = DigestUtils.md5Hex(newFileInputStream);
            //比较md5值，不一致则说明文件不完整
            if (!fileMd5.equals(mergeMd5)) {
                log.error("文件合并校验失败，原始文件：{}，合并文件：{}", fileMd5, mergeMd5);
                return RestResponse.validfail(false, "文件合并校验失败，最终上传失败");
            }
            //文件大小
            uploadFileParamsDto.setFileSize(minioFile.length());
        } catch (Exception e) {
            log.debug("校验文件失败,fileMd5:{},异常:{}", fileMd5, e.getMessage(), e);
            return RestResponse.validfail(false, "文件合并校验失败，最终上传失败");
        } finally {
            if (minioFile != null) {
                minioFile.delete();
            }
        }

        //将文件信息入库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(fileMd5, uploadFileParamsDto, companyId, bucket_video, objectName);
        if (mediaFiles == null) {
            return RestResponse.validfail(false, "文件入库失败");
        }

        //清理分块文件
        clearChunkFiles(chunkFileFolderPath, chunkTotal);

        return RestResponse.success(true);
    }
}
