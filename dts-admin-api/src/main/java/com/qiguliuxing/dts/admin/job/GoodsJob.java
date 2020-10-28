package com.qiguliuxing.dts.admin.job;

import com.qiguliuxing.dts.db.domain.DtsGoods;
import com.qiguliuxing.dts.db.service.DtsGoodsService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class GoodsJob {
    private final Log logger = LogFactory.getLog(GoodsJob.class);

    @Autowired
    private DtsGoodsService goodsService;

    /**
     * 每隔一分钟检查 商品上下架
     */
    @Scheduled(fixedDelay =  60*1000)
    public void checkGoodsExpired(){
        logger.info("系统开启每分任务检查，更新商品上下架");
        LocalDateTime nowTime = LocalDateTime.now();
        //查询所有商品
        List<DtsGoods> goodsList = goodsService.selectAll();
        List<DtsGoods> updateGoodsList = new ArrayList<>();
        //遍历所有商品
        for(DtsGoods dtsGoods:goodsList){
            LocalDateTime startTime = dtsGoods.getStartTime();
            LocalDateTime endTime = dtsGoods.getEndTime();
            if(nowTime.isAfter(startTime) && nowTime.isBefore(endTime)){//当前时间在起售时间之后并且在结束时间之前
                if(!dtsGoods.getIsOnSale()){
                    dtsGoods.setIsOnSale(true);
                    updateGoodsList.add(dtsGoods);
                    logger.info("更新商品信息，商品为："+dtsGoods.getId());
                }
            }else if(nowTime.equals(startTime)){//开始时间等于当前时间
                if(!dtsGoods.getIsOnSale()){
                    dtsGoods.setIsOnSale(true);
                    updateGoodsList.add(dtsGoods);
                    logger.info("更新商品信息，商品为："+dtsGoods.getId());
                }
            }else{
                if(dtsGoods.getIsOnSale()){
                    dtsGoods.setIsOnSale(false);
                    updateGoodsList.add(dtsGoods);
                    logger.info("更新商品信息，商品为："+dtsGoods.getId());
                }
            }
//            goodsService.updateIsOnSale(dtsGoods);
//            updateGoodsList.add(dtsGoods);
        }
        if(updateGoodsList.size() > 0){
            Integer count = goodsService.updateGoodsList(updateGoodsList);
            logger.info("系统结束每分任务检查，更新商品上下架成功，成功数量:"+count);
        }
    }
    /**
     * 每天更改商品上下架
     */
//    @Scheduled(cron = "0 0 0 * * ?")
    public void updateGoodsStatusList(){
//        logger.info("系统开启每天任务检查商品上下架时间");
//        LocalDateTime nowTime = LocalDateTime.now();//当前时间 (凌晨 00:00:00)
//        //查询所有商品
//        List<DtsGoods> goodsList = goodsService.selectAll();
//        List<DtsGoods> updateGoodsList = new ArrayList<>();
//        //遍历所有商品
//        for(DtsGoods dtsGoods:goodsList){
//            LocalDateTime startTime = dtsGoods.getStartTime();
//            LocalDateTime endTime = dtsGoods.getEndTime();
//            if(nowTime.isAfter(endTime) || nowTime.equals(endTime)){//如果当前时间在结束时间之后  或者  当前时间和结束时间相等
//               dtsGoods.setIsOnSale(false);
//            }else if(nowTime.isAfter(startTime) && nowTime.isBefore(endTime)){//如果当前时间在开始时间之后 并且 当前时间在结束时间之前
//               dtsGoods.setIsOnSale(true);
//            }else if(nowTime.equals(startTime)){//如果当前时间等于开始时间
//               dtsGoods.setIsOnSale(true);
//            }
//
//            updateGoodsList.add(dtsGoods);
//        }
//        goodsService.updateGoodsList(updateGoodsList);
//        logger.info("------商品上下架结束------");
    }

}
