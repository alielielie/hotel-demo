package cn.itcast.hotel.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/**
 * @BelongsProject: hotel-demo
 * @BelongsPackage: cn.itcast.hotel.pojo
 * @Author: zt
 * @CreateTime: 2023-05-29  22:16
 * @Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult {

    private Long total;
    private List<HotelDoc> hotels;

}
