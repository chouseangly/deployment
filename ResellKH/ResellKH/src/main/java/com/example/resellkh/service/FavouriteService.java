package com.example.resellkh.service;

import com.example.resellkh.model.dto.FavouriteRequest;
import com.example.resellkh.model.entity.Favourite;

import java.util.List;

public interface FavouriteService {
    Favourite addFavourite(FavouriteRequest favouriteRequest);
    Favourite removeFavourite(Long userId, Long productId);
    boolean isFavourite(Long userId, Long productId);
    List<Favourite> getFavouritesWithProductsByUserId(Long userId);
    List<Long> getUserIdByProductId(Long productId);
}