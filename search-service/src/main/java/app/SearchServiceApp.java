package app;

import app.domain.ProductIndex;
import app.domain.SKUIndex;
import app.service.SearchProductService;
import app.web.ProductSearchController;
import core.framework.api.AbstractApplication;
import core.framework.api.search.ElasticSearch;
import core.framework.api.search.ElasticSearchBuilder;
import core.framework.api.search.ElasticSearchType;
import core.framework.api.util.Types;

/**
 * @author neo
 */
public class SearchServiceApp extends AbstractApplication {
    @Override
    protected void initialize() {
        ElasticSearch search = bindSupplier(ElasticSearch.class, null, new ElasticSearchBuilder()
            .remote("192.168.2.2"));
        onShutdown(search::close);

        bind(Types.generic(ElasticSearchType.class, ProductIndex.class), null, search.type("main", "product", ProductIndex.class));
        bind(Types.generic(ElasticSearchType.class, SKUIndex.class), null, search.type("main", "sku", SKUIndex.class));

        bind(SearchProductService.class);

        ProductSearchController searchController = bind(ProductSearchController.class);
        route().get("/p/search", searchController::search);
        route().post("/p/index", searchController::index);
    }
}
