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

package io.jefrajames.lrademo.trip.boundary;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.jefrajames.lrademo.trip.control.TripService;
import io.jefrajames.lrademo.trip.entity.Trip;
import io.jefrajames.lrademo.trip.entity.TripStatus;
import lombok.extern.java.Log;

@ApplicationScoped
@Path("/trips")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Log
public class TripResource {

    @Inject
    TripService tripService;

    @Context
    UriInfo uriInfo;

    private URI resourceUri(long bookingId) {
        return URI.create(String.format("%s/%d", uriInfo.getRequestUri().toString(), bookingId));
    }

    @GET
    @Path("to/{destination}")
    @Operation(description = "Find by destination", summary = "Find by destination")
    public List<Trip> findByDestination(
        @Parameter(name = "destination", description = "The holiday destination", required = true, allowEmptyValue = false, example = "Dublin")
        @PathParam("destination") String destination) {
        return tripService.findByDestination(destination);
    }
    

    @GET
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Trip found", content = @Content(mediaType = "application/json"))
    @APIResponse(responseCode = "204", description = "Trip not found")
    @Operation(description = "Find a trip by id", summary = "Find by id")
    public Trip findById(@PathParam("id") Long id) {
        return tripService.findById(id);
    }

    private static Trip requestToEntity(String lraId, TripBookRequest bookRequest) {
        Trip trip = new Trip();

        trip.bookedAt=LocalDateTime.now();
        trip.customerId = bookRequest.getCustomerId();
        trip.departure=bookRequest.getDeparture();
        trip.destination = bookRequest.getDestination();
        trip.departureDate = bookRequest.getDepartureDate();
        trip.returnDate=bookRequest.getReturnDate();
        trip.seatCount=bookRequest.getSeatCount();
        trip.firstClass=bookRequest.getFirstClass();
        trip.pricing = new BigDecimal(0.0);
        trip.status = TripStatus.PENDING;
        trip.lraId=lraId;

        return trip;
    }

    @LRA(value = LRA.Type.SUPPORTS, end = false)
    @POST
    @Path("/book")
    @Operation(description = "Trip booking", summary = "Book a trip", hidden = true)
    @APIResponse(responseCode = "201", description = "Trip book pending (waiting for completion)", content = @Content(mediaType = "application/json"))
    @APIResponse(responseCode = "422", description = "Trip book rejected")
    public Response book(
            @Parameter(description = "LRA id (automatically provided)", hidden = true) @HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId,
            @Parameter(description = "Booking request", required = true) @Valid TripBookRequest bookRequest) {

        log.warning("\n\n\nDEMO booking trip LRA " + lraId);

        Trip trip = tripService.book(requestToEntity(lraId, bookRequest));

        return Response.created(resourceUri(trip.id)).entity(trip).build();
    }

    @Compensate
    @Path("/compensate")
    @Consumes(MediaType.TEXT_PLAIN)
    @PUT
    @Operation(description = "Compensate a Long Running Action", summary = "LRA compensate", hidden=true)
    public Response compensate(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) {
        log.warning("DEMO compensating LRA " + lraId);
        tripService.compensate(lraId);
        return Response.ok().build();
    }

    @Complete
    @Path("/complete")
    @Consumes(MediaType.TEXT_PLAIN)
    @PUT
    @Operation(description = "Complete a Long Running Action", summary = "LRA completion", hidden=true)
    public Response complete(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) String lraId) {
        log.warning("DEMO completing LRA " + lraId);
        tripService.complete(lraId);
        return Response.ok().build();
    }

}