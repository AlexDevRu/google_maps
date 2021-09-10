package com.example.data.mappers

import com.example.data.api.responses.Location
import com.example.data.api.responses.directions.*
import com.example.domain.models.directions.Direction

fun DirectionResponse.toModel(): Direction {
    var dist = 0.0
    var dur = 0.0

    for(d in routes.orEmpty()) {
        for(i in d.legs) {
            dist += i.distance.value
        }
    }

    for(d in routes.orEmpty()) {
        for(i in d.legs) {
            dur += i.duration.value
        }
    }

    return Direction(
        bounds = bounds?.toModel(),
        routes = routes?.map { it.toModel() },
        distance = dist / 1000,
        duration = dur / 3600,
        status = status
    )
}

fun Bounds.toModel(): com.example.domain.models.directions.Bounds {
    return com.example.domain.models.directions.Bounds(
        northeast = northeast.toModel(),
        southwest = southwest.toModel()
    )
}

fun Route.toModel(): com.example.domain.models.directions.Route {
    return com.example.domain.models.directions.Route(
        legs = legs.map { it.toModel() },
        overview_polyline = overview_polyline.toModel(),
        summary = summary
    )
}

fun Leg.toModel(): com.example.domain.models.directions.Leg {
    return com.example.domain.models.directions.Leg(
        distance = distance.toModel(),
        duration = duration.toModel(),
        end_address = end_address,
        end_location = end_location.toModel(),
        start_address = start_address,
        steps = steps?.map { it.toModel() }
    )
}

fun Step.toModel(): com.example.domain.models.directions.Step {
    return com.example.domain.models.directions.Step(
        distance = distance.toModel(),
        duration = duration.toModel(),
        end_location = end_location.toModel(),
        html_instructions = html_instructions,
        polyline = polyline.toModel(),
        start_location = start_location.toModel(),
        travel_mode = travel_mode
    )
}

fun Distance.toModel(): com.example.domain.models.directions.Distance {
    return com.example.domain.models.directions.Distance(
        text = text,
        value = value
    )
}

fun Duration.toModel(): com.example.domain.models.directions.Duration {
    return com.example.domain.models.directions.Duration(
        text = text,
        value = value
    )
}

fun Location.toModel(): com.example.domain.models.Location {
    return com.example.domain.models.Location(
        lat = lat,
        lng = lng
    )
}

fun Polyline.toModel(): com.example.domain.models.directions.Polyline {
    return com.example.domain.models.directions.Polyline(
        points = points
    )
}