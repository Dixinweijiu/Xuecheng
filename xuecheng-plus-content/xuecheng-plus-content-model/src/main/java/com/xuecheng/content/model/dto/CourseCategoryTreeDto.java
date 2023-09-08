package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author jxchen
 * @version 1.0
 * @description 课程分类树形节点Dto
 * @date 2023/9/8 16 00
 */
@Data
@ToString
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {

    @ApiModelProperty("课程分类下级结点信息")
    List<CourseCategoryTreeDto> childrenTreeNodes;

}
