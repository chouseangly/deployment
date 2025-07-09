package com.example.resellkh.service.Impl;
import com.example.resellkh.model.dto.FavouriteRequest;
import com.example.resellkh.model.entity.Favourite;
import com.example.resellkh.repository.FavouriteRepo;
import com.example.resellkh.service.FavouriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavouriteServiceImpl implements FavouriteService {

    private final FavouriteRepo favouriteRepo;

    @Override
    public Favourite addFavourite(FavouriteRequest favouriteRequest) {
        if (favouriteRepo.isFavourite(favouriteRequest.getUserId(), favouriteRequest.getProductId())) {
            throw new IllegalStateException("This product is already in favourites.");
        }
        favouriteRequest.setCreatedAt(LocalDateTime.now());
        return favouriteRepo.addFavourite(favouriteRequest);
    }



    @Override
    public Favourite removeFavourite(Integer userId, Integer productId) {
        List<Favourite> list = favouriteRepo.getFavourites(userId, productId);
        if (list == null || list.isEmpty()) return null;

        favouriteRepo.removeFavourite(userId, productId);
        return list.get(0);
    }
    @Override
    public boolean isFavourite(Integer userId, Integer productId) {
        return favouriteRepo.isFavourite(userId, productId);
    }

    @Override
    public List<Favourite> getFavouritesWithProductsByUserId(Integer userId) {
        return favouriteRepo.getFavouritesWithProductByUserId(userId);
    }

}
