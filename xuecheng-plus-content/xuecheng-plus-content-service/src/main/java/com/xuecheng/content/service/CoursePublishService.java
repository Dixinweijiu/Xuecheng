package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

import java.io.File;

/**
 * @author jxchen
 * @version 1.0
 * @description 课程预览、课程发布接口
 * @date 2023/9/25 16:55
 */
public interface CoursePublishService {

    /**
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     * @description 获取课程预览信息
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * @param companyId 机构id
     * @param courseId  课程id
     * @description 提交审核
     */

    public void commitAudit(Long companyId, Long courseId);


    /**
     * @param companyId 机构id
     * @param courseId  课程id
     * @description 发布课程
     */
    public void coursePublish(Long companyId, Long courseId);


    /**
     * @description 生成课程静态化页面
     * @param courseId 课程id
     * @return 静态化html
     */
    public File generateCourseHtml(Long courseId);


    /**
     * @description 上传课程静态化页面
     * @param courseId 课程id
     * @param file 静态化html
     */
    public void uploadCourseHtml(Long courseId, File file);
}
