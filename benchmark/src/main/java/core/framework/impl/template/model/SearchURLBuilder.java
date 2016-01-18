package core.framework.impl.template.model;

import core.framework.api.util.Lists;
import core.framework.api.util.Sets;
import core.framework.api.util.Strings;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author chi
 */
public class SearchURLBuilder {
    private final SearchProductRequest searchProductRequest;
    private final Boolean global;

    private String storeName;
    private String countryCode;
    private String vendorNumber;
    private CategoryUIView category;
    private String color;
    private String brand;
    private String size;
    private String price;
    private String sort;
    private Integer offset = 0;

    public SearchURLBuilder(SearchProductRequest searchProductRequest, Boolean global) {
        this.searchProductRequest = searchProductRequest;
        this.global = global;
    }

    public SearchURLBuilder setCategory(CategoryUIView category) {
        this.category = category;
        return this;
    }

    public SearchURLBuilder setColor(String color) {
        this.color = color;
        return this;
    }

    public SearchURLBuilder setCountryCode(String countryCode) {
        this.countryCode = countryCode;
        return this;
    }

    public SearchURLBuilder setStoreName(String storeName) {
        this.storeName = storeName;
        return this;
    }

    public SearchURLBuilder setVendorNumber(String vendorNumber) {
        this.vendorNumber = vendorNumber;
        return this;
    }

    public SearchURLBuilder setBrand(String brand) {
        this.brand = brand;
        return this;
    }

    public SearchURLBuilder setSize(String size) {
        this.size = size;
        return this;
    }

    public SearchURLBuilder setPrice(String price) {
        this.price = price;
        return this;
    }

    public SearchURLBuilder setSort(String sort) {
        this.sort = sort;
        return this;
    }

    public SearchURLBuilder setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public String build() {
        URIBuilder builder = new URIBuilder("/").addPath(countryCode);
        if (global) {
            builder.addPath("search");
        } else {
            builder.addPath("s").addPath(storeName);
        }

        if (searchProductRequest.query != null) {
            builder.addQueryParam("q", searchProductRequest.query);
        }

        if (global) {
            String vendorNumbers = vendorNumbers();
            if (!Strings.isEmpty(vendorNumbers)) {
                builder.addQueryParam("vendor", vendorNumbers);
            }
        }

        String categoryId = categoryId();
        if (!Strings.isEmpty(categoryId)) {
            builder.addQueryParam("category", categoryId);
        }

        String brands = brands();
        if (!Strings.isEmpty(brands)) {
            builder.addQueryParam("brand", brands);
        }

        String color = colors();
        if (!Strings.isEmpty(color)) {
            builder.addQueryParam("color", color);
        }

        String size = size();
        if (!Strings.isEmpty(size)) {
            builder.addQueryParam("size", size);
        }

        String price = price();
        if (!Strings.isEmpty(price)) {
            builder.addQueryParam("price", price);
        }

        String sort = sort();
        if (!Strings.isEmpty(sort)) {
            builder.addQueryParam("sort", sort);
        }

        if (offset != null) {
            builder.addQueryParam("offset", String.valueOf(offset));
        }

        return builder.toURI();
    }

    private String vendorNumbers() {
        return vendorNumber == null ? join(searchProductRequest.filter.vendorNumbers) : join(searchProductRequest.filter.vendorNumbers, vendorNumber);
    }

    String categoryId() {
        return category == null ? searchProductRequest.filter.categoryId : category.categoryId;
    }

    String sort() {
        return sort == null ? searchProductRequest.sort.name() : sort;
    }

    String colors() {
        return color == null ? join(searchProductRequest.filter.colors) : join(searchProductRequest.filter.colors, color);
    }

    String size() {
        return size == null ? join(searchProductRequest.filter.sizes) : join(searchProductRequest.filter.sizes, size);
    }

    String price() {
        if (!Strings.isEmpty(price)) {
            return price;
        }

        if (searchProductRequest.filter.minPrice != null) {
            if (Double.MAX_VALUE - searchProductRequest.filter.maxPrice < 1) {
                return String.format("%.1f", searchProductRequest.filter.minPrice) + "-*";
            } else {
                return String.format("%.1f", searchProductRequest.filter.minPrice) + "-"
                    + String.format("%.1f", searchProductRequest.filter.maxPrice);
            }
        }
        return "";
    }

    String brands() {
        List<String> brands = Lists.newArrayList();
        if (searchProductRequest.filter.brands != null) {
            brands.addAll(searchProductRequest.filter.brands.stream().collect(Collectors.toList()));
        }
        if (brand != null) {
            brands.add(brand);
        }
        return join(brands);
    }

    String join(List<String> list, String... values) {
        Set<String> concat = Sets.newHashSet(values);
        if (list != null) {
            concat.addAll(list);
        }
        return concat.stream()
            .filter(value -> !Strings.isEmpty(value))
            .collect(Collectors.joining("~"));
    }
}
