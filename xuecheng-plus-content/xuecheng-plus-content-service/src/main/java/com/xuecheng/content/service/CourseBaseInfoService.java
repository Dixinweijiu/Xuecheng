package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * @author jxchen
 * @version 1.0
 * @description 课程信息管理接口
 * @date 2023/9/7 21 35
 */
public interface CourseBaseInfoService {

    /**
     * @description 课程查询接口
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.content.model.po.CourseBase>
     * @auther Jxchen
     * @date 2023/9/7 21 40
     */
    //课程分页查询
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * @description 新增课程
     * @param companyId 机构id
     * @param addCourseDto 新增课程模型类
     * @return 课程信息
     */
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);


    /**
     * @description 根据课程id查询课程信息
     * @param courseId 课程id
     * @return 课程信息
     */
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * @description 修改课程
     * @param companyId 机构id
     * @param editCourseDto 修改课程模型类
     * @return 课程信息
     */
    CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);


    void deleteCourseBase(Long companyId, Long courseId);

}
