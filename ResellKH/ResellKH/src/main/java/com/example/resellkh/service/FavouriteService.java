package com.example.resellkh.service;

import com.example.resellkh.model.dto.FavouriteRequest;
import com.example.resellkh.model.entity.Favourite;

import java.util.List;

public interface FavouriteService {
    Favourite addFavourite(FavouriteRequest favouriteRequest);
    Favourite removeFavourite(Integer userId, Integer productId);
    List<Favourite> getFavouritesByUserId(Integer userId);
    boolean isFavourite(Integer userId, Integer productId);
}
