package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jxchen
 * @version 1.0
 * @description TODO
 * @date 2023/9/8 20 21
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        //调用mapper递归查询出递归信息
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        //找到每个节点的子节点，最终封装成List<CourseCategoryTreeDto>
        //将List转换为map，key就是节点的id，value就是CourseCategoryTreeDto对象，方便从map获取节点，通过filter排除根节点
        Map<String, CourseCategoryTreeDto> mapTemp = courseCategoryTreeDtos.stream().filter(item->!id.equals(item.getId())).collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));
        //定义一个List作为最终返回的List
        List<CourseCategoryTreeDto> categoryTreeDtoList = new ArrayList<>();
        //从头遍历List<CourseCategoryTreeDto>，一边遍历一边找子节点放在父节点的childrenTreeNodes上
        courseCategoryTreeDtos.stream().filter(item->!id.equals(item.getId())).forEach(item->{
            //开始处理
            if(item.getParentid().equals(id)){
                categoryTreeDtoList.add(item);
            }
            //找到节点的父节点
            CourseCategoryTreeDto courseCategoryTreeParentDto = mapTemp.get(item.getParentid());
            //判断是否为空
            if (courseCategoryTreeParentDto != null){
                //不为空
                if (courseCategoryTreeParentDto.getChildrenTreeNodes() == null){
                    //若父节点ChildrenTreeNodes属性为空，则new一个
                    courseCategoryTreeParentDto.setChildrenTreeNodes(new ArrayList<>());
                }
                //找到子节点放入父节点的childrenTreeNodes
                courseCategoryTreeParentDto.getChildrenTreeNodes().add(item);
            }


        });


        return categoryTreeDtoList;
    }
}
