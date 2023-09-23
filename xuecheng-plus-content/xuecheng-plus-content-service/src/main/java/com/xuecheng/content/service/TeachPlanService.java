package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachPlanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;

/**
 * @author jxchen
 * @version 1.0
 * @description 课程基本信息管理业务接口
 * @date 2023/9/11 11:23
 */
public interface TeachPlanService {

    /**
     * @param courseId 课程id
     * @return List<TeachPlanDto>
     * @description 查询课程计划树型结构
     */
    public List<TeachPlanDto> findTeachPlanTree(Long courseId);

    /**
     * @param saveTeachPlanDto 课程计划修改、新增的信息
     * @description 只在课程计划
     */
    public void saveTeachPlan(SaveTeachPlanDto saveTeachPlanDto);

    /**
     * @param teachPlanId 课程计划id
     * @description 删除课程计划
     */
    public void deleteTeachPlan(Long teachPlanId);

    /**
     * @param movement    移动类型
     * @param teachPlanId 课程计划id
     * @description 根据移动类型上移或下移课程计划
     */
    public void moveTeachPlan(String movement, Long teachPlanId);


    /**
     * @param bindTeachPlanMediaDto 课程计划和媒资绑定信息类
     * @return com.xuecheng.content.model.po.TeachplanMedia
     * @description 课程计划和媒资信息绑定
     */
    public TeachplanMedia associaitonMedia(BindTeachPlanMediaDto bindTeachPlanMediaDto);
}
