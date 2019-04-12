package com.thm.app_server.service.impl;

import com.thm.app_server.exception.ResourceNotFoundException;
import com.thm.app_server.model.ParkingLot;
import com.thm.app_server.model.Review;
import com.thm.app_server.repository.ParkingLotRepository;
import com.thm.app_server.repository.ReviewRepository;
import com.thm.app_server.service.ReviewService;
import org.springframework.stereotype.Service;

@Service
public class ReviewServiceImpl implements ReviewService {


    private ReviewRepository reviewRepository;
    private ParkingLotRepository parkingLotRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository, ParkingLotRepository parkingLotRepository) {
        this.reviewRepository = reviewRepository;
        this.parkingLotRepository = parkingLotRepository;
    }

    @Override
    public void addReview(Review review) {
        ParkingLot parkingLot = review.getTarget();
        int count = parkingLot.getReviewCount();
        int sum = parkingLot.getSum();
        count++;
        sum += review.getStar();
        parkingLot.setReviewCount(count);
        parkingLot.setSum(sum);
        parkingLot.setStar(sum * 1f / count);
        review.setTarget(parkingLot);
        reviewRepository.save(review);
        parkingLotRepository.save(parkingLot);
    }

    @Override
    public Review findById(Long id) {
        return reviewRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review", "ID", id));
    }

    @Override
    public Review editReview(Long id, int star, String comment) {
        Review review = findById(id);
        review.setComment(comment);
        int oldStar = review.getStar();
        review.setStar(star);
        ParkingLot parkingLot = review.getTarget();
        int sum = parkingLot.getSum();
        sum = sum - oldStar + star;
        parkingLot.setSum(sum);
        parkingLot.setStar((float) (sum * 1f / parkingLot.getReviewCount()));
        reviewRepository.save(review);
        parkingLotRepository.save(parkingLot);
        return review;
    }

    @Override
    public void deleteReview(Long id) {
        Review review = findById(id);
        ParkingLot parkingLot = review.getTarget();
        int count = parkingLot.getReviewCount();
        int sum = parkingLot.getSum();
        count -= 1;
        sum -= review.getStar();
        parkingLot.setReviewCount(count);
        parkingLot.setSum(sum);
        parkingLot.setStar(count == 0 ? 0 : (sum * 1f / count));
        reviewRepository.delete(review);
        parkingLotRepository.save(parkingLot);
    }

}
