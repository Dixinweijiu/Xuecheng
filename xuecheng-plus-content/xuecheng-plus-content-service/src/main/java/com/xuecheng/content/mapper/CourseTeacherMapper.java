package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface CourseTeacherMapper extends BaseMapper<CourseTeacher> {

    List<CourseTeacher> getTeacherInfoById(Long courseId);

    CourseTeacher getTeacherInfo(@Param("courseId") Long courseId, @Param("teacherName") String teacherName);

}
