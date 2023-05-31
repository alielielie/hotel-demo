package cn.itcast.hotel.mq;

import cn.itcast.hotel.constants.MqConstants;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @BelongsProject: hotel-demo
 * @BelongsPackage: cn.itcast.hotel.mq
 * @Author: zt
 * @CreateTime: 2023-05-31  15:46
 * @Description:
 */

@Component
public class HotelListener {

    @Resource
    private IHotelService hotelService;

    /*
     * @Description: 监听酒店新增或修改的业务
     * @Author: zt
     * @Date: 2023/5/31 15:47
     * @param: [id]
     * @return: void
     **/
    @RabbitListener(queues = MqConstants.HOTEL_INSERT_QUEUE)
    public void listenHotelInsertOrUpdate(Long id){
        hotelService.insertById(id);
    }

    /*
     * @Description: 监听酒店删除的业务
     * @Author: zt
     * @Date: 2023/5/31 15:48
     * @param: [id]
     * @return: void
     **/
    @RabbitListener(queues = MqConstants.HOTEL_DELETE_QUEUE)
    public void listenHotelDelete(Long id){
        hotelService.deleteById(id);
    }

}
