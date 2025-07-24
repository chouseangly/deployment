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
    public Favourite removeFavourite(Long userId, Long productId) {
        return favouriteRepo.removeFavourite(userId, productId);
    }

    @Override
    public boolean isFavourite(Long userId, Long productId) {
        return favouriteRepo.isFavourite(userId, productId);
    }

    @Override
    public List<Favourite> getFavouritesWithProductsByUserId(Long userId) {
        return favouriteRepo.getFavouritesWithProductByUserId(userId);
    }

    @Override
    public List<Long> getUserIdByProductId(Long productId) {
        return favouriteRepo.findUserIdsByProductId(productId);
    }
}