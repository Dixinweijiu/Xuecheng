package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * @author jxchen
 * @version 1.0
 * @description 视频文件任务处理
 * @date 2023/9/20 20:17
 */
public interface MediaFileProcessService {


    /**
     * @param shardTotal 分片总数
     * @param shardIndex 分片序号
     * @param count       任务数
     * @return 任务列表
     * @description 根据分片参数获取待处理任务
     */
    public List<MediaProcess> getMediaProcessList(int shardTotal, int shardIndex, int count);


    /**
     * @param id 任务id
     * @return 更新记录数
     * @description 开启一个任务
     */
    public boolean startTask(long id);


    /**
     * @param taskId   任务id
     * @param status   任务状态
     * @param fileId   文件id
     * @param url      url
     * @param errorMsg 错误信息
     * @description 保存任务结果
     * @author Mr.M
     * @date 2022/10/15 11:29
     */

    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);
}
