package com.abdelrahman.accountpromax.models

data class ClientBalanceUi(
    val clientId: Long,
    val clientName: String,
    val lehTotal: Double,
    val alehTotal: Double
) {
    val balance: Double get() = lehTotal - alehTotal
}
