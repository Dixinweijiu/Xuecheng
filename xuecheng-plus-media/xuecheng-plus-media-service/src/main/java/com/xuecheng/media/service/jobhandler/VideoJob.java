package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author jxchen
 * @version 1.0
 * @description 分片视频处理任务
 * @date 2023/9/20 22:13
 */
@Component
@Slf4j
public class VideoJob {

    @Autowired
    MediaFileService mediaFileService;

    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegPath;

    /**
     * 分片视频处理任务
     */
    @XxlJob("videoJobHandler")
    public void videoJob() throws Exception {
        //获取分片总数和分片序号
        int shardTotal = XxlJobHelper.getShardTotal();
        int shardIndex = XxlJobHelper.getShardIndex();

        //确定CPU核心数量
        int processors = Runtime.getRuntime().availableProcessors();

        //查询待处理的任务 5 -> processors
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardTotal, shardIndex, 5);
        //任务数量
        int size = mediaProcessList.size();
        log.debug("取到的视频处理任务数量:{}", size);
        if (size <= 0) {
            return;
        }

        //创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        //使用计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> {
            //将任务加入线程池
            executorService.execute(() -> {
                try {
                    //任务id
                    Long taskId = mediaProcess.getId();
                    //开启任务
                    boolean b = mediaFileProcessService.startTask(taskId);
                    if (!b) {
                        //失败
                        log.debug("抢占任务失败，任务id:{}", taskId);
                        return;
                    }
                    //md5
                    String fileId = mediaProcess.getFileId();
                    //桶
                    String bucket = mediaProcess.getBucket();
                    //对象名
                    String objectName = mediaProcess.getFilePath();
                    //文件下载到本地
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                    if (file == null) {
                        log.debug("下载视频出错：null，任务id:{}, bucket:{}, objectName:{}", taskId, bucket, objectName);
                        //保存任务处理失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
                        return;
                    }
                    //ffmpeg的路径
                    String ffmpeg_path = ffmpegPath;//ffmpeg的安装位置
                    //源avi视频的路径
                    String video_path = file.getAbsolutePath();
                    //转换后mp4文件的名称
                    String mp4_name = fileId + ".mp4";
                    //创建临时文件
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.debug("创建临时文件异常，任务id:{}，异常信息：{}", taskId, e.getMessage());
                        //保存任务处理失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件异常");
                        return;
                    }
                    //转换后mp4文件的路径
                    String mp4_path = mp4File.getAbsolutePath();
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4_path);
                    //开始视频转换，成功将返回success
                    String result = videoUtil.generateMp4();
                    if (!result.equals("success")) {
                        log.debug("视频转换格式异常, bucket:{}, objectName:{}, 异常信息:{}", bucket, objectName, result);
                        //保存任务处理失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, result);
                        return;
                    }
                    //上传到minio
                    //objectName还是avi的、另外没加桶名称
                    boolean b1 = mediaFileService.addMediaFilesToMinio(mp4_path, bucket, objectName, "video/mp4");
                    if (!b1) {
                        log.debug("上传mp4到minio失败，任务id:{}", taskId);
                        //保存任务处理失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传mp4到minio失败");
                        return;
                    }
                    //拼接url
                    String url = getFilePathByMd5(fileId, ".mp4");
                    //保存成功结果
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, null);
                } finally {
                    //计数器-1
                    countDownLatch.countDown();
                }
            });

        });

        //阻塞,指定最大限度等待时间
        countDownLatch.await(30, TimeUnit.MINUTES);


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

}
