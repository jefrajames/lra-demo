// Copyright 2022 jefrajames
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.jefrajames.lrademo.trip.control;

import java.math.BigDecimal;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import io.jefrajames.lrademo.trip.entity.CompensatedLRA;
import io.jefrajames.lrademo.trip.entity.Destination;
import io.jefrajames.lrademo.trip.entity.DestinationDB;
import io.jefrajames.lrademo.trip.entity.Trip;
import io.jefrajames.lrademo.trip.entity.TripStatus;
import lombok.extern.java.Log;

@ApplicationScoped
@Log
public class TripService {

    @Inject
    DestinationDB destinationDB;

    public Trip findById(Long id) {
        return Trip.findById(id);
    }

    public List<Trip> findByDestination(String destination) {
        return Trip.find("destination", destination).list();
    }

    private boolean checkDeparture(String departure) {
        return "Paris".equals(departure);
    }

    private static void sleep(long howLong) {
        try {
            Thread.sleep(howLong);
        } catch (InterruptedException ignore) {
        }
    }

    private boolean checkAlreadyCompensated(String lraId) {

        log.info("DEMO check if already compensated " + lraId);

        return CompensatedLRA.count("lraId", lraId) != 0;

    }

    private void processBook(Trip trip) {

        log.warning("DEMO booking with thread " + Thread.currentThread());

        if (!checkDeparture(trip.departure)) {
            trip.businessError = "Rejected departure " + trip.departure;
            trip.status = TripStatus.REJECTED;
            return;
        }

        Destination destination = destinationDB.lookup(trip.destination);
        if (destination == null) {
            trip.businessError = "Rejected destination " + trip.destination;
            trip.status = TripStatus.REJECTED;
            return;
        }

        trip.pricing = destination.getPricing().multiply(new BigDecimal(trip.seatCount));
        trip.transportType = destination.getTransportType();
        trip.departureTime = destination.getDepartureTime();
        trip.returnTime = destination.getReturnTime();

        if ("Dublin".equals(trip.destination)) {
            log.warning("DEMO forcing timeout for DUBLIN");
            sleep(1500);
            log.warning("DEMO eof timeout");
        }

        if (checkAlreadyCompensated(trip.lraId))
            trip.status = TripStatus.CANCELED;

        return;
    }

    @Transactional
    public Trip book(Trip trip) {
        processBook(trip);
        Trip.persist(trip);
        return trip;
    }

    @Transactional
    public void compensate(String lraId) {

        Trip trip = Trip.find("lraId", lraId).firstResult();
        if (trip == null) {
            log.warning("DEMO compensating LRA not found " + lraId);
            CompensatedLRA.persist(new CompensatedLRA(lraId.toString()));
            return;
        }

        log.info("TEST, trip to be compensated " + trip);

        if (trip.status != TripStatus.REJECTED)
            trip.status = TripStatus.CANCELED;

    }

    @Transactional
    public void complete(String lraId) {

        Trip trip = Trip.find("lraId", lraId).firstResult();
        if (trip == null) {
            log.warning("DEMO completing LRA not found " + lraId);
            // CompensatedLRA.persist(new CompensatedLRA(lraId.toString()));
            return;
        }

        log.info("TEST, trip to be completed " + trip);

        if (trip.status == TripStatus.PENDING)
            trip.status = TripStatus.ACCEPTED;
    }

}