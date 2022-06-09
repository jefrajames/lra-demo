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

package io.jefrajames.lrademo.holiday.boundary;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.jefrajames.lrademo.holiday.control.HolidayService;
import io.jefrajames.lrademo.holiday.control.trip.TripProxy;
import io.jefrajames.lrademo.holiday.entity.Holiday;
import lombok.extern.java.Log;

@Log
@ApplicationScoped
@Path("/holiday")
public class HolidayResource {

    @RestClient
    @Inject
    TripProxy tripProxy;

    @Inject
    HolidayService holidayService;

    @Context
    private UriInfo uriInfo;

    private URI resourceUri(long bookingId) {
        return URI.create(String.format("%s/%d", uriInfo.getRequestUri().toString(), bookingId));
    }

    @GET
    @Operation(description = "Find all holidays", summary = "Find all holidays")
    @APIResponse(responseCode = "200", description = "List of all holdays")
    public List<Holiday> findAll() {
        return Holiday.findAll().list();
    }

    @GET
    @Path("/{id}")
    @Operation(description = "Find a holiday by id", summary = "Find a holiday by id")
    @APIResponse(responseCode = "200", description = "Entity found")
    @APIResponse(responseCode = "404", description = "Entity not found")
    @APIResponse(responseCode = "502", description = "No LRA Coordinator")
    public Holiday findById(@PathParam("id") Long id) {
        Holiday holiday = Holiday.findById(id);

        if (holiday == null)
            throw new WebApplicationException(Response.status(404).build());

        return holiday;
    }

    private static Holiday requestToEntity(String lraId, HolidayBookRequest bookRequest) {

        Holiday holiday = new Holiday();

        holiday.customerId = bookRequest.getCustomerId();
        holiday.destination = bookRequest.getDestination();
        holiday.departure = bookRequest.getDeparture();
        holiday.category = bookRequest.getCategory();
        holiday.departureDate = bookRequest.getDepartureDate();
        holiday.returnDate = bookRequest.getReturnDate();
        holiday.category = bookRequest.getCategory();
        holiday.peopleCount = bookRequest.getPeopleCount();
        holiday.lraId = lraId;
        holiday.bookedAt = LocalDateTime.now();

        return holiday;
    }

    @LRA(value = LRA.Type.REQUIRED, end = true, timeLimit = 2, timeUnit = ChronoUnit.SECONDS, cancelOn = {
            Response.Status.INTERNAL_SERVER_ERROR }, cancelOnFamily = { Response.Status.Family.CLIENT_ERROR })
    @POST
    @Path("/book")
    @Operation(description = "Holiday booking with LRA", summary = "Book a holiday with LRA")
    @RequestBody(name = "holiday", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HolidayBookRequest.class), examples = {
            @ExampleObject(name = "Let's go to London", value = "{\"customer_id\": 42, \"departure\": \"Paris\", \"destination\": \"London\", \"departure_date\": \"2023-04-23\", \"return_date\": \"2023-04-26\", \"category\": \"BASIC\", \"people_count\": 1 }"),
            @ExampleObject(name = "Let's go to Dublin", value = "{\"customer_id\": 42, \"departure\": \"Paris\", \"destination\": \"Dublin\", \"departure_date\": \"2023-04-23\", \"return_date\": \"2023-04-26\", \"category\": \"BASIC\", \"people_count\": 2 }"),
            @ExampleObject(name = "Let's go to Budapest", value = "{\"customer_id\": 42, \"departure\": \"Paris\", \"destination\": \"Budapest\", \"departure_date\": \"2023-04-23\", \"return_date\": \"2023-04-26\", \"category\": \"BASIC\", \"people_count\": 2 }")
    }), required = true, description = "Booking holiday to London")
    @APIResponse(responseCode = "201", description = "Holiday booked", content = @Content(mediaType = "application/json"))
    @APIResponse(responseCode = "422", description = "Holiday book rejected")
    @APIResponse(responseCode = "502", description = "No LRA Coordinator")
    public Response book(
            @Parameter(description = "LRA id (automatically provided)", hidden = true) @HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId,
            @Parameter(description = "Booking request", required = true) @Valid HolidayBookRequest bookRequest) {

        log.info("\n\n\nDEMO booking holiday, starting LRA " + lraId);

        Holiday holiday = holidayService.book(requestToEntity(lraId, bookRequest));

        if (holiday.businessError != null)
            return Response.status(422).entity(holiday).header("Content-Location", resourceUri(holiday.id)).build();

        return Response.created(resourceUri(holiday.id)).entity(holiday).build();
    }

    @Complete
    @Path("/complete")
    @Operation(description = "Complete a LRA", summary = "LRA complete", hidden = true)
    @Produces(MediaType.APPLICATION_JSON)
    @PUT
    public Response complete(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) {
        log.info("DEMO completing holiday LRA " + lraId);
        holidayService.complete(lraId);
        return Response.ok().build();
    }

    @Compensate
    @Path("/compensate")
    @Operation(description = "Compensate a LRA", summary = "LRA compensate", hidden = true)
    @PUT
    public Response compensate(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) {
        log.info("DEMO compensating holiday LRA " + lraId);
        holidayService.compensate(lraId);
        return Response.ok().build();
    }

    @POST
    @Path("/nolra")
    @Operation(description = "Holiday booking without LRA", summary = "Book a holiday without LRA")
    @APIResponse(responseCode = "201", description = "Holiday book accepted", content = @Content(mediaType = "application/json"))
    @RequestBody(name = "holiday", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HolidayBookRequest.class), examples = @ExampleObject(name = "Let's go to London", value = "{\"customer_id\": 42, \"departure\": \"Paris\", \"destination\": \"London\", \"departure_date\": \"2023-04-23\", \"return_date\": \"2023-04-26\", \"category\": \"BASIC\", \"people_count\": 1 }")), required = true, description = "Booking holiday to London")
    @APIResponse(responseCode = "422", description = "Holiday book refused")
    public Response bookNoLra(
            @Parameter(description = "Booking request", required = true) @Valid HolidayBookRequest bookRequest) {

        log.info("DEMO booking holiday without LRA");

        Holiday holiday = holidayService.book(requestToEntity(null, bookRequest));

        if (holiday.businessError != null)
            return Response.status(422).entity(holiday).header("Content-Location", resourceUri(holiday.id)).build();
        else
            return Response.created(resourceUri(holiday.id)).entity(holiday).build();
    }

}