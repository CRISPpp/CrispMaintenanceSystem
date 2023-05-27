package cn.crisp.crispmaintenanceorder.dto;

import lombok.Data;

@SuppressWarnings({"all"})
@Data
public class QueryDto<T> {
    /**
     * 是否分页
     */
    private Boolean paging = true;

    /**
     * 一页最多几个数据
     */
    private Integer size = 10;

    /**
     * 获取第几页，从第 0 页开始
     */
    private Integer current = 0;

    /**
     * 筛选条件
     */
    private T condition;

    /**
     * 需要跳过的个数
     * @return
     */
    public int getSkip() {
        return current * size;
    }
}
