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

package io.jefrajames.lrademo.holiday.entity;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.ToString;

@Entity
@Table(name="holiday", indexes = @Index(columnList = "lra_id"))
@JsonPropertyOrder({"id", "booked_at", "status", "business_error", "lra_id", "book_response_time", "lra_response_time", "customer_id", "departure", "destination", "departure_date", "return_date", "category", "people_count", "trip_id", "departure_time", "return_time", "transport_type", "pricing", "trip_pricing"})
@ToString(includeFieldNames = true)
public class Holiday extends PanacheEntity {

    @JsonProperty("booked_at")
    @Column(name="booked_at", updatable = false)
    public LocalDateTime bookedAt;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    public HolidayStatus status;

    @Column(name="business_error")
    @JsonProperty("business_error")
    public String businessError;

    @Column(name="lra_id", unique= true, updatable = false)
    @JsonProperty("lra_id")
    public String lraId;

    @Column(name="book_response_time")
    @JsonProperty("book_response_time")
    public long bookResponseTime;

    @Column(name="lra_response_time")
    @JsonProperty("lra_response_time")
    public long lraResponseTime;

    @Column(name = "customer_id", updatable = false)
    @JsonProperty("customer_id")
    public Long customerId;

    @Column(name="departure", updatable=false)
    public String departure;

    @Column(name="destination", updatable=false)
    public String destination;

    @Column(name = "departure_date", updatable=false)
    @JsonProperty("departure_date")
    public LocalDate departureDate;

    @Column(name = "return_date", updatable=false)
    @JsonProperty("return_date")
    public LocalDate returnDate;

    @Column(name = "category", updatable=false)
    @Enumerated(EnumType.STRING)
    public HolidayCategory category;

    @Column(name= "people_count", updatable=false)
    @JsonProperty("people_count")
    public int peopleCount;

    @Column(name="trip_id")
    @JsonProperty("trip_id")
    public Long tripId;

    @Column(name="trip_pricing", precision=10, scale = 2)
    @JsonProperty("trip_pricing")
    public BigDecimal tripPricing;

    @Column(name="transport_type")
    @JsonProperty("transport_type")
    public TransportType transportType;

    @JsonProperty("departure_time")
    @Column(name="departure_time")
    public LocalTime departureTime;

    @JsonProperty("return_time")
    @Column(name="return_time")
    public LocalTime returnTime;

}
