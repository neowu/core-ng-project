package app;

import app.service.SearchProductService;
import app.web.ProductSearchController;
import core.framework.api.AbstractApplication;
import core.framework.api.search.ElasticSearch;
import core.framework.api.search.ElasticSearchBuilder;

/**
 * @author neo
 */
public class SearchServiceApp extends AbstractApplication {
    @Override
    protected void initialize() {
        ElasticSearch search = bindSupplier(ElasticSearch.class, null, new ElasticSearchBuilder().remote("192.168.2.2"));
        onShutdown(search::shutdown);

        bind(SearchProductService.class);

        ProductSearchController searchController = bind(ProductSearchController.class);
        route().get("/p/search", searchController::search);
        route().post("/p/index", searchController::index);
    }
}
