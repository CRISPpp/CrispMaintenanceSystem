package cn.crisp.crispmaintenanceuser.es;


import cn.crisp.crispmaintenanceuser.entity.ESIndexInfo;
import cn.crisp.crispmaintenanceuser.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
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

    public ESService(@Value("${es.hostname}") String hostname,  @Value("${es.port}") String port){
        client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(hostname, Integer.parseInt(port), "http"))
        );
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
     * @param user
     * @param indexName
     * @return
     */
    @SneakyThrows
    public String docInsert(User user, String indexName) {
        IndexRequest request = new IndexRequest();
        request.index(indexName).id(user.getId().toString());

        ObjectMapper mapper = new ObjectMapper();
        String userJson = mapper.writeValueAsString(user);

        request.source(userJson, XContentType.JSON);

        IndexResponse response = client.index(request, RequestOptions.DEFAULT);

        DocWriteResponse.Result result = response.getResult();

        return result.toString();
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

    
}
