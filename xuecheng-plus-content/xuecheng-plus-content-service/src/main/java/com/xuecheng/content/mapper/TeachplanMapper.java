package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import org.apache.ibatis.annotations.Param;

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

    /**
     * @description 查找大章节下小章节的最大排序字段值
     * @param courseId 课程id
     * @param parentId 父节点id
     * @return 最大排序字段值
     */
    public Integer selectMaxOrderBy(@Param("courseId") Long courseId, @Param("parentId") Long parentId);

    /**
     * @description 统计大章节计划下有小章节的数量
     * @param teachPlanId 课程计划id
     * @return 小章节数量
     */
    public int getChildrenNum(Long teachPlanId);

    public Teachplan getUpperTeachPlan(Long teachPlanId);

    public Teachplan getLowerTeachPlan(Long teachPlanId);
}
