package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jxchen
 * @version 1.0
 * @description TODO
 * @date 2023/9/26 11:37
 */
@Api(value = "课程公开查询接口", tags = "课程公开查询接口")
@RestController
@RequestMapping("/open")
public class CourseOpenController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    CoursePublishService coursePublishService;

    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable("courseId") Long courseId){
        CoursePreviewDto coursePreviewDto = coursePublishService.getCoursePreviewInfo(courseId);

        return coursePreviewDto;
    }
}
