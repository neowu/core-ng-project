package app.service;

import app.domain.ProductIndex;
import app.domain.SKUIndex;
import core.framework.api.search.ElasticSearchType;
import core.framework.api.util.JSON;
import core.framework.api.util.Lists;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.inject.Inject;
import java.util.List;

/**
 * @author neo
 */
public class SearchProductService {
    @Inject
    ElasticSearchType<ProductIndex> productType;
    @Inject
    ElasticSearchType<SKUIndex> skuType;

    public void index(ProductIndex product) {
        productType.index(String.valueOf(product.id), product);
    }

    public void index(SKUIndex sku) {
        skuType.index(sku.sku, String.valueOf(sku.productId), sku);
    }

    public List<ProductIndex> search(SearchProductRequest request) {
        List<ProductIndex> results = Lists.newArrayList();

        BoolQueryBuilder childQuery = QueryBuilders.boolQuery();
        childQuery.should(QueryBuilders.matchQuery("name", request.query));

        SearchResponse response = productType.search(new SearchSourceBuilder()
            .query(QueryBuilders.hasChildQuery("sku", childQuery))
//            .aggregation(AggregationBuilders.children("test").childType("sku").subAggregation(AggregationBuilders.max("price")))
            .from(0)
            .size(20));

        for (SearchHit hit : response.getHits()) {
            ProductIndex product = JSON.fromJSON(ProductIndex.class, hit.sourceAsString());
            results.add(product);
        }

        return results;
    }
}
