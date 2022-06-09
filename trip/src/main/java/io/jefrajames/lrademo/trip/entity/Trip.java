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

package io.jefrajames.lrademo.trip.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.ToString;

@Entity
@Table(name="trip", indexes = @Index(columnList = "lra_id"))
@JsonPropertyOrder({"id", "booked_at", "status", "business_error", "lra_id", "book_response_time", "customer_id", "departure", "destination", "departure_date", "return_date", "category", "people_count", "departure_time", "return_time", "transport_type", "pricing"})
@ToString
public class Trip extends PanacheEntity {

    @Column(name = "customer_id")
    @JsonProperty("customer_id")
    public Long customerId;

    @JsonProperty("booked_at")
    @Column(name="booked_at", updatable = false)
    public LocalDateTime bookedAt;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    public TripStatus status;

    @NotEmpty
    @Column(name="departure", updatable = false)
    public String departure;

    @NotEmpty
    @Column(name="destination", updatable = false)
    public String destination;

    @JsonProperty("seat_count")
    @Column(name="seat_count", updatable = false)
    public Integer seatCount;

    @JsonProperty("departure_date")
    @Column(name="departure_date", updatable = false)
    public LocalDate departureDate;

    @JsonProperty("return_date")
    @Column(name="return_date", updatable = false)
    public LocalDate returnDate;

    @JsonProperty("first_class")
    @Column(name="first_class", updatable = false)
    public Boolean firstClass;

    @Column(name="pricing", precision=10, scale = 2)
    public BigDecimal pricing;


    @Enumerated(EnumType.STRING)
    @Column(name="transport_type")
    @JsonProperty("transport_type")
    public TransportType transportType;
    
    @Column(name="lra_id", unique = true, updatable = false)
    @JsonProperty("lra_id")
    public String lraId;
    
    @Column(name="business_error")
    @JsonProperty("business_error")
    public String businessError;
    
    @JsonProperty("departure_time")
    @Column(name="departure_time")
    public LocalTime departureTime;

    @JsonProperty("return_time")
    @Column(name="return_time")
    public LocalTime returnTime;
}
