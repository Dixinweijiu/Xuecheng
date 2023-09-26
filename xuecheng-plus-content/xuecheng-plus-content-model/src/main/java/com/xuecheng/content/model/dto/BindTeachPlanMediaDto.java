package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @author jxchen
 * @version 1.0
 * @description 绑定课程计划和媒资的信息类
 * @date 2023/9/23 14:17
 */
@Data
@ToString
public class BindTeachPlanMediaDto {

    @ApiModelProperty(value = "媒资文件id", required = true)
    private String mediaId;

    @ApiModelProperty(value = "媒资文件名称", required = true)
    private String fileName;

    @ApiModelProperty(value = "课程计划id", required = true)
    private Long teachplanId;

}
