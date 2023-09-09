package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @author jxchen
 * @version 1.0
 * @description TODO
 * @date 2023/9/8 20 20
 */
public interface CourseCategoryService {
    List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
