package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsGoodsMapper;
import com.qiguliuxing.dts.db.domain.*;
import com.qiguliuxing.dts.db.domain.DtsGoods.Column;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DtsGoodsService {
	Column[] columns = new Column[] { Column.id, Column.name, Column.brief, Column.picUrl, Column.isHot, Column.isNew,
			Column.counterPrice, Column.retailPrice, Column.shopId,Column.shopCategoryId,Column.shopType,Column.tripDesc,
			Column.costDesc,Column.destination,Column.keywords, Column.latitudeAndLongitude, Column.address};
	@Resource
	private DtsGoodsMapper goodsMapper;

	/**
	 * 获取热卖商品
	 *
	 * @param offset
	 * @param limit
	 * @return
	 */
	public List<DtsGoods> queryByHot(int offset, int limit) {
		DtsGoodsExample example = new DtsGoodsExample();
		example.or().andIsHotEqualTo(true).andIsOnSaleEqualTo(true).andDeletedEqualTo(false).andShopTypeEqualTo(1);
		example.setOrderByClause("browse desc");
		PageHelper.startPage(offset, limit);
		return goodsMapper.selectByExampleSelective(example, columns);
	}

	/**
	 * 获取新品上市
	 *
	 * @param offset
	 * @param limit
	 * @return
	 */
	public List<DtsGoods> queryByNew(int offset, int limit) {
		DtsGoodsExample example = new DtsGoodsExample();
		example.or().andIsNewEqualTo(true).andIsOnSaleEqualTo(true).andDeletedEqualTo(false).andShopTypeEqualTo(1);
		example.setOrderByClause("add_time desc");
		PageHelper.startPage(offset, limit);

		return goodsMapper.selectByExampleSelective(example, columns);
	}

	/**
	 * 获取商铺新品上市
	 * @param offset
	 * @param limit
	 * @param shopId
	 * @return
	 */
	public List<DtsGoods> queryByNew(int offset, int limit,Integer shopId) {
		DtsGoodsExample example = new DtsGoodsExample();
		example.or().andIsNewEqualTo(true).andIsOnSaleEqualTo(true).andDeletedEqualTo(false).andShopIdEqualTo(shopId);
		example.setOrderByClause("add_time desc");
		PageHelper.startPage(offset, limit);
		return goodsMapper.selectByExampleSelective(example, columns);
	}

	/**
	 * 获取分类下的商品
	 *
	 * @param catList
	 * @param offset
	 * @param limit
	 * @return
	 */
	public List<DtsGoods> queryByCategory(List<Integer> catList, int offset, int limit) {
		DtsGoodsExample example = new DtsGoodsExample();
		example.or().andCategoryIdIn(catList).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
		example.setOrderByClause("sort_order  asc");
		PageHelper.startPage(offset, limit);

		return goodsMapper.selectByExampleSelective(example, columns);
	}

	/**
	 * 获取分类下的商品
	 *
	 * @param categoryId
	 * @param offset
	 * @param limit
	 * @return
	 */
	public List<DtsGoods> queryByCategory(Integer categoryId, int offset, int limit) {
		DtsGoodsExample example = new DtsGoodsExample();
		example.or().andCategoryIdEqualTo(categoryId).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
		example.setOrderByClause("add_time desc");
		PageHelper.startPage(offset, limit);

		return goodsMapper.selectByExampleSelective(example, columns);
	}

	public List<DtsGoods> querySelective(Integer categoryId, Integer brandId, String keywords, Boolean isHot, Boolean isNew,
			Integer offset, Integer limit, String sort, String order,List<Integer> categoryL2Ids,Integer shopId) {
		DtsGoodsExample example = new DtsGoodsExample();
		DtsGoodsExample.Criteria criteria1 = example.or();
//		DtsGoodsExample.Criteria criteria2 = example.or();

//		if (!StringUtils.isEmpty(categoryId) && categoryId != 0) {
//			criteria1.andCategoryIdEqualTo(categoryId);
////			criteria2.andCategoryIdEqualTo(categoryId);
//		}

		if(!StringUtils.isEmpty(shopId) && shopId != 0){
			criteria1.andShopIdEqualTo(shopId);
		}
		if (!StringUtils.isEmpty(brandId)) {
			criteria1.andBrandIdEqualTo(brandId);
//			criteria2.andBrandIdEqualTo(brandId);
		}
		if (!StringUtils.isEmpty(isNew)) {
			criteria1.andIsNewEqualTo(isNew);
//			criteria2.andIsNewEqualTo(isNew);
		}
		if (!StringUtils.isEmpty(isHot)) {
			criteria1.andIsHotEqualTo(isHot);
//			criteria2.andIsHotEqualTo(isHot);
		}
		if (!StringUtils.isEmpty(keywords)) {
			criteria1.andNameLike("%" + keywords + "%");
//			criteria1.andKeywordsLike("%" + keywords + "%");
//			criteria2.andNameLike("%" + keywords + "%");
		}
		if(!StringUtils.isEmpty(categoryL2Ids) && categoryL2Ids.size() > 0){
			criteria1.andCategoryIdIn(categoryL2Ids);
		}
		criteria1.andIsOnSaleEqualTo(true);
//		criteria2.andIsOnSaleEqualTo(true);
		criteria1.andDeletedEqualTo(false);
//		criteria2.andDeletedEqualTo(false);

		if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
			example.setOrderByClause(sort + " " + order);
		}

		PageHelper.startPage(offset, limit);
		return goodsMapper.selectByExampleSelective(example, columns);
	}

	public List<DtsGoods> querySelective(String goodsSn, String name, Integer page, Integer size, String sort,
		String order,Integer shopId,Boolean isOnSale,Integer shopType) {
		DtsGoodsExample example = new DtsGoodsExample();
		DtsGoodsExample.Criteria criteria = example.createCriteria();

//		LocalDateTime nowTime = LocalDateTime.now();//当前时间

		criteria.andDeletedEqualTo(false);
//		criteria.andStartTimeLessThanOrEqualTo(nowTime);
//		criteria.andEndTimeGreaterThanOrEqualTo(nowTime);

		if (!StringUtils.isEmpty(goodsSn)) {
			criteria.andGoodsSnEqualTo(goodsSn);
		}
		if (!StringUtils.isEmpty(name)) {
			criteria.andNameLike("%" + name + "%");
		}

		if(!StringUtils.isEmpty(shopId)){
			criteria.andShopIdEqualTo(shopId);
		}
		if(!StringUtils.isEmpty(isOnSale)){
			criteria.andIsOnSaleEqualTo(isOnSale);
		}
		if(!StringUtils.isEmpty(shopType)){
			criteria.andShopTypeEqualTo(shopType);
		}
		if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
			example.setOrderByClause(sort + " " + order);
		}

		PageHelper.startPage(page, size);
		return goodsMapper.selectByExampleWithBLOBs(example);
	}

	/**
	 * 获取某个商品信息,包含完整信息
	 *
	 * @param id
	 * @return
	 */
	public DtsGoods findById(Integer id) {
		DtsGoodsExample example = new DtsGoodsExample();
		example.or().andIdEqualTo(id).andDeletedEqualTo(false);
		return goodsMapper.selectOneByExampleWithBLOBs(example);
	}

	/**
	 * 获取某个商品信息，仅展示相关内容
	 *
	 * @param id
	 * @return
	 */
	public DtsGoods findByIdVO(Integer id) {
		DtsGoodsExample example = new DtsGoodsExample();
		example.or().andIdEqualTo(id).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
		return goodsMapper.selectOneByExampleSelective(example, columns);
	}

	/**
	 * 获取所有在售物品总数
	 *
	 * @return
	 */
	public Integer queryOnSale() {
		DtsGoodsExample example = new DtsGoodsExample();
		example.or().andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
		return (int) goodsMapper.countByExample(example);
	}

	public int updateById(DtsGoods goods) {
		goods.setUpdateTime(LocalDateTime.now());
		return goodsMapper.updateByPrimaryKeySelective(goods);
	}

	public void deleteById(Integer id) {
		goodsMapper.logicalDeleteByPrimaryKey(id);
	}

	public void add(DtsGoods goods) {
		goods.setAddTime(LocalDateTime.now());
		goods.setUpdateTime(LocalDateTime.now());
		goodsMapper.insertSelective(goods);
	}

	/**
	 * 获取所有物品总数，包括在售的和下架的，但是不包括已删除的商品
	 *
	 * @return
	 */
	public int count(Integer shopId) {
		DtsGoodsExample example = new DtsGoodsExample();
		example.or().andDeletedEqualTo(false).andShopIdEqualTo(shopId);
		return (int) goodsMapper.countByExample(example);
	}

	public List<Integer> getCatIds(Integer brandId, String keywords, Boolean isHot, Boolean isNew,Integer shopId) {
		DtsGoodsExample example = new DtsGoodsExample();
		DtsGoodsExample.Criteria criteria1 = example.or();
//		DtsGoodsExample.Criteria criteria2 = example.or();

		if(!StringUtils.isEmpty(shopId) && shopId != 0){
			criteria1.andShopIdEqualTo(shopId);
		}
		if (!StringUtils.isEmpty(brandId)) {
			criteria1.andBrandIdEqualTo(brandId);
//			criteria2.andBrandIdEqualTo(brandId);
		}
		if (!StringUtils.isEmpty(isNew)) {
			criteria1.andIsNewEqualTo(isNew);
//			criteria2.andIsNewEqualTo(isNew);
		}
		if (!StringUtils.isEmpty(isHot)) {
			criteria1.andIsHotEqualTo(isHot);
//			criteria2.andIsHotEqualTo(isHot);
		}
		if (!StringUtils.isEmpty(keywords)) {
//			criteria1.andKeywordsLike("%" + keywords + "%");
//			criteria2.andNameLike("%" + keywords + "%");
			criteria1.andNameLike("%" + keywords + "%");
//			criteria2.andNameLike("%" + keywords + "%");
		}
		criteria1.andIsOnSaleEqualTo(true);
//		criteria2.andIsOnSaleEqualTo(true);
		criteria1.andDeletedEqualTo(false);
//		criteria2.andDeletedEqualTo(false);

		List<DtsGoods> goodsList = goodsMapper.selectByExampleSelective(example, Column.categoryId);
		List<Integer> cats = new ArrayList<Integer>();
		for (DtsGoods goods : goodsList) {
			cats.add(goods.getCategoryId());
		}
		return cats;
	}

	public boolean checkExistByName(String name) {
		DtsGoodsExample example = new DtsGoodsExample();
		example.or().andNameEqualTo(name).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
		return goodsMapper.countByExample(example) != 0;
	}

	/**
	 * 根据店铺，获取店铺对应类别的商品
	 * 
	 * @param bid
	 * @param cid
	 * @param offset
	 * @param limit
	 * @return
	 */
	public List<DtsGoods> queryByBrandId(int bid, int cid, int offset, int limit) {
		DtsGoodsExample example = new DtsGoodsExample();
		example.or().andBrandIdEqualTo(bid).andCategoryIdEqualTo(cid).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
		example.setOrderByClause("browse desc");
		PageHelper.startPage(offset, limit);

		return goodsMapper.selectByExampleSelective(example, columns);
	}

	/**
	 * 同类商品，且不同店铺
	 * 
	 * @param bid
	 * @param cid
	 * @param offset
	 * @param limit
	 * @return
	 */
	public List<DtsGoods> queryByCategoryAndNotSameBrandId(int bid, int cid, int offset, int limit) {
		DtsGoodsExample example = new DtsGoodsExample();
		example.or().andBrandIdNotEqualTo(bid).andCategoryIdEqualTo(cid).andIsOnSaleEqualTo(true)
				.andDeletedEqualTo(false);
		example.setOrderByClause("browse desc");
		PageHelper.startPage(offset, limit);

		return goodsMapper.selectByExampleSelective(example, columns);
	}

	public List<DtsGoodsType> selectShopGoodsPermission(Integer shopId) {
		return goodsMapper.selectShopGoodsPermission(shopId);
	}

	public List<DtsCategory> selectCategory(Integer shopId) {
		return goodsMapper.selectCategory(shopId);
	}

	public List queryByType(int shopType) {
		DtsGoodsExample example = new DtsGoodsExample();
		example.or().andIsOnSaleEqualTo(true).andDeletedEqualTo(false).andShopTypeEqualTo(shopType);
		example.setOrderByClause("browse desc");
		PageHelper.startPage(0, 10);
		return goodsMapper.selectByExampleSelective(example, columns);
	}

	public List<DtsGoods> queryByShopCategory(List<Integer> l2List, int offset, Integer limit) {
		DtsGoodsExample example = new DtsGoodsExample();
		example.or().andIsOnSaleEqualTo(true).andDeletedEqualTo(false).andShopTypeEqualTo(1).andShopCategoryIdIn(l2List);
		example.setOrderByClause("browse desc");
		PageHelper.startPage(offset, limit);
		return goodsMapper.selectByExampleSelective(example,columns);
	}

	public List<DtsGoods> selectShopGoodsByShopCategoryId(List<DtsShopCategoryEntity> dtsShopCategoryChildList, Integer page, Integer limit, List<Integer> shopType) {
		List<Integer> categoryIdList = new ArrayList<>();
		for(DtsShopCategoryEntity dtsShopCategoryEntity:dtsShopCategoryChildList){
			Integer id = dtsShopCategoryEntity.getId();
			categoryIdList.add(id);
		}
		DtsGoodsExample example = new DtsGoodsExample();
		example.or().andIsOnSaleEqualTo(true).andApproveStatusEqualTo(1).andDeletedEqualTo(false).andShopTypeIn(shopType).andShopCategoryIdIn(categoryIdList);
		example.setOrderByClause("browse desc");
		PageHelper.startPage(page, limit);
		return goodsMapper.selectByExampleSelective(example,columns);
	}

	public List<DtsTourismAttack> queryTrourismAttack(String name, Integer page, Integer limit, String sort, String order) {
		PageHelper.startPage(page, limit);
		return goodsMapper.queryTrourismAttack(name,sort,order);
	}

	public void insertTourismAttack(DtsTourismAttack dtsTourismAttack) {
		goodsMapper.insertTourismAttack(dtsTourismAttack);
	}

	public void updateTourismAttack(DtsTourismAttack dtsTourismAttack) {
		goodsMapper.updateTourismAttack(dtsTourismAttack);
	}

	public void deletetourism(Integer id) {
		goodsMapper.deletetourism(id);
	}

	public DtsTourismAttack selectTourismAttackById(Integer id) {
		return goodsMapper.selectTourismAttackById(id);
	}

	public List<DtsGoods> selectAll() {
		return goodsMapper.selectAll();
	}

//	public Integer updateGoodsList(List<DtsGoods> goodsList) {
//		return goodsMapper.updateGoodsList(goodsList);
//	}

	public Integer updateIsOnSale(DtsGoods dtsGoods) {
		return goodsMapper.updateIsOnSale(dtsGoods);
	}

	public DtsGoods queryById(Integer id) {
		DtsGoodsExample example = new DtsGoodsExample();
		example.or().andIdEqualTo(id);
		return goodsMapper.selectOneByExampleWithBLOBs(example);
	}

	public Integer updateGoodsList(List<DtsGoods> updateGoodsList) {
		return goodsMapper.updateGoodsList(updateGoodsList);
	}
}
