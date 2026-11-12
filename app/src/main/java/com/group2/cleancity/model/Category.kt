package com.group2.cleancity.model

data class Category (
    val id: String,
    val name: String,
    val description: String,
    val defaultPriority: Priority
)