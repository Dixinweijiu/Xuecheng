package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;

import java.io.File;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author Mr.M
     * @date 2022/9/10 8:57
     */
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


    /**
     * @description 上传文件
     * @param companyId 机构id
     * @param uploadFileParamsDto 文件上传参数信息模型类
     * @param localFilePath 本地文件路径
     * @return 文件上传响应信息模型类
     */
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath);


    /**
     * @description 将文件信息保存到数据库
     * @param fileMd5 文件md5
     * @param uploadFileParamsDto 文件上传参数信息类
     * @param companyId 机构id
     * @param bucket 桶名称
     * @param objectName 对象名称（包括路径）
     * @return com.xuecheng.media.model.po.MediaFiles
     */
    public MediaFiles addMediaFilesToDb(String fileMd5, UploadFileParamsDto uploadFileParamsDto, Long companyId, String bucket,
                                        String objectName);


    /**
     * @description 文件上传前检查是否已于数据库/minio存在
     * @param fileMd5 Md5
     * @return 是否存在
     */
    public RestResponse<Boolean> checkFile(String fileMd5);


    /**
     * @description 分块文件上传前检查是否已于数据库/minio存在
     * @param fileMd5 Md5
     * @param chunkIndex 分块序号
     * @return 是否存在
     */
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);


    /**
     * @description 上传分块文件
     * @param fileMd5 md5
     * @param chunk 分块序号
     * @param localChunkFilePath 分块文件本地路径
     * @return 响应
     */
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath);


    /**
     * @description 合并分块
     * @param companyId 机构id
     * @param fileMd5 md5
     * @param chunkTotal 分块总数
     * @param uploadFileParamsDto 文件上传信息
     * @return 响应
     */
    public RestResponse mergeChunk(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);


    /**
     * @param bucket     桶
     * @param objectName 对象名称
     * @return 下载后的文件
     * @description 从minio下载文件
     */
    public File downloadFileFromMinIO(String bucket, String objectName);

    /**
     * @param localFilePath 本地文件路径
     * @param bucket        桶名称
     * @param objectName    对象名称
     * @param mimeType      mimeType
     * @return 上传结果
     * @description 上传文件到Minio
     */
    public boolean addMediaFilesToMinio(String localFilePath, String bucket, String objectName, String mimeType);
}
