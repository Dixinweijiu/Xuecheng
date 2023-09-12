package com.xuecheng.content.model.dto;

import com.xuecheng.base.exception.ValidationGroups;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * @author jxchen
 * @version 1.0
 * @description TODO
 * @date 2023/9/12 16:20
 */
@Data
@ToString
public class SaveTeacherDto {

    @NotNull(groups = ValidationGroups.Insert.class, message = "课程id不能为空")
    @NotNull(groups = ValidationGroups.Update.class, message = "课程id不能为空")
    @ApiModelProperty(value = "课程id", required = true)
    public Long courseId;

    @NotNull(groups = ValidationGroups.Insert.class, message = "新增教师名称不能为空")
    @NotNull(groups = ValidationGroups.Update.class, message = "修改教师名称不能为空")
    @ApiModelProperty(value = "教师名称", required = true)
    public String teacherName;

    public Long id;

    public String position;

    public String introduction;
}
