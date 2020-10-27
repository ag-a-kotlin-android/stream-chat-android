package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.api.models.QuerySort.SortAttribute.FieldSortAttribute
import io.getstream.chat.android.client.extensions.camelCaseToSnakeCase
import io.getstream.chat.android.client.extensions.snakeToLowerCamelCase
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

public class QuerySort<T : Any> {
    public var sortSpecifications: List<SortSpecification<T>> = emptyList()
        private set

    private fun add(sortSpecification: SortSpecification<T>): QuerySort<T> {
        sortSpecifications = sortSpecifications + sortSpecification
        return this
    }

    public fun asc(field: KProperty1<T, out Comparable<*>?>): QuerySort<T> {
        return add(SortSpecification(FieldSortAttribute(field, field.name.camelCaseToSnakeCase()), SortDirection.ASC))
    }

    public fun desc(field: KProperty1<T, out Comparable<*>?>): QuerySort<T> {
        return add(SortSpecification(FieldSortAttribute(field, field.name.camelCaseToSnakeCase()), SortDirection.DESC))
    }

    public fun asc(fieldName: String, javaClass: Class<T>): QuerySort<T> {
        return add(SortSpecification(getSortFeature(fieldName, javaClass), SortDirection.ASC))
    }

    public fun desc(fieldName: String, javaClass: Class<T>): QuerySort<T> {
        return add(SortSpecification(getSortFeature(fieldName, javaClass), SortDirection.DESC))
    }

    public fun asc(fieldName: String, kClass: KClass<T>): QuerySort<T> {
        return add(SortSpecification(getSortFeature(fieldName, kClass), SortDirection.ASC))
    }

    public fun desc(fieldName: String, kClass: KClass<T>): QuerySort<T> {
        return add(SortSpecification(getSortFeature(fieldName, kClass), SortDirection.DESC))
    }

    public fun toMap(): Map<String, Any> = sortSpecifications.flatMap { sortSpec ->
        listOf(KEY_FIELD_NAME to sortSpec.sortAttribute.name, KEY_DIRECTION to sortSpec.sortDirection.value)
    }.toMap()

    private fun getSortFeature(fieldName: String, javaClass: Class<T>): SortAttribute<T> {
        val kClass = Reflection.createKotlinClass(javaClass) as KClass<T>
        return getSortFeature(fieldName, kClass)
    }

    private fun getSortFeature(fieldName: String, kClass: KClass<T>): SortAttribute<T> {
        return kClass.members.filterIsInstance<KProperty1<T, Comparable<*>?>>()
            .firstOrNull { it.name == fieldName.snakeToLowerCamelCase() }?.let { FieldSortAttribute(it, fieldName) }
            ?: SortAttribute.FieldNameSortAttribute(fieldName)
    }

    public data class SortSpecification<T>(
        val sortAttribute: SortAttribute<T>,
        val sortDirection: SortDirection
    )

    public sealed class SortAttribute<T>(public val name: String) {
        public class FieldSortAttribute<T>(public val field: KProperty1<T, Comparable<*>?>, name: String) :
            SortAttribute<T>(name)

        public class FieldNameSortAttribute<T>(fieldName: String) : SortAttribute<T>(fieldName)
    }

    public companion object {
        public const val KEY_DIRECTION: String = "direction"
        public const val KEY_FIELD_NAME: String = "field"

        public inline fun <reified T : Any> QuerySort<T>.asc(fieldName: String): QuerySort<T> = asc(fieldName, T::class)
        public inline fun <reified T : Any> QuerySort<T>.desc(fieldName: String): QuerySort<T> =
            desc(fieldName, T::class)

        public inline fun <reified T : Any> asc(fieldName: String): QuerySort<T> = QuerySort<T>().asc(fieldName)
        public inline fun <reified T : Any> desc(fieldName: String): QuerySort<T> = QuerySort<T>().desc(fieldName)
        public inline fun <reified T : Any> asc(field: KProperty1<T, Comparable<*>?>): QuerySort<T> =
            QuerySort<T>().asc(field)

        public inline fun <reified T : Any> desc(field: KProperty1<T, Comparable<*>?>): QuerySort<T> =
            QuerySort<T>().desc(field)
    }

    public enum class SortDirection(public val value: Int) {
        DESC(-1), ASC(1)
    }
}
