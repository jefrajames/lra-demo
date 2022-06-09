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

package io.jefrajames.lrademo.holiday.control.trip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.jefrajames.lrademo.holiday.entity.TransportType;
import lombok.Data;

@Data
public class TripBookResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("booked_at")
    private LocalDateTime bookedAt;

    private String destination;

    private String departure;

    @JsonProperty("seat_count")
    private Integer seatCount;

    @JsonProperty("departure_date")
    private LocalDate departureDate;

    @JsonProperty("return_date")
    private LocalDate returnDate;

    @JsonProperty("first_class")
    private Boolean firstClass;

    private BigDecimal pricing;

    private TripStatus status;

    @JsonProperty("transport_type")
    private TransportType transportType;

    @JsonProperty("departure_time")
    private LocalTime departureTime;

    @JsonProperty("return_time")
    private LocalTime returnTime;

    @JsonProperty("business_error")
    private String businessError;

}