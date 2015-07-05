package app.service;

import app.IntegrationTest;
import app.domain.ProductIndex;
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
        request.query = "nike";
        List<ProductIndex> search = searchProductService.search(request);

        Assert.assertFalse(search.isEmpty());
    }
}