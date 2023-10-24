package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachPlanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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

    @Autowired
    CourseTeacherService courseTeacherService;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    MediaServiceClient mediaServiceClient;


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
        coursePreviewDto.setCourseTeachers(courseTeacherService.getTeacherInfoById(courseId));
        return coursePreviewDto;
    }

    /**
     * @param companyId 机构id
     * @param courseId  课程id
     * @description 提交审核
     */
    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        //查询课程基本信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if (courseBaseInfo == null) {
            //找不到课程
            XueChengPlusException.cast("找不到课程");
        }
        //本机构只能提交本机构的课程
        if (!Objects.equals(companyId, courseBaseInfo.getCompanyId())) {
            //不允许提交
            XueChengPlusException.cast("本机构只能提交本机构的课程");
        }
        //如果课程的审核状态为已提交则不允许提交
        if (courseBaseInfo.getAuditStatus().equals("202003")) {
            //不允许提交
            XueChengPlusException.cast("课程已提交审核，不能再次提交，请等待审核");
        }
        //课程的图片、计划信息没有填写也不允许提交
        if (StringUtils.isEmpty(courseBaseInfo.getPic())) {
            //不允许提交
            XueChengPlusException.cast("课程没有图片，不能提交，请上传课程图片");
        }
        //查询课程计划
        List<TeachPlanDto> teachPlanTree = teachPlanService.findTeachPlanTree(courseId);
        if (teachPlanTree == null || teachPlanTree.size() == 0) {
            //不允许提交
            XueChengPlusException.cast("没有课程计划，不能提交，请上传课程信息");
        }
        //查询到课程基本信息、营销信息、计划等信息插入到课程预发布表
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        //营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        List<CourseTeacher> teachers = courseTeacherService.getTeacherInfoById(courseId);
        coursePublishPre.setMarket(JSON.toJSONString(courseMarket));
        coursePublishPre.setTeachplan(JSON.toJSONString(teachPlanTree));
        coursePublishPre.setTeachers(JSON.toJSONString(teachers));
        coursePublishPre.setUsername(courseBaseInfo.getCreatePeople());
        coursePublishPre.setCreateDate(LocalDateTime.now());
        coursePublishPre.setStatus("202003");
        //插入前先判断表里有没有 有就更新 没有就插入
        CoursePublishPre coursePublishPre1 = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre1 == null) {
            //插入
            int insert = coursePublishPreMapper.insert(coursePublishPre);
            if (insert < 0) {
                XueChengPlusException.cast("插入预发布表失败");
            }
        } else {
            //更新
            int update = coursePublishPreMapper.updateById(coursePublishPre);
            if (update < 0) {
                XueChengPlusException.cast("更新预发布表失败");
            }
        }
        //更新课程基本信息表的审核状态为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");
        int update = courseBaseMapper.updateById(courseBase);
        if (update < 0) {
            XueChengPlusException.cast("更新审核状态失败");
        }
    }

    /**
     * @param courseId 课程id
     * @description 发布课程
     */
    @Transactional
    @Override
    public void coursePublish(Long companyId, Long courseId) {
        //提取课程信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            //查询不到课程
            XueChengPlusException.cast("课程不存在，无法发布");
        }
        if (!courseBase.getCompanyId().equals(companyId)) {
            //机构id不同
            XueChengPlusException.cast("本机构只允许发布本机构的课程，无法发布");
        }
        //从预发布表中取信息
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            //如果预发布表里不存在
            XueChengPlusException.cast("课程未提交审核，无法发布");
        }
        String status = coursePublishPre.getStatus();
        if (Objects.equals(status, "202003")) {
            //如果预发布表里审核状态为已提交
            XueChengPlusException.cast("课程未审核，无法发布");
        }
        if (Objects.equals(status, "202001")) {
            //如果预发布表里审核状态为审核不通过
            XueChengPlusException.cast("课程未通过审核，无法发布");
        }
        if (Objects.equals(status, "202004")) {
            //通过审核，可以发布
            saveCoursePublish(courseId);
            //向mq_message表插入信息
            saveCoursePublishMessage(courseId);
            //删除预发布表信息
            coursePublishPreMapper.deleteById(courseId);
        }
    }

    /**
     * @param courseId 课程id
     * @return 静态化html
     * @description 生成课程静态化页面
     */
    @Override
    public File generateCourseHtml(Long courseId) {

        //静态化文件
        File htmlFile = null;

        try {//配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //拿到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            //指定模板的目录
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");
            //获得模板
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = getCoursePreviewInfo(courseId);

            HashMap<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //需要模板和数据
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            //将静态化内容输出到文件
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile("course", ".html");
            log.debug("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("课程静态化异常:{}",e.toString());
            XueChengPlusException.cast("课程静态化异常");
        }

        return htmlFile;
    }

    /**
     * @param courseId 课程id
     * @param file     静态化html
     * @description 上传课程静态化页面
     */
    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        //获取分片文件
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        //获取静态化页面文件名
        String fileName = "course/" + courseId.toString() + ".html";
        //调用远程媒资服务上传
        String s = mediaServiceClient.uploadFile(multipartFile, fileName);
        if (s == null) {
            XueChengPlusException.cast("上传静态文件异常");
        }
    }

    /**
     * @description 根据课程id保存课程发布信息
     * @param courseId 课程id
     */
    private void saveCoursePublish(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (courseBase == null) {
            //查询不到课程
            XueChengPlusException.cast("课程不存在，无法发布");
        }
        if (coursePublishPre == null) {
            //查询不到课程
            XueChengPlusException.cast("课程发布信息为空，无法发布");
        }
        //更新课程表信息
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);
        //插入发布表
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        coursePublish.setStatus("203002");
        CoursePublish updateCoursePublish = coursePublishMapper.selectById(courseId);
        if (updateCoursePublish == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }
    }

    /**
     * @description 向消息表写课程发表信息
     * @param courseId 课程id
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage coursePublish = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (coursePublish == null) {
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }



}
