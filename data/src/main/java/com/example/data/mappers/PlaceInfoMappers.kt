package com.example.data.mappers

import com.example.data.api.responses.place_info.*
import com.example.domain.models.place_info.PlaceInfo

fun PlaceInfoResponse.toModel(): PlaceInfo {
    return PlaceInfo(
        address = result.formatted_address,
        phoneNumber = result.formatted_phone_number,
        name = result.name,
        openingHours = result.opening_hours?.toModel(),
        photos = result.photos?.map { it.toModel() },
        placeId = result.place_id,
        rating = result.rating,
        reviews = result.reviews?.map { it.toModel() },
        types = result.types,
        url = result.url,
        userRatingsTotal = result.user_ratings_total,
        utcOffset = result.utc_offset,
        website = result.website,
        location = result.geometry?.location?.toModel()
    )
}

fun Photo.toModel(): com.example.domain.models.place_info.Photo {
    return com.example.domain.models.place_info.Photo(
        height = height,
        htmlAttributions = html_attributions,
        photoReference = photo_reference,
        width = width
    )
}

fun OpeningHours.toModel(): com.example.domain.models.place_info.OpeningHours {
    return com.example.domain.models.place_info.OpeningHours(
        openNow = open_now,
        periods = periods?.map { it.toModel() },
        weekdayText = weekday_text
    )
}

fun Period.toModel(): com.example.domain.models.place_info.Period {
    return com.example.domain.models.place_info.Period(
        close = close.toModel(),
        open = open.toModel()
    )
}

fun Time.toModel(): com.example.domain.models.place_info.Time {
    return com.example.domain.models.place_info.Time(
        day = day,
        time = "${time.substring(0, 2)}:${time.substring(2, 4)}"
    )
}

fun Review.toModel(): com.example.domain.models.place_info.Review {
    return com.example.domain.models.place_info.Review(
        authorName = author_name,
        authorUrl = author_url,
        profilePhotoUrl = profile_photo_url,
        rating = rating,
        relativeTimeDescription = relative_time_description,
        text = text,
        time = time
    )
}