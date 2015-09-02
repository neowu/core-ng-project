package app.product.service;

import app.IntegrationTest;
import app.product.domain.ProductDocument;
import app.product.web.SearchProductRequest;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;

/**
 * @author neo
 */
public class SearchProductServiceTest extends IntegrationTest {
    @Inject
    SearchProductService searchProductService;

    @Test
    public void search() {
        SearchProductRequest request = new SearchProductRequest();
        request.query = "NIKE-003";
        List<ProductDocument> results = searchProductService.search(request);

        Assert.assertFalse(results.isEmpty());
    }
}