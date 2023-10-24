package com.xuecheng.content.service.jobHandler;

import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author jxchen
 * @version 1.0
 * @description TODO
 * @date 2023/10/10 16:15
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;

    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception{

        //分片参数
        int shardIndex = XxlJobHelper.getShardIndex(); // 获取执行器序号，从0开始
        int shardTotal = XxlJobHelper.getShardTotal(); // 获取执行器总数
        //调用抽象类方法执行任务
        process(shardIndex, shardTotal, "course_publish", 30, 60);

    }


    /**
     * @param mqMessage 执行任务内容
     * @return boolean true:处理成功，false处理失败
     * @description 课程发布任务处理
     * @author Mr.M
     * @date 2022/9/21 19:47
     */
    @Override
    public boolean execute(MqMessage mqMessage) {
        String businessKey1 = mqMessage.getBusinessKey1();
        long courseId = Integer.parseInt(businessKey1);
        //将课程静态化上传minio
        generateCourseHtml(mqMessage, courseId);
        //向elasticsearch写索引
        saveCourseIndex(mqMessage, courseId);
        //向redis写缓存
        saveCourseCache(mqMessage, courseId);
        //返回true表示任务完成
        return true;
    }

    /**
     * @description 第一阶段：生成课程静态化页面并上传minio
     * @param mqMessage 消息
     * @param courseId 课程id
     */
    private void generateCourseHtml(MqMessage mqMessage, long courseId){
        log.debug("开始进行课程静态化,课程id:{}",courseId);
        //消息id
        Long taskId = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        //做任务幂等性处理
        //取出该阶段执行状态
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne > 0) {
            //已处理了，无需再处理
            log.debug("课程静态化任务完成，无需处理...");
            return;
        }
        //开始处理
        //生成静态化页面
        File file = coursePublishService.generateCourseHtml(courseId);
        //上传静态化页面
        if (file != null) {
            coursePublishService.uploadCourseHtml(courseId, file);
        }
        //任务处理完成，将任务状态写为完成
        mqMessageService.completedStageOne(taskId);

    }

    /**
     * @description 第二阶段：向elasticsearch写索引
     * @param mqMessage 消息
     * @param courseId 课程id
     */
    private void saveCourseIndex(MqMessage mqMessage, long courseId){
        //消息id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //做任务幂等性处理
        //取出该阶段执行状态
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo > 0) {
            //已处理了，无需再处理
            log.debug("保存课程索引任务完成，无需处理...");
            return;
        }
        //开始处理

        //任务处理完成，将任务状态写为完成
        mqMessageService.completedStageTwo(taskId);
    }

    /**
     * @description 第三阶段：向redis写缓存
     * @param mqMessage 消息
     * @param courseId 课程id
     */
    private void saveCourseCache(MqMessage mqMessage, long courseId){
        //消息id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //做任务幂等性处理
        //取出该阶段执行状态
        int stageThree = mqMessageService.getStageThree(taskId);
        if (stageThree > 0) {
            //已处理了，无需再处理
            log.debug("写缓存任务完成，无需处理...");
            return;
        }
        //开始处理

        //任务处理完成，将任务状态写为完成
        mqMessageService.completedStageThree(taskId);
    }
}
