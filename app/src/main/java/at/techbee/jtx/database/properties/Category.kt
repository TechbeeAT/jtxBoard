/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.properties

import android.content.ContentValues
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.*
import at.techbee.jtx.database.COLUMN_ID
import at.techbee.jtx.database.ICalObject
import kotlinx.parcelize.Parcelize
import java.util.regex.Pattern

/** The name of the the table for Categories that are linked to an ICalObject.
 * [https://tools.ietf.org/html/rfc5545#section-3.8.1.2]*/
const val TABLE_NAME_CATEGORY = "category"

/** The name of the ID column for categories.
 * This is the unique identifier of a Category
 * Type: [Long]*/
const val COLUMN_CATEGORY_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects.
 * Type: [Long] */
const val COLUMN_CATEGORY_ICALOBJECT_ID = "icalObjectId"


/* The names of all the other columns  */
/**
 * Purpose:  This property defines the name of the category for a calendar component.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.2]
 * Type: [String]
 */
const val COLUMN_CATEGORY_TEXT = "text"
/**
 * Purpose:  To specify the language for text values in a property or property parameter, in this case of the category.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.2] and [https://tools.ietf.org/html/rfc5545#section-3.2.10]
 * Type: [String]
 */
const val COLUMN_CATEGORY_LANGUAGE = "language"
/**
 * Purpose:  To specify other properties for the category.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.2]
 * Type: [String]
 */
const val COLUMN_CATEGORY_OTHER = "other"

@Parcelize
@Entity(tableName = TABLE_NAME_CATEGORY,
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf(COLUMN_ID),
                childColumns = arrayOf(COLUMN_CATEGORY_ICALOBJECT_ID),
                onDelete = ForeignKey.CASCADE)],
)

data class Category (

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(index = true, name = COLUMN_CATEGORY_ID)
        var categoryId: Long = 0L,

        @ColumnInfo(index = true, name = COLUMN_CATEGORY_ICALOBJECT_ID)     var icalObjectId: Long = 0L,
        @ColumnInfo(index = true, name = COLUMN_CATEGORY_TEXT)                       var text: String = "",
        @ColumnInfo(name = COLUMN_CATEGORY_LANGUAGE)                   var language: String? = null,
        @ColumnInfo(name = COLUMN_CATEGORY_OTHER)                      var other: String? = null
): Parcelable


{
    companion object Factory {

        /**
         * Create a new [Category] from the specified [ContentValues].
         *
         * @param values A [Category] that at least contain [COLUMN_CATEGORY_TEXT] and [COLUMN_CATEGORY_ICALOBJECT_ID]
         * @return A newly created [Category] instance.
         */
        fun fromContentValues(values: ContentValues?): Category? {

            if (values == null)
                return null

            if (values.getAsLong(COLUMN_CATEGORY_ICALOBJECT_ID) == null || values.getAsString(COLUMN_CATEGORY_TEXT) == null)     // at least a icalObjectId and text must be given for a Comment!
                return null

            return Category().applyContentValues(values)
        }


        /**
         * extract categories (all words that start with #) and return a list of categories
         * @return a list of [Category] from the text (# included)
         */
        fun extractHashtagsFromText(text: String?): List<Category> {

            if(text.isNullOrEmpty())
                return emptyList()

            val categories = mutableListOf<Category>()
            val matcher = Pattern.compile("#[a-zA-Z0-9]*").matcher(text)
            while (matcher.find()) {
                if(matcher.group().length >= 2)    // hashtag should have at least one character
                    categories.add(Category(text = matcher.group()))
            }
            return categories
        }
    }


    fun applyContentValues(values: ContentValues): Category {

        values.getAsLong(COLUMN_CATEGORY_ICALOBJECT_ID)?.let { icalObjectId -> this.icalObjectId = icalObjectId }
        values.getAsString(COLUMN_CATEGORY_TEXT)?.let { text -> this.text = text }
        values.getAsString(COLUMN_CATEGORY_LANGUAGE)?.let { language -> this.language = language }
        values.getAsString(COLUMN_CATEGORY_OTHER)?.let { other -> this.other = other }

        return this
    }
}

