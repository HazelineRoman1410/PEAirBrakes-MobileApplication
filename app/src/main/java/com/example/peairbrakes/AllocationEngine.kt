package com.example.peairbrakes

import kotlin.math.*

data class TechAllocationResult(
    val technician: User,
    val score: Double,
    val distanceKm: Double,
    val activeJobsCount: Int,
    val suitabilityReason: String
)

object AllocationEngine {

    /**
     * Determines required specialty from breakdown description or category
     */
    fun determineRequiredSpecialty(description: String): String {
        val descLower = description.lowercase()
        return when {
            descLower.contains("brake") || descLower.contains("air") || descLower.contains("compressor") || descLower.contains("valve") -> "Air Brake Specialist"
            descLower.contains("engine") || descLower.contains("diesel") || descLower.contains("smoke") || descLower.contains("injector") -> "Truck Engine Mechanic"
            descLower.contains("electric") || descLower.contains("battery") || descLower.contains("alternator") || descLower.contains("wiring") || descLower.contains("light") -> "Truck Electrical Specialist"
            descLower.contains("transmission") || descLower.contains("gear") || descLower.contains("clutch") -> "Transmission Specialist"
            descLower.contains("tyre") || descLower.contains("wheel") || descLower.contains("puncture") || descLower.contains("tire") -> "Wheel and Tyre Specialist"
            descLower.contains("hydraulic") || descLower.contains("fluid") || descLower.contains("leak") -> "Hydraulic Systems Specialist"
            else -> "General Truck Mechanic"
        }
    }

    /**
     * Ranks all available technicians for a given breakdown request
     */
    fun rankTechnicians(db: DatabaseHelper, request: AssistanceRequest): List<TechAllocationResult> {
        val allTechs = db.getAllTechnicians()
        val allRequests = db.getAllRequests()

        val results = mutableListOf<TechAllocationResult>()
        val reqSpecialty = if (request.specialtyRequired.isNotEmpty() && request.specialtyRequired != "General Truck Mechanic") {
            request.specialtyRequired
        } else {
            determineRequiredSpecialty(request.description)
        }

        for (tech in allTechs) {
            // Filter out offline technicians
            if (tech.availabilityStatus == "Offline") continue

            // Count current active jobs assigned to this technician
            val activeJobs = allRequests.count {
                (it.assignedTo == tech.email || it.secondaryTechs.contains(tech.email)) &&
                it.status != "Completed" && it.status != "Closed" && it.status != "Invoiced"
            }

            // Calculate distance in KM using Haversine formula
            val distance = calculateDistance(
                request.latitude, request.longitude,
                tech.latitude, tech.longitude
            )

            var score = 0.0

            // 1. Specialty Match (+40 points max)
            if (tech.specialty.equals(reqSpecialty, ignoreCase = true)) {
                score += 40.0
            } else if (tech.specialty.contains("General", ignoreCase = true)) {
                score += 20.0
            }

            // 2. Qualifications & Experience (+20 points max)
            score += min(20.0, tech.experienceYears * 2.0)

            // 3. Availability (+15 points max)
            if (tech.availabilityStatus == "Available") {
                score += 15.0
            }

            // 4. Workload (+15 points max: subtract 5 pts per active job)
            val workloadScore = max(0.0, 15.0 - (activeJobs * 5.0))
            score += workloadScore

            // 5. Proximity (+10 points max: inverted distance score)
            val proximityScore = max(0.0, 10.0 - (distance / 10.0))
            score += proximityScore

            val suitabilityReason = "Score: ${score.toInt()}/100 | ${tech.specialty} | Distance: ${String.format(java.util.Locale.US, "%.1f", distance)} km | Active Jobs: $activeJobs"

            results.add(TechAllocationResult(tech, score, distance, activeJobs, suitabilityReason))
        }

        // Rank highest score first
        return results.sortedByDescending { it.score }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1 == 0.0 || lon1 == 0.0 || lat2 == 0.0 || lon2 == 0.0) return 15.0
        val r = 6371.0 // Radius of Earth in KM
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
