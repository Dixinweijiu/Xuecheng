package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;

import java.util.List;

/**
 * @author jxchen
 * @version 1.0
 * @description 课程基本信息管理业务接口
 * @date 2023/9/11 11:23
 */
public interface TeachPlanService {

    /**
     * @description 查询课程计划树型结构
     * @param courseId 课程id
     * @return List<TeachPlanDto>
     */
    public List<TeachPlanDto> findTeachPlanTree(Long courseId);

    /**
     * @description 只在课程计划
     * @param saveTeachPlanDto 课程计划修改、新增的信息
     */
    public void saveTeachPlan(SaveTeachPlanDto saveTeachPlanDto);

    /**
     * @description 删除课程计划
     * @param teachPlanId 课程计划id
     */
    public void deleteTeachPlan(Long teachPlanId);

    /**
     * @description 根据移动类型上移或下移课程计划
     * @param movement 移动类型
     * @param teachPlanId 课程计划id
     */
    public void moveTeachPlan(String movement, Long teachPlanId);
}
