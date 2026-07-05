package com.vacation.feature.calendar.domain.model

/** A rentable unit inside the vacation house (e.g. "Sea View", "Garden Studio"). */
data class Apartment(
    val id: ApartmentId,
    val name: String,
)
