package com.abdelrahman.accountpromax.models

data class TransactionTimelineItem(
    val tx: TransactionEntity,
    val cumulativeBalance: Double
)
