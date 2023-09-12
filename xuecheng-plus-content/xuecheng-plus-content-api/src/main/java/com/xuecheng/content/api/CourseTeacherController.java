package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author jxchen
 * @version 1.0
 * @description TODO
 * @date 2023/9/12 15:45
 */

@Api(value = "课程教师管理接口",tags = "课程教师管理接口")
@Slf4j
@RestController
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;


    @ApiOperation("查询课程下的教师列表")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> getTeacherInfoById(@PathVariable Long courseId){
        List<CourseTeacher> courseTeacherList = courseTeacherService.getTeacherInfoById(courseId);
        return courseTeacherList;
    };


    @ApiOperation("新增、修改课程教师")
    @PostMapping("/courseTeacher")
    public CourseTeacher saveCourseTeacher(@RequestBody SaveTeacherDto saveTeacherDto){

        Long companyId = 1232141425L;

        CourseTeacher courseTeacher = courseTeacherService.saveCourseTeacher(companyId, saveTeacherDto);
        return courseTeacher;
    }

    @ApiOperation("删除课程教师")
    @DeleteMapping("/courseTeacher/course/{courseId}/{courseTeacherId}")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long courseTeacherId) {

        Long companyId = 1232141425L;

        courseTeacherService.deleteCourseTeacher(companyId, courseId, courseTeacherId);
    }

}
