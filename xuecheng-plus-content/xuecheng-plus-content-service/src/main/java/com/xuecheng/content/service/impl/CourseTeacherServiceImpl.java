package com.xuecheng.content.service.impl;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.SaveTeacherDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author jxchen
 * @version 1.0
 * @description TODO
 * @date 2023/9/12 15:50
 */

@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    /**
     * @param courseId 课程信息
     * @return 教师信息数据类
     * @description 根据课程id查询教师信息
     */
    @Override
    public List<CourseTeacher> getTeacherInfoById(Long courseId) {
        return courseTeacherMapper.getTeacherInfoById(courseId);
    }

    /**
     * @param saveTeacherDto 课程教师数据模型类
     * @return 课程教师信息
     * @description 新增、修改课程教师
     */
    @Override
    public CourseTeacher saveCourseTeacher(Long companyId, SaveTeacherDto saveTeacherDto) {
        //数据校验：只能对自己机构的课程添加、修改教师
        CourseBase courseBase = courseBaseMapper.selectById(saveTeacherDto.getCourseId());
        if (!companyId.equals(courseBase.getCompanyId())) {
            XueChengPlusException.cast("本机构只能新增、修改自己旗下课程的教师");
        }

        //Dto中如果有id，则是修改；否则是新增
        if (saveTeacherDto.getId() == null) {
            //新增
            CourseTeacher newCourseTeacher = new CourseTeacher();
            BeanUtils.copyProperties(saveTeacherDto, newCourseTeacher);
            courseTeacherMapper.insert(newCourseTeacher);
            return newCourseTeacher;
        } else {
            //修改
            CourseTeacher courseTeacher = new CourseTeacher();
            BeanUtils.copyProperties(saveTeacherDto, courseTeacher);
            courseTeacherMapper.updateById(courseTeacher);
            return courseTeacher;
        }
    }

    @Override
    public void deleteCourseTeacher(Long companyId, Long courseId, Long courseTeacherId) {
        //数据校验：只能对自己机构的课程删除教师
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!companyId.equals(courseBase.getCompanyId())) {
            XueChengPlusException.cast("本机构只能新增、修改自己旗下课程的教师");
        }

        courseTeacherMapper.deleteById(courseTeacherId);

    }

}
