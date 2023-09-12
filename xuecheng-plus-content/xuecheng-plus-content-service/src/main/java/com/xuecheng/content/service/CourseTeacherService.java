package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @author jxchen
 * @version 1.0
 * @description TODO
 * @date 2023/9/12 15:48
 */
public interface CourseTeacherService {

    /**
     * @description 根据课程id查询教师信息
     * @param courseId 课程信息
     * @return 教师信息数据类
     */
    public List<CourseTeacher> getTeacherInfoById(Long courseId);


    /**
     * @description 新增课程教师
     * @param companyId 机构id
     * @param saveTeacherDto 课程教师数据模型类
     * @return 课程教师信息
     */
    public CourseTeacher saveCourseTeacher(Long companyId, SaveTeacherDto saveTeacherDto);


    public void deleteCourseTeacher(Long companyId, Long courseId, Long courseTeacherId);

}
