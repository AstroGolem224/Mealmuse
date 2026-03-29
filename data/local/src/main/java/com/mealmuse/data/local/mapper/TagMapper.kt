package com.mealmuse.data.local.mapper

import com.mealmuse.data.local.entity.TagEntity
import com.mealmuse.domain.model.Tag

fun TagEntity.toDomain(): Tag = Tag(
    id = id,
    name = name,
    color = color
)

fun Tag.toEntity(): TagEntity = TagEntity(
    id = id,
    name = name,
    color = color
)
