package com.xuecheng.base.model;

import lombok.Data;
import lombok.ToString;

/**
 * @author jxchen
 * @version 1.0
 * @description 分页查询分页参数
 * @date 2023/9/6 21 26
 */
@Data
@ToString
public class PageParams {

    //当前页码
    private Long pageNo = 1L;
    //每页显示的记录数量
    private Long pageSize = 30L;

    public PageParams() {
    }

    public PageParams(Long pageNo, Long pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public Long getPageNo() {
        return pageNo;
    }

    public void setPageNo(Long pageNo) {
        this.pageNo = pageNo;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }
}
