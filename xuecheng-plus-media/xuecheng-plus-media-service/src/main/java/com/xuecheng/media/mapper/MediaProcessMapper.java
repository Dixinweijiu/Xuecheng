package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    /**
     * @param sharedTotal 分片总数
     * @param sharedIndex 分片序号
     * @param count       任务数
     * @return 任务列表
     * @description 根据分片参数获取待处理任务
     */
    @Select("select *" +
            "from media_process mp " +
            "where mp.id % #{shardTotal} = #{shardIndex}" +
            "and (mp.status = 1 or mp.status = 3)" +
            "and mp.fail_count < 3 " +
            "limit #{count}")
    List<MediaProcess> selectListBySharedIndex(@Param("shardTotal") int shardTotal,
                                               @Param("shardIndex") int shardIndex,
                                               @Param("count") int count);


    /**
     * @param id 任务id
     * @return 更新记录数
     * @description 开启一个任务
     */
    @Update("update media_process mp " +
            "set mp.status = '4'" +
            "where (mp.status = '1' or mp.status = '3')" +
            "and mp.fail_count < 3 " +
            "and mp.id = #{id}")
    int startTask(@Param("id") long id);
}
