package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author jxchen
 * @version 1.0
 * @description 课程计划树形模型类
 * @date 2023/9/11 10:12
 */
@Data
@ToString
public class TeachPlanDto extends Teachplan {

    //课程计划相关媒体资源
    TeachplanMedia teachplanMedia;

    //子节点
    List<TeachPlanDto> teachPlanTreeNodes;
}
