package app.product.service;

import app.product.domain.ProductDocument;
import app.product.web.SearchProductRequest;
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
    ElasticSearchType<ProductDocument> productType;

    public void index(ProductDocument product) {
        productType.index(String.valueOf(product.id), product);
    }

    public List<ProductDocument> search(SearchProductRequest request) {
        List<ProductDocument> results = Lists.newArrayList();

        BoolQueryBuilder query = QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("skus.sku", request.query))
            .should(QueryBuilders.matchQuery("name", request.query));

        SearchResponse response = productType.search(new SearchSourceBuilder()
            .query(query)
            .from(0)
            .size(20));

        for (SearchHit hit : response.getHits()) {
            ProductDocument product = JSON.fromJSON(ProductDocument.class, hit.sourceAsString());
            results.add(product);
        }

        return results;
    }
}
