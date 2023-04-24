package cn.crisp.crispmaintenanceuser.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.settings.Settings;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ESIndexInfo {
    //别名
    private  Map<String, List<AliasMetadata>> alias;
    //索引结构
    private Map<String, MappingMetadata> mapping;
    //配置
    private Map<String, Settings> settings;
}
