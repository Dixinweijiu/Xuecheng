package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    /**
     * @description 根据课程id查询课程计划树形结构
     * @param courseId 课程id
     * @return 课程计划树形结构
     */
    public List<TeachPlanDto> selectTreeNodes(Long courseId);
}
