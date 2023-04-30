package cn.crisp.crispmaintenanceuser.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用来做查询的属性-值类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESMap<T> {
    String t;
    T v;
}
