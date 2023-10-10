package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

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
}
