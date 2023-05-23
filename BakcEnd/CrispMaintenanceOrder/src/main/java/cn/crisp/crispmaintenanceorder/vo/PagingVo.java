package cn.crisp.crispmaintenanceorder.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@SuppressWarnings({"all"})
/**
 * 分页结果
 */
@Data
@AllArgsConstructor
public class PagingVo<T> {
    /**
     * 总共几个
     */
    private long total;

    /**
     * 数据
     */
    private List<T> records;

}
