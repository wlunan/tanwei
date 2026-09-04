package com.tanwei.service;

import com.tanwei.dto.Result;
import com.tanwei.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2021-12-22
 */
public interface IShopTypeService extends IService<ShopType> {

    Result queryShopTypeList();
}
