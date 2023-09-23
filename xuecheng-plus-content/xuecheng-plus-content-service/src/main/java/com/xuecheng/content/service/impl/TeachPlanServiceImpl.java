package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachPlanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jxchen
 * @version 1.0
 * @description TODO
 * @date 2023/9/11 11:25
 */
@Service
public class TeachPlanServiceImpl implements TeachPlanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    /**
     * @param courseId 课程id
     * @return List<TeachplanDto>
     * @description 查询课程计划树型结构
     */
    @Override
    public List<TeachPlanDto> findTeachPlanTree(Long courseId) {
        List<TeachPlanDto> teachPlanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachPlanDtos;
    }

    /**
     * @param saveTeachPlanDto 课程计划修改、新增的信息
     * @description 只在课程计划
     */
    @Override
    public void saveTeachPlan(SaveTeachPlanDto saveTeachPlanDto) {
        //课程计划id
        Long id = saveTeachPlanDto.getId();
        //如果存在id则是修改，如果不存在则是新增
        if (id != null) {
            //修改
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachPlanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        } else {
            //新增，需要确定order_by的值
            int count = teachplanMapper.selectMaxOrderBy(saveTeachPlanDto.getCourseId(), saveTeachPlanDto.getParentid());
            Teachplan teachplan = new Teachplan();
            teachplan.setOrderby(count + 1);
            BeanUtils.copyProperties(saveTeachPlanDto, teachplan);
            teachplanMapper.insert(teachplan);
        }
    }

    /**
     * @param teachPlanId 课程计划id
     * @description 删除课程计划
     */
    @Override
    public void deleteTeachPlan(Long teachPlanId) {
        //找到对应的课程计划
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        //根据大章节或者小章节，有不同的删除策略
        if (teachplan.getParentid() == 0) {
            //大章节——如果下面有小章节，那么就不可以删除
            //判断下面是否有小章节
            if (teachplanMapper.getChildrenNum(teachPlanId) == 0) {
                //没有小章节 可以删除
                teachplanMapper.deleteById(teachPlanId);
            } else {
                //有小章节，返回异常
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
            }
        } else {
            //小章节，可以删除
            teachplanMapper.deleteById(teachPlanId);
            //删除媒体资源表信息 先查找有没有
            Long mediaId = teachplanMediaMapper.getIdByTeachPlanId(teachPlanId);
            if (mediaId != null) {
                //有媒体资源 删除
                int i = teachplanMediaMapper.deleteById(mediaId);
                if (i <= 0) {
                    XueChengPlusException.cast("删除课程计划对应媒体资源失败");
                }
            }
        }
    }

    /**
     * @param movement    移动类型
     * @param teachPlanId 课程计划id
     * @description 根据移动类型上移或下移课程计划
     */
    @Override
    public void moveTeachPlan(String movement, Long teachPlanId) {
        //获得要换位的teachPlan
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);

        if (movement.equals("movedown")) {
            //下移
            //获取下方计划作为目标
            Teachplan aimTeachPlan = teachplanMapper.getLowerTeachPlan(teachPlanId);
            exchangeOrderBy(teachplan, aimTeachPlan);
        } else if (movement.equals("moveup")) {
            //上移
            //获取上方计划作为目标
            Teachplan aimTeachPlan = teachplanMapper.getUpperTeachPlan(teachPlanId);
            exchangeOrderBy(teachplan, aimTeachPlan);
        } else {
            XueChengPlusException.cast("移动类型错误");
        }
    }

    /**
     * @param teachPlan    要交换的teachPlan
     * @param aimTeachPlan 目标teachPlan
     * @description 交换两个teachPlan的orderBy字段
     */
    private void exchangeOrderBy(Teachplan teachPlan, Teachplan aimTeachPlan) {
        //检查有无目标计划，若有，交换二者排序字段值，若无，抛出异常
        if (aimTeachPlan == null) {
            XueChengPlusException.cast("没有移动空间");
        } else {
            //交换
            Integer lowerOrderBy = aimTeachPlan.getOrderby();
            aimTeachPlan.setOrderby(teachPlan.getOrderby());
            teachPlan.setOrderby(lowerOrderBy);
            //更新数据库
            teachplanMapper.updateById(aimTeachPlan);
            teachplanMapper.updateById(teachPlan);
        }
    }


    /**
     * @param bindTeachPlanMediaDto 课程计划和媒资绑定信息类
     * @return com.xuecheng.content.model.po.TeachplanMedia
     * @description 课程计划和媒资信息绑定
     */
    @Transactional
    @Override
    public TeachplanMedia associaitonMedia(BindTeachPlanMediaDto bindTeachPlanMediaDto) {

        //先删除原有记录，根据课程计划id
        Long teachPlanId = bindTeachPlanMediaDto.getTeachPlanId();
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        if(teachplan==null){
            XueChengPlusException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if(grade!=2){
            XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
        }
        Long courseId = teachplan.getCourseId();
        int delete = teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, bindTeachPlanMediaDto.getTeachPlanId()));

        //再添加新的记录
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachPlanMediaDto, teachplanMedia);
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setMediaFilename(bindTeachPlanMediaDto.getFileName());
        teachplanMediaMapper.insert(teachplanMedia);
        return teachplanMedia;
    }
}
