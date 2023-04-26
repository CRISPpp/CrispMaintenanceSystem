package cn.crisp.crispmaintenanceuser.es;


import cn.crisp.crispmaintenanceuser.entity.ESIndexInfo;
import cn.crisp.crispmaintenanceuser.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;

/**
 * ES工具类
 * @author crisp
 */
@Component
public class ESService {
    @Value("${es.hostname}")
    private String hostname;

    @Value("${es.port}")
    private String port;

    public RestHighLevelClient client;

    /**
     * 这里是bean生命周期创建完后给client进行赋值
     */
    @PostConstruct
    public void init(){
        client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(hostname, Integer.parseInt(port), "http"))
        );
    }

    /**
     * 这是bean生命周期销毁时给连接关闭
     */
    @SneakyThrows
    @PreDestroy
    public void close(){
        client.close();
    }

    /**
     * 创建索引
     * @param indexName
     * @return
     */
    @SneakyThrows
    public Boolean createIndex(String indexName) {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        return createIndexResponse.isAcknowledged();
    }

    /**
     * 查询索引具体信息
     * @param indexName
     * @return
     */
    @SneakyThrows
    public ESIndexInfo getIndex(String indexName) {
        GetIndexRequest request = new GetIndexRequest(indexName);
        GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);

        return new ESIndexInfo(response.getAliases(), response.getMappings(), response.getSettings());
    }

    /**
     * 删除索引
     * @param indexName
     * @return
     */
    @SneakyThrows
    public Boolean deleteIndex(String indexName) {
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        return response.isAcknowledged();
    }


    /**
     * 插入数据或者修改某条数据
     * @param obj 具体数据
     * @param indexName
     * @return
     */
    @SneakyThrows
    public <T> String docInsert(T obj, String id, String indexName) {
        IndexRequest request = new IndexRequest();
        request.index(indexName).id(id);

        ObjectMapper mapper = new ObjectMapper();
        String objJson = mapper.writeValueAsString(obj);

        request.source(objJson, XContentType.JSON);

        IndexResponse response = client.index(request, RequestOptions.DEFAULT);

        DocWriteResponse.Result result = response.getResult();

        return result.toString();
    }

    /**
     * 批量插入数据
     * @param list
     * @param id
     * @param indexName
     * @param <T>
     * @return
     */
    @SneakyThrows
    public <T> String docBatchInsert(List<T> list, List<String> id, String indexName) {
        if (list.size() != id.size()) throw new Exception("列表和id列表长度不一致");
        BulkRequest request = new BulkRequest();

        for (int i = 0; i < list.size(); i ++) {
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.index(indexName).id(id.get(i));
            ObjectMapper mapper = new ObjectMapper();
            String objJson = mapper.writeValueAsString(list.get(i));
            indexRequest.source(objJson, XContentType.JSON);
            request.add(indexRequest);
        }

        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
        if (Arrays.stream(response.getItems()).filter(BulkItemResponse::isFailed).count() > 0) {
            return "部分数据插入失败，请检查数据";
        }

        StringBuilder ret = new StringBuilder();
        ret.append("执行时间: ");
        ret.append(response.getTook().toString());
        ret.append(" 命中数据");
        ret.append(Arrays.stream(response.getItems()).filter(bulkItemResponse -> "CREATED".equals(bulkItemResponse.getResponse().getResult().toString())).count());

        return ret.toString();
    }


    /**
     * 查询具体的数据
     * @param id
     * @param indexName
     * @param myClass
     * @param <T>
     * @return
     */
    @SneakyThrows
    public <T> T docGet(String id, String indexName, Class<? extends T> myClass) {
        GetRequest request = new GetRequest();
        request.index(indexName).id(id);
        GetResponse response = client.get(request, RequestOptions.DEFAULT);

        ObjectMapper mapper = new ObjectMapper();
        T ret = (T) mapper.readValue(response.getSourceAsString(), myClass);

        return ret;
    }

    /**
     * 删除数据
     * @param id
     * @param indexName
     * @return
     */
    @SneakyThrows
    public String docDelete(String id, String indexName) {
        DeleteRequest request = new DeleteRequest();
        request.index(indexName).id(id);

        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);

        DocWriteResponse.Result result = response.getResult();
        return result.toString();
    }

    @SneakyThrows
    public String docBatchDelete(List<String> id, String indexName) {
        BulkRequest request = new BulkRequest();

        id.stream().forEach(s -> {
            DeleteRequest deleteRequest = new DeleteRequest();
            deleteRequest.index(indexName).id(s);
            request.add(deleteRequest);
        });

        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);

        if (Arrays.stream(response.getItems()).filter(BulkItemResponse::isFailed).count() > 0) {
            return "部分数据删除失败，请检查数据";
        }

        StringBuilder ret = new StringBuilder();
        ret.append("执行时间: ");
        ret.append(response.getTook().toString());
        ret.append(" 命中数据");
        ret.append(Arrays.stream(response.getItems()).filter(bulkItemResponse -> "DELETED".equals(bulkItemResponse.getResponse().getResult().toString())).count());

        return ret.toString();
    }

    
}
