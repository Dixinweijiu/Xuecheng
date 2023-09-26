package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseTeacher;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author jxchen
 * @version 1.0
 * @description 课程预览模型类
 * @date 2023/9/25 16:53
 */
@Data
@ToString
public class CoursePreviewDto {

    //课程基本信息
    CourseBaseInfoDto courseBase;

    //课程计划信息
    List<TeachPlanDto> teachplans;

    //课程营销信息
//    CourseMarket courseMarket;

    //教师信息
    List<CourseTeacher> courseTeachers;

}
