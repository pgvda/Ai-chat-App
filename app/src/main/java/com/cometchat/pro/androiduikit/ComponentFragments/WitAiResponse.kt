package com.cometchat.pro.androiduikit.ComponentFragments

data class WitAiResponse(
    val entities: Map<String, List<Entity>>,
    // Add other fields based on Wit.ai response structure
)

data class Entity(
    val value: String
    // Add other fields based on Wit.ai response structure
)