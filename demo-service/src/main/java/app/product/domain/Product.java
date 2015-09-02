package app.product.domain;

import core.framework.api.db.Column;
import core.framework.api.db.PrimaryKey;
import core.framework.api.db.Table;
import core.framework.api.validate.Length;
import core.framework.api.validate.NotNull;

import java.time.LocalDateTime;

/**
 * @author neo
 */
@Table(name = "product")
public class Product {
    @PrimaryKey(autoIncrement = true)
    @Column(name = "id")
    public Integer id;
    @Column(name = "date")
    public LocalDateTime date;
    @NotNull
    @Column(name = "name")
    public String name;
    @Column(name = "price")
    public Double price;
    @Length(max = 200)
    @Column(name = "description")
    public String description;
    @Column(name = "active")
    public Boolean active;
}
