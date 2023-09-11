package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jxchen
 * @version 1.0
 * @description TODO
 * @date 2023/9/7 21:41
 */
@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        //拼接查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //根据名称模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                CourseBase::getName, queryCourseParamsDto.getCourseName());
        //根据课程审核状态精确查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        //根据课程发布状态精确查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),
                CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());
        //创建page分页参数对象，参数：当前页码，每页记录数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //开始进行分页查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        //数据列表
        List<CourseBase> items = pageResult.getRecords();
        //总记录数
        long count = pageResult.getTotal();
        //List<T> items, long counts, long pageNo, long pageSize
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(items, count, pageParams.getPageNo(),pageParams.getPageSize());
        //打印
        return courseBasePageResult;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {

        //参数的合法性校验
        if (StringUtils.isBlank(addCourseDto.getName())) {
//            throw new RuntimeException("课程名称为空");
            XueChengPlusException.cast("课程名称为空");
        }

        if (StringUtils.isBlank(addCourseDto.getMt())) {
            XueChengPlusException.cast("课程名称为空");
        }

        if (StringUtils.isBlank(addCourseDto.getSt())) {
            XueChengPlusException.cast("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getGrade())) {
            XueChengPlusException.cast("课程等级为空");
        }

        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
            XueChengPlusException.cast("教育模式为空");
        }

        if (StringUtils.isBlank(addCourseDto.getUsers())) {
            XueChengPlusException.cast("适应人群为空");
        }

        if (StringUtils.isBlank(addCourseDto.getCharge())) {
            XueChengPlusException.cast("收费规则为空");
        }

        //向课程基本信息表course_base写入信息
        CourseBase courseBaseNew = new CourseBase();
        //将传入的页面参数放到CourseBase新对象中
        BeanUtils.copyProperties(addCourseDto, courseBaseNew);
        courseBaseNew.setCompanyId(companyId);
        courseBaseNew.setCreateDate(LocalDateTime.now());
        //审核状态默认为未提交，发布状态默认为未发布
        courseBaseNew.setAuditStatus("202002");
        courseBaseNew.setStatus("203001");
        //插入数据库
        int insert = courseBaseMapper.insert(courseBaseNew);
        if (insert <= 0){
            XueChengPlusException.cast("课程插入失败");
        }
        //向课程营销信息表course_market写入信息
        CourseMarket courseMarketNew = new CourseMarket();
        //获得课程id
        Long courseId = courseBaseNew.getId();
        BeanUtils.copyProperties(addCourseDto, courseMarketNew);
        courseMarketNew.setId(courseId);
        //保存营销信息
        saveCourseMarket(courseMarketNew);
        //从数据库查询课程的详细信息，包含两部分，并返回
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseId);

        return courseBaseInfo;
    }



    //单独写一个方法查询课程信息
    /**
     * @param courseId 课程id
     * @return 课程信息
     * @description 根据课程id查询课程信息
     */
    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {

        //从课程信息表查询
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }
        //从课程营销表查询
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //组装在一起
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        //查找大分类小分类名称
        String mtName = courseCategoryMapper.selectById(courseBaseInfoDto.getMt()).getName();
        String stName = courseCategoryMapper.selectById(courseBaseInfoDto.getSt()).getName();
        courseBaseInfoDto.setMtName(mtName);
        courseBaseInfoDto.setStName(stName);


        return courseBaseInfoDto;

    }

    /**
     * @param companyId     机构id
     * @param editCourseDto 修改课程模型类
     * @return 课程信息
     * @description 修改课程
     */
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {

        //拿到课程id，查询课程信息
        Long courseId = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            XueChengPlusException.cast("课程不存在");
        }

        //数据合法性校验
        //本机构只能修改本机构的课程：companyId
        if (!companyId.equals(courseBase.getCompanyId())) {
            XueChengPlusException.cast("本机构只能修改自己旗下的课程");
        }

        //封装数据
        BeanUtils.copyProperties(editCourseDto, courseBase);
        //修改时间
        courseBase.setChangeDate(LocalDateTime.now());

        //更新数据库
        int i = courseBaseMapper.updateById(courseBase);
        if (i <= 0) {
            XueChengPlusException.cast("修改课程失败");
        }

        //更新营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto, courseMarket);
        saveCourseMarket(courseMarket);

        //查询课程信息
        CourseBaseInfoDto courseBaseInfoDto = getCourseBaseInfo(courseId);

        return courseBaseInfoDto;

    }

    //单独写一个方法保存课程营销信息
    private int saveCourseMarket(CourseMarket courseMarketNew) {
        //合法性校验
        //校验收费规则
        String charge = courseMarketNew.getCharge();
        if (StringUtils.isBlank(charge)) {
            XueChengPlusException.cast("收费规则为空");
        }
        //如果为收费，进行价格校验
        if (charge.equals("201001")) {
            //课程收费时，价格不能为空，且价格必须大于0
            if (courseMarketNew.getPrice() == null || courseMarketNew.getPrice() <= 0){
                XueChengPlusException.cast("课程收费时，价格不能为空，且价格必须大于0");
            }
        }

        //从数据库查询课程信息，如果有则进行更新，如果没有则进行新增
        CourseMarket courseMarket = courseMarketMapper.selectById(courseMarketNew.getId());
        if (courseMarket != null) {
            //不为空，进行更新
            BeanUtils.copyProperties(courseMarketNew, courseMarket);
            courseMarket.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarket);
        } else {
            //为空，进行新增
            return courseMarketMapper.insert(courseMarketNew);
        }

    }
}
