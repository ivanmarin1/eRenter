package com.vacation.feature.calendar.presentation

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Stable, collision-free ids for newly created apartments and bookings. */
@OptIn(ExperimentalUuidApi::class)
internal fun newId(): String = Uuid.random().toString()
