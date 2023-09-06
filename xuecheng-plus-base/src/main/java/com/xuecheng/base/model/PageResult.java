package com.xuecheng.base.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author jxchen
 * @version 1.0
 * @description 分页查询结果模型类
 * @date 2023/9/6 21 36
 */

@Data
@ToString
public class PageResult<T> implements Serializable {

    // 数据列表
    private List<T> items;

    // 总记录数
    private long counts;

    // 当前页码
    private long pageNo;

    // 每页记录数
    private long pageSize;

    public PageResult(List<T> items, long counts, long pageNo, long pageSize) {
        this.items = items;
        this.counts = counts;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }
}
