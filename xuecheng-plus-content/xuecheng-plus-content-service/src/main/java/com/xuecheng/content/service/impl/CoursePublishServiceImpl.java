package com.xuecheng.content.service.impl;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jxchen
 * @version 1.0
 * @description TODO
 * @date 2023/9/25 16:58
 */
@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachPlanService teachPlanService;

//    @Autowired
//    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseTeacherService courseTeacherService;

    /**
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     * @description 获取课程预览信息
     */
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfoService.getCourseBaseInfo(courseId));
        coursePreviewDto.setTeachplans(teachPlanService.findTeachPlanTree(courseId));
//        coursePreviewDto.setCourseMarket(courseMarketMapper.selectById(courseId));
        coursePreviewDto.setCourseTeachers(courseTeacherService.getTeacherInfoById(courseId));
        return coursePreviewDto;
    }
}
