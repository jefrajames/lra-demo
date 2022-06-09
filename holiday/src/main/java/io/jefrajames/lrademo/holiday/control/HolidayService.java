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

package io.jefrajames.lrademo.holiday.control;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.ProcessingException;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.jefrajames.lrademo.holiday.control.trip.TripBookRequest;
import io.jefrajames.lrademo.holiday.control.trip.TripBookResponse;
import io.jefrajames.lrademo.holiday.control.trip.TripProxy;
import io.jefrajames.lrademo.holiday.entity.CompensatedLRA;
import io.jefrajames.lrademo.holiday.entity.Holiday;
import io.jefrajames.lrademo.holiday.entity.HolidayCategory;
import io.jefrajames.lrademo.holiday.entity.HolidayStatus;
import lombok.extern.java.Log;

@ApplicationScoped
@Log
public class HolidayService {

    private static final BigDecimal MAX_PRICING = new BigDecimal(500.00);
    private static final long CUSTOMER_ID = 42;

    @RestClient
    @Inject
    TripProxy tripProxy;

    private static TripBookRequest generateTripBookRequest(Holiday holiday) {

        TripBookRequest tripBookRequest = new TripBookRequest();

        tripBookRequest.setCustomerId(holiday.customerId);
        tripBookRequest.setDepartureDate(holiday.departureDate);
        tripBookRequest.setDestination(holiday.destination);
        tripBookRequest.setDeparture(holiday.departure);
        tripBookRequest.setFirstClass(holiday.category == HolidayCategory.LUXURY);
        tripBookRequest.setReturnDate(holiday.returnDate);
        tripBookRequest.setSeatCount(holiday.peopleCount);

        return tripBookRequest;
    }

    private boolean checkCustomer(Holiday holiday) {
        return (holiday.customerId == CUSTOMER_ID);
    }

    private boolean checkPricing(Holiday holiday) {
        return (MAX_PRICING.compareTo(holiday.tripPricing) == 1);
    }

    private boolean bookTrip(Holiday holiday) {
        TripBookRequest tripBookRequest = generateTripBookRequest(holiday);

        try {
            TripBookResponse trip = tripProxy.book(tripBookRequest);
            holiday.tripPricing = trip.getPricing();
            holiday.tripId = trip.getId();
            holiday.transportType = trip.getTransportType();
            holiday.departureTime = trip.getDepartureTime();
            holiday.returnTime = trip.getReturnTime();
            holiday.businessError = trip.getBusinessError();

            return holiday.businessError == null;
        } catch (ProcessingException e) {
            // Timeout and co
            log.warning("Demo Got ProcessingException " + e);
            holiday.businessError = "Unable to book trip";
            return false;
        }

    }

    private static void sleep(long howLong) {
        try {
            Thread.sleep(howLong);
        } catch (InterruptedException ignore) {
        }
    }

    private CompensatedLRA checkAlreadyCompensated(String lraId) {

        log.info("DEMO check if already compensated " + lraId);

        return CompensatedLRA.find("lraId", lraId).firstResult();

        // return CompensatedLRA.count("lraId", lraId) != 0;

    }

    private void processBook(Holiday holiday) {

        holiday.status = HolidayStatus.PENDING;

        if (!checkCustomer(holiday)) {
            holiday.businessError = "Unknown customer";
            holiday.status = HolidayStatus.REJECTED;
            return;
        }

        if (!bookTrip(holiday)) {
            holiday.status = HolidayStatus.REJECTED;
            return;
        }

        if (!checkPricing(holiday)) {
            holiday.businessError = "Max pricing exceeded";
            holiday.status = HolidayStatus.REJECTED;
            return;
        }

        // Big timeout if Budapest
        if ("Budapest".equals(holiday.destination))
            sleep(3000);

        CompensatedLRA compensatedLRA = checkAlreadyCompensated(holiday.lraId);
        if (compensatedLRA!=null) {
            // LRA has already been compensated
            holiday.status = HolidayStatus.CANCELED;
            holiday.lraResponseTime = ChronoUnit.MILLIS.between(holiday.bookedAt,
            compensatedLRA.compensatedAt);
        }
        else
            holiday.status = HolidayStatus.ACCEPTED;
    }

    @Transactional
    public Holiday book(Holiday holiday) {

        processBook(holiday);
        holiday.bookResponseTime = ChronoUnit.MILLIS.between(holiday.bookedAt, LocalDateTime.now());
        Holiday.persist(holiday);
        return holiday;
    }

    @Transactional
    public void complete(String lraId) {

        log.info("DEMO calling complete " + lraId);

        Holiday holiday = Holiday.find("lraId", lraId).firstResult();
        if (holiday == null) {
            log.warning("Unable to complete: LRA Id not found " + lraId);
            return;
        }

        holiday.lraResponseTime = ChronoUnit.MILLIS.between(holiday.bookedAt,
                LocalDateTime.now());
    }

    @Transactional
    public void compensate(String lraId) {
        log.info("DEMO compensating LRA " + lraId);

        Holiday holiday = Holiday.find("lraId", lraId).firstResult();
        if (holiday == null) {
            log.warning("DEMO compensating LRA not found " + lraId);
            CompensatedLRA.persist(new CompensatedLRA(lraId.toString()));
            return;
        }

        if (holiday.status == HolidayStatus.ACCEPTED)
            holiday.status = HolidayStatus.CANCELED;

        holiday.lraResponseTime = ChronoUnit.MILLIS.between(holiday.bookedAt,
                LocalDateTime.now());

        log.info("TEST, holiday after compensate " + holiday);
    }

}