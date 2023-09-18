package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;

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


}
