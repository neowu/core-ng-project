package core.framework.impl.template.model;

import core.framework.api.util.Lists;
import core.framework.api.util.Strings;

import java.text.DecimalFormat;
import java.util.List;

/**
 * @author neo
 */
// copied from website filter
public class FilterUIView {
    public Boolean global;
    public String query;
    public String priceName;
    public String storeName;
    public String countryCode;
    public CategoryUIView selectedCategory;
    public List<CategoryUIView> parentCategories;
    public List<CategoryUIView> subCategories;
    public List<ColorUIView> colors;
    public List<SizeUIView> sizes;
    public List<PriceUIView> prices;
    public List<VendorUIView> vendors;
    public List<BrandUIView> brands;
    public SearchProductRequest searchProductRequest;

    public Boolean hasParentCategory() {
        return parentCategories != null && !parentCategories.isEmpty();
    }

    public Boolean isMainFilterSelected() {
        return selectedCategory != null
            || (searchProductRequest.filter.brands != null && !searchProductRequest.filter.brands.isEmpty())
            || (searchProductRequest.filter.vendorNumbers != null && !searchProductRequest.filter.vendorNumbers.isEmpty());
    }

    public Boolean hasColors() {
        return isMainFilterSelected() && colors != null && !colors.isEmpty();
    }

    public Boolean hasSizes() {
        return isMainFilterSelected() && sizes != null && !sizes.isEmpty();
    }

    public Boolean hasPrices() {
        return prices != null && !prices.isEmpty();
    }

    public Boolean hasVendors() {
        return vendors != null && !vendors.isEmpty();
    }

    public Boolean hasBrands() {
        return brands != null && !brands.isEmpty();
    }

    public Boolean hasQuery() {
        return !Strings.isEmpty(searchProductRequest.query);
    }

    public Boolean isCategorySelected() {
        return searchProductRequest.filter.categoryId != null;
    }

    public Boolean isVendorSelected(VendorUIView vendor) {
        return searchProductRequest.filter.vendorNumbers != null && searchProductRequest.filter.vendorNumbers.contains(vendor.vendorNumber);
    }

    public Boolean isBrandSelected(BrandUIView brand) {
        return searchProductRequest.filter.brands != null && searchProductRequest.filter.brands.contains(brand.brandName);
    }

    public Boolean isColorSelected(ColorUIView color) {
        return searchProductRequest.filter.colors != null && searchProductRequest.filter.colors.contains(color.colorName);
    }

    public Boolean isSizeSelected(SizeUIView size) {
        return searchProductRequest.filter.sizes != null && searchProductRequest.filter.sizes.contains(size.sizeName);
    }

    public Boolean isPriceSelected(PriceUIView price) {
        return priceName != null && priceName.equals(price.priceName);
    }

    public String storeUrl() {
        return '/' + countryCode + "/";
    }

    public String searchURL(CategoryUIView category) {
        return new SearchURLBuilder(searchProductRequest, global)
            .setCountryCode(countryCode)
            .setStoreName(storeName)
            .setCategory(category)
            .build();
    }

    public String searchURL(VendorUIView vendor) {
        return new SearchURLBuilder(searchProductRequest, global)
            .setCountryCode(countryCode)
            .setStoreName(storeName)
            .setVendorNumber(vendor.vendorNumber)
            .build();
    }

    public String searchURL(BrandUIView brand) {
        return new SearchURLBuilder(searchProductRequest, global)
            .setCountryCode(countryCode)
            .setStoreName(storeName)
            .setBrand(brand.brandName)
            .build();
    }

    public String searchURL(ColorUIView color) {
        return new SearchURLBuilder(searchProductRequest, global)
            .setCountryCode(countryCode)
            .setStoreName(storeName)
            .setColor(color.colorName)
            .build();
    }

    public String searchURL(SizeUIView size) {
        return new SearchURLBuilder(searchProductRequest, global)
            .setCountryCode(countryCode)
            .setStoreName(storeName)
            .setSize(size.sizeName)
            .build();
    }

    public String searchURL(PriceUIView price) {
        return new SearchURLBuilder(searchProductRequest, global)
            .setCountryCode(countryCode)
            .setStoreName(storeName)
            .setPrice(price.priceName)
            .build();
    }

    public String searchURL(Sort sort) {
        return new SearchURLBuilder(searchProductRequest, global)
            .setCountryCode(countryCode)
            .setStoreName(storeName)
            .setSort(sort.name())
            .build();
    }

    public String searchURL(Integer offset) {
        return new SearchURLBuilder(searchProductRequest, global)
            .setCountryCode(countryCode)
            .setStoreName(storeName)
            .setOffset(offset)
            .build();
    }

    public String searchURLWithoutCategory() {
        CategoryUIView categoryUIView = new CategoryUIView();
        categoryUIView.categoryId = "";
        return new SearchURLBuilder(searchProductRequest, global)
            .setCountryCode(countryCode)
            .setStoreName(storeName)
            .setCategory(categoryUIView)
            .build();
    }

    public List<Sort> sortMethods() {
        return Lists.newArrayList(Sort.values());
    }

    public Boolean isSortSelected(Sort sort) {
        return searchProductRequest.sort != null && searchProductRequest.sort.equals(sort);
    }

    public Boolean isFetchSizeSelected(int fetchSize) {
        return searchProductRequest.limit == fetchSize;
    }

    public String categoryTree() {
        StringBuilder b = new StringBuilder();
        if (isCategorySelected()) {
            b.append("<li class=\"list-item active\"><a class=\"selected-link\" href=\"");
            b.append(searchURL(selectedCategory)).append("\">").append(selectedCategory.categoryName)
                .append("</a><a class=\"reset-filter\" data-exclude-filter=\"category\"><i class=\"fa fa-close\"></i></a><ul>");

            for (CategoryUIView subCategory : subCategories) {
                b.append("<li class=\"list-item\"><a href=\"");
                b.append(searchURL(subCategory)).append("\">").append(subCategory.categoryName).append("</a></li>");
            }
            b.append("</ul></li>");

            for (CategoryUIView parentCategory : parentCategories) {
                b.insert(0, "<li class=\"list-item\"><a href=\"" + searchURL(parentCategory) + "\">" + parentCategory.categoryName + "</a><ul>");
                b.append("</ul></li>");
            }

            b.insert(0, "<li class=\"list-item\"><a href=\"" + searchURLWithoutCategory() + "\">All category</a><ul>");
            b.append("</ul></li>");
        } else {
            for (CategoryUIView subCategory : subCategories) {
                b.append("<li class=\"list-item\"><a href=\"");
                b.append(searchURL(subCategory)).append("\">").append(subCategory.categoryName).append("</a></li>");
            }
        }

        b.insert(0, "<ul class=\"list\">");
        b.append("</ul>");
        return b.toString();
    }

    public String priceLabel(PriceUIView priceUIView) {
        DecimalFormat decimalFormat = new DecimalFormat("###,###.##");
        if (priceUIView.startPrice < 0.001) {
            return "Under Q" + decimalFormat.format(priceUIView.endPrice);
        }
        StringBuilder b = new StringBuilder();
        String formattedStartPrice = decimalFormat.format(priceUIView.startPrice);
        b.append('Q').append(formattedStartPrice);
        if (priceUIView.endPrice == null) {
            b.append(" & Above");
        } else {
            b.append(" to Q").append(decimalFormat.format(priceUIView.endPrice));
        }
        return b.toString();
    }
}
