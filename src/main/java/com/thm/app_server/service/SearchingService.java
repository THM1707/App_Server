package com.thm.app_server.service;

import com.thm.app_server.model.ParkingLot;
import com.thm.app_server.model.Standard;
import com.thm.app_server.repository.ParkingLotRepository;
import com.thm.app_server.utils.Haversine;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SearchingService {
    private ParkingLotRepository parkingLotRepository;

    public SearchingService(ParkingLotRepository parkingLotRepository) {
        this.parkingLotRepository = parkingLotRepository;
    }

    public Map<Integer, Long> getSortedValues(double lat, double lng, double maxDistance, int budget, int duration,
                                              Standard standard) {
        Map<Integer, Long> result = new HashMap<>();
        List<ParkingLot> parkingLotList = parkingLotRepository.findAll();
        List<ValueRanking> sorted = new ArrayList<>();
        for (ParkingLot p : parkingLotList) {
            LocalDateTime date = LocalDateTime.now();
            String[] closeArray = p.getCloseTime().split(":");
            int remain = Integer.parseInt(closeArray[0]) * 60 + Integer.parseInt(closeArray[1]) - (date.getHour() * 60 + date.getMinute());
            double distance = Haversine.distance(lat, lng, p.getLatitude(), p.getLongitude(), "K");
            if (p.getPrice() <= budget && distance <= maxDistance + 0.5
                    && remain >= duration * 60 && p.getCurrent() < p.getCapacity()) {
                double value = standard.getDistanceWeight() * Math.log(maxDistance / distance)
                        - standard.getPriceWeight() * p.getPrice() / budget;
                System.out.println(p.getId() + ", " + value);
                sorted.add(new ValueRanking(p.getId(), value));
            }
        }
        sorted.sort(Comparator.comparingDouble(ValueRanking::getValue).reversed());
        int count = sorted.size() >= 3 ? 3 : sorted.size();
        for (int i = 0; i < count; i++) {
            result.put(i + 1, sorted.get(i).getId());
        }
        return result;
    }

    class ValueRanking {
        Long id;
        double value;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        ValueRanking(Long id, double value) {
            this.id = id;
            this.value = value;
        }
    }

}
