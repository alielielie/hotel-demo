package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.DispatcherServlet;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Resource
    private RestHighLevelClient client;

    @Override
    public PageResult search(RequestParams params) {
        SearchResponse response = null;
        try {
            //1.准备Request
            SearchRequest request = new SearchRequest("hotel");
            //2.准备DSL
            //2.1.query
            buildBasicQuery(params, request);
            //2.2.分页
            int page = params.getPage();
            int size = params.getSize();
            request.source().from((page - 1) * size).size(size);
            //2.3.排序
            String location = params.getLocation();
            if (location != null && !location.equals("")) {
                request.source().sort(
                        SortBuilders.geoDistanceSort("location", new GeoPoint(location))
                                .order(SortOrder.ASC)
                                .unit(DistanceUnit.KILOMETERS)
                );
            }
            //3.发送请求，得到响应
            response = client.search(request, RequestOptions.DEFAULT);
            //4.解析响应
            return handleResponse(response);
        } catch (IOException e) {
            throw new RuntimeException();
        }

    }

    @Override
    public Map<String, List<String>> filters(RequestParams params) {
        try {
            //1.准备request
            SearchRequest request = new SearchRequest("hotel");
            //2.准备DSL
            //2.1.query
            buildBasicQuery(params, request);
            //2.2.设置size
            request.source().size(0);
            //2.3.聚合
            buildAggregation(request);
            //3.发送请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //4.解析结果
            Map<String, List<String>> result = new HashMap<>();
            Aggregations aggregations = response.getAggregations();
            //根据聚类名称，获取聚类结果
            List<String> brandList = getAggByName(aggregations, "brandAgg");
            List<String> cityList = getAggByName(aggregations, "cityAgg");
            List<String> starList = getAggByName(aggregations, "starAgg");
            //4.2.放入map
            result.put("brand", brandList);
            result.put("city", cityList);
            result.put("starName", starList);
            return result;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public List<String> getSuggestions(String preFix) {
        try {
            //1.准备request
            SearchRequest request = new SearchRequest("hotel");
            //2.准备DSL
            request.source().suggest(
                    new SuggestBuilder().addSuggestion(
                            "test",
                            SuggestBuilders.completionSuggestion("suggestion")
                                    .prefix(preFix)
                                    .size(10)
                    )
            );
            //3.发起请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //4.解析结果
            Suggest suggestions = response.getSuggest();
            CompletionSuggestion suggestion = suggestions.getSuggestion("test");
            List<CompletionSuggestion.Entry.Option> options = suggestion.getOptions();
            List<String> list = new ArrayList<>(options.size());
            for (CompletionSuggestion.Entry.Option option : options) {
                list.add(option.getText().toString());
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private List<String> getAggByName(Aggregations aggregations, String aggName) {
        //4.1.根据聚合名称获取聚合结果
        Terms brandTerms = aggregations.get(aggName);
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        List<String> brandList = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            String key = bucket.getKeyAsString();
            brandList.add(key);
        }
        return brandList;
    }

    private void buildAggregation(SearchRequest request) {
        request.source().aggregation(
                AggregationBuilders
                        .terms("brandAgg")
                        .field("brand")
                        .size(100)
        );
        request.source().aggregation(
                AggregationBuilders
                        .terms("cityAgg")
                        .field("city")
                        .size(100)
        );
        request.source().aggregation(
                AggregationBuilders
                        .terms("starAgg")
                        .field("starName")
                        .size(100)
        );
    }

    private void buildBasicQuery(RequestParams params, SearchRequest request) {
        //1.构建BooleanQuery
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //关键字搜索
        String key = params.getKey();
        if (key == null || "".equals(key)) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        }
        //条件过滤
        //城市条件
        if (params.getCity() != null && !params.getCity().equals("")) {
            boolQuery.filter(QueryBuilders.termQuery("city", params.getCity()));
        }
        //品牌条件
        if (params.getBrand() != null && !params.getBrand().equals("")) {
            System.out.println("params.getBrand() = " + params.getBrand());
            boolQuery.filter(QueryBuilders.termQuery("brand", params.getBrand()));
        }
        //星级条件
        if (params.getStarName() != null && !params.getStarName().equals("")) {
            boolQuery.filter(QueryBuilders.termQuery("starName", params.getStarName()));
        }
        //价格条件
        if (params.getMinPrice() != null && params.getMaxPrice() != null) {
            boolQuery.filter(QueryBuilders.
                    rangeQuery("price").gte(params.getMinPrice()).lte(params.getMaxPrice()));
        }
        //2.算分控制
        FunctionScoreQueryBuilder functionScoreQuery =
                QueryBuilders.functionScoreQuery(
                        //原始查询，相关性算分查询
                        boolQuery,
                        //function score的数组
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                                //其中的一个function score元素
                                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                        //过滤条件
                                        QueryBuilders.termQuery("isAD", true),
                                        //算分函数
                                        ScoreFunctionBuilders.weightFactorFunction(10)
                                )
                        });
        request.source().query(functionScoreQuery);
    }

    private PageResult handleResponse(SearchResponse response) {
        SearchHits searchHits = response.getHits();
        //4.1.获取总条数
        long total = searchHits.getTotalHits().value;
        //4.2.文档数组
        SearchHit[] hits = searchHits.getHits();
        //4.3.遍历
        List<HotelDoc> hotels = new ArrayList<>();
        for (SearchHit hit : hits) {
            //获取文档source
            String json = hit.getSourceAsString();
            //反序列化
            HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
            //获取排序值
            Object[] sortValues = hit.getSortValues();
            if(sortValues.length > 0){
                Object sortValue = sortValues[0];
                hotelDoc.setDistance(sortValue);
            }
            hotels.add(hotelDoc);
        }
        //4.4.返回
        return new PageResult(total, hotels);
    }

}
