package cn.itcast.hotel.pojo;

import lombok.Data;

/**
 * @BelongsProject: hotel-demo
 * @BelongsPackage: cn.itcast.hotel.pojo
 * @Author: zt
 * @CreateTime: 2023-05-29  22:14
 * @Description:
 */

@Data
public class RequestParams {

    private String key;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String city;
    private String brand;
    private String starName;
    private Integer minPrice;
    private Integer maxPrice;
    private String location;

}
