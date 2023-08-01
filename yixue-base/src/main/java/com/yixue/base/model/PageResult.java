package com.yixue.base.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author Tavis
 * @date 2023-07-31
 * @desc 分页查询结果模型类
 */
@Data
@ToString
public class PageResult<T> implements Serializable {

    //数据列表
    private List<T> items;

    //总记录数
    private Long counts;

    //当前页码
    private Long page;

    //每页记录数
    private Long pageSize;

    public PageResult() {
    }

    public PageResult(List<T> items, Long counts, Long page, Long pageSize) {
        this.items = items;
        this.counts = counts;
        this.page = page;
        this.pageSize = pageSize;
    }
}
