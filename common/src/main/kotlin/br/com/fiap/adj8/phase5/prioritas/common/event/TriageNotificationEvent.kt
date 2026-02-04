package br.com.fiap.adj8.phase5.prioritas.common.event

import java.time.LocalDateTime
import java.util.*

const val TRIAGE_QUEUE = "prioritas.triage.queue"

data class TriageNotificationEvent(
    val triageId: UUID,
    val assessedAt: LocalDateTime,
    val riskLevel: String,
    val riskColor: String,
    val patientId: UUID,
    val vitalSigns: VitalSignsPayload
)

data class VitalSignsPayload(
    val temperature: Double?,
    val heartRate: Int?,
    val oxygenSaturation: Int?,
    val hasChestPain: Boolean,
    val systolicPressure: Int?,
    val diastolicPressure: Int?
)