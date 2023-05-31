package cn.itcast.hotel.constants;

/**
 * @BelongsProject: hotel-demo
 * @BelongsPackage: cn.itcast.hotel.constants
 * @Author: zt
 * @CreateTime: 2023-05-31  15:16
 * @Description:
 */

public class MqConstants {
    //交换机
    public final static String HOTEL_EXCHANGE = "hotel.topic";
    //监听新增和修改的队列
    public final static String HOTEL_INSERT_QUEUE = "hotel.insert.queue";
    //监听删除的队列
    public final static String HOTEL_DELETE_QUEUE = "hotel.delete.queue";
    //新增或修改的RoutingKey
    public final static String HOTEL_INSERT_KEY = "hotel.insert";
    //删除的RoutingKey
    public final static String HOTEL_DELETE_KEY = "hotel.delete";
}
