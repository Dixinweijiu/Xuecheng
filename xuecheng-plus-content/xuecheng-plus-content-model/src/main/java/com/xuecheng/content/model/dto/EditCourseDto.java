package com.xuecheng.content.model.dto;

import com.xuecheng.base.exception.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @description 添加课程dto
 * @author Jxchen
 * @date 2023/9/10 20:10
 * @version 1.0
 */
@Data
@ApiModel(value="EditCourseDto", description="修改课程基本信息")
public class EditCourseDto extends AddCourseDto{

    @NotNull(groups = ValidationGroups.Update.class, message = "修改课程id不能为空")
    @ApiModelProperty(value = "课程id", required = true)
    private Long id;

}
