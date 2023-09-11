package com.xuecheng.content.service;

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
     * @return List<TeachplanDto>
     */
    public List<TeachPlanDto> findTeachPlanTree(Long courseId);
}
