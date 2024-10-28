package com.sph.sphmedia

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.single

/**
 * Extracts the list of data from a PagingData object.
 * Useful for testing transformations on PagingData.
 *
 * flowOf(PagingData.from(listOf(model)).toList() == listOf(model)
 *
 * When nothing else is left, Java reflection will always be there to help us out.
 */
@Suppress("UNCHECKED_CAST")
suspend fun <T : Any> PagingData<T>.toList(): List<T> {
    val flow = PagingData::class.java.getDeclaredField("flow").apply {
        isAccessible = true
    }.get(this) as Flow<Any?>
    val pageEventInsert = flow.single()
    val pageEventInsertClass = Class.forName("androidx.paging.PageEvent\$Insert")
    val pagesField = pageEventInsertClass.getDeclaredField("pages").apply {
        isAccessible = true
    }
    val pages = pagesField.get(pageEventInsert) as List<Any?>
    val transformablePageDataField =
        Class.forName("androidx.paging.TransformablePage").getDeclaredField("data").apply {
            isAccessible = true
        }
    val listItems =
        pages.flatMap { transformablePageDataField.get(it) as List<*> }
    return listItems as List<T>
}