package cn.itcast.hotel.web;

import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: hotel-demo
 * @BelongsPackage: cn.itcast.hotel.web
 * @Author: zt
 * @CreateTime: 2023-05-29  22:17
 * @Description:
 */

@RestController
@RequestMapping("/hotel")
public class HotelController {

    @Resource
    private IHotelService hotelService;

    @PostMapping("/list")
    public PageResult search(@RequestBody RequestParams params){
        return hotelService.search(params);
    }

    @PostMapping("/filters")
    public Map<String, List<String>> getFilters(@RequestBody RequestParams params){
        return hotelService.filters(params);
    }

    @GetMapping("/suggestion")
    public List<String> getSuggestions(@RequestParam("key") String preFix){
        return hotelService.getSuggestions(preFix);
    }

}

