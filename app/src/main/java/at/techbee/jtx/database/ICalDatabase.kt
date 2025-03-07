/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.VisibleForTesting
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import at.techbee.jtx.R
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredListSetting
import at.techbee.jtx.database.locals.StoredResource
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Attendee
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.properties.Comment
import at.techbee.jtx.database.properties.Organizer
import at.techbee.jtx.database.properties.Relatedto
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.database.properties.TABLE_NAME_ALARM
import at.techbee.jtx.database.properties.Unknown
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.database.views.ICal4List


/**
 * A database that stores ICal information.
 * And a global method to get access to the database.
 */

@Database(
    entities = [
        Attendee::class,
        Category::class,
        Comment::class,
        ICalCollection::class,
        ICalObject::class,
        Organizer::class,
        Relatedto::class,
        Resource::class,
        Alarm::class,
        Unknown::class,
        Attachment::class,
        StoredListSetting::class,
        StoredCategory::class,
        StoredResource::class,
        ExtendedStatus::class],
    views = [
        ICal4List::class,
        CollectionsView::class],
    version = 40,
    exportSchema = true,
    autoMigrations = [
        AutoMigration (from = 2, to = 3, spec = ICalDatabase.AutoMigration2to3::class),
        AutoMigration (from = 3, to = 4),
        AutoMigration (from = 4, to = 5),  // no AutoMigrationSpec needed from 3 to 4
        AutoMigration (from = 5, to = 6),
        AutoMigration (from = 6, to = 7),  // only view update
        AutoMigration (from = 7, to = 8),  // icalobject & view update
        AutoMigration (from = 8, to = 9),  // icalobject & view update
        AutoMigration (from = 9, to = 10),  // view update
        AutoMigration (from = 10, to = 11),  // view update
        AutoMigration (from = 11, to = 12),  // index update
        AutoMigration (from = 13, to = 14),  // view updates
        AutoMigration (from = 14, to = 15),  // view updates
        AutoMigration (from = 15, to = 16),  // view updates
        AutoMigration (from = 16, to = 17),  // view updates
        AutoMigration (from = 17, to = 18),  // room update
        // no AutoMigration from 18 to 19
        AutoMigration (from = 19, to = 20, spec = ICalDatabase.AutoMigration19to20::class),  // removed recur columns
        AutoMigration (from = 20, to = 21),  // view update
        AutoMigration (from = 21, to = 22),  // view update
        AutoMigration (from = 22, to = 23),  // view update
        AutoMigration (from = 23, to = 24),  // added ListSettingsStorage
        AutoMigration (from = 24, to = 25),  // added StoredCategory, StoredResource
        AutoMigration (from = 25, to = 26),  // added column Parent Expanded
        AutoMigration (from = 26, to = 27),  // added geoLat and geoLong to ical4list view, added StoredCategory, StoredResource
        AutoMigration (from = 27, to = 28),  // added Extended Status
        AutoMigration (from = 28, to = 29),  // added Geofence Radius
        AutoMigration (from = 29, to = 30),  // added recuridTimezone
        // no AutoMigration from 30 to 31
        AutoMigration (from = 31, to = 32),  // view update
        AutoMigration (from = 32, to = 33),  // view update
        AutoMigration (from = 33, to = 34),  // new last sync column in ICalCollection
        AutoMigration (from = 34, to = 35),  // new/updated indices
        AutoMigration (from = 35, to = 36),  // new/updated indices
        AutoMigration (from = 36, to = 37),  // new/updated indices
        AutoMigration (from = 37, to = 38),  // view udpate
        AutoMigration (from = 38, to = 39),  // new column isAlarmNotificationActive + migration spec to remove indices
        AutoMigration (from = 39, to = 40),  // new column syncId in ICalCollection
    ]
)
@TypeConverters(Converters::class)
abstract class ICalDatabase : RoomDatabase() {

    /**
     * Connects the database to the DAO.
     */
    abstract fun iCalDatabaseDao(): ICalDatabaseDao


    @DeleteColumn(tableName = TABLE_NAME_ALARM, columnName = "trigger")
    class AutoMigration2to3: AutoMigrationSpec

    @DeleteColumn.Entries(
        DeleteColumn(tableName = TABLE_NAME_ICALOBJECT, columnName = "recur_original_icalobjectid"),
        DeleteColumn(tableName = TABLE_NAME_ICALOBJECT, columnName = "recur_islinkedinstance")
    )
    class AutoMigration19to20: AutoMigrationSpec


    /**
     * Define a companion object, this allows us to add functions on the SleepDatabase class.
     */
    companion object {
        /**
         * INSTANCE will keep a reference to any database returned via getInstance.
         *
         * This will help us avoid repeatedly initializing the database, which is expensive.
         *
         *  The value of a volatile variable will never be cached, and all writes and
         *  reads will be done to and from the main memory. It means that changes made by one
         *  thread to shared data are visible to other threads.
         */


        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE `contact`")
            }
        }

        // WORKAROUND!!! Recreating the view that was not deleted properly
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP VIEW IF EXISTS `ical4viewNote`")
                db.execSQL("CREATE VIEW `ical4viewNote` AS SELECT icalobject._id, icalobject.module, icalobject.component, icalobject.summary, icalobject.description, icalobject.created, icalobject.lastmodified, relatedto.icalObjectId, attachment.binary, attachment.fmttype, attachment.uri, icalobject.sortIndex FROM icalobject INNER JOIN relatedto ON icalobject._id = relatedto.linkedICalObjectId LEFT JOIN attachment ON icalobject._id = attachment.icalObjectId WHERE icalobject.deleted = 0 AND icalobject.module = 'NOTE'")
            }
        }

        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("update icalobject set uid = (SELECT sub.uid from icalobject sub where sub._id = icalobject.recur_original_icalobjectid) where recur_islinkedinstance = 1")
                db.execSQL("update icalobject set recurid = null where recur_islinkedinstance = 0 and recur_original_icalobjectid is not null")
            }
        }

        private val MIGRATION_30_31 = object : Migration(30, 31) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("update icalobject set recurid = trim(recurid, 'Z') WHERE recurid like '%Z'")
                db.execSQL("update icalobject set recuridtimezone = dtstarttimezone where recurid is not null")
            }
        }

        @Volatile
        private var INSTANCE: ICalDatabase? = null

        /**
         * Helper function to get the database.
         *
         * If a database has already been retrieved, the previous database will be returned.
         * Otherwise, create a new database.
         *
         * This function is threadsafe, and callers should cache the result for multiple database
         * calls to avoid overhead.
         *
         * This is an example of a simple Singleton pattern that takes another Singleton as an
         * argument in Kotlin.
         *
         * To learn more about Singleton read the wikipedia article:
         * https://en.wikipedia.org/wiki/Singleton_pattern
         *
         * @param context The application context Singleton, used to get access to the filesystem.
         */
        fun getInstance(context: Context): ICalDatabase {

            // Multiple threads can ask for the database at the same time, ensure we only initialize
            // it once by using synchronized. Only one thread may enter a synchronized block at a
            // time.
            synchronized(this) {
                // Copy the current value of INSTANCE to a local variable so Kotlin can smart cast.
                // Smart cast is only available to local variables.
                var instance = INSTANCE
                // If instance is `null` make a new database instance.
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            ICalDatabase::class.java,
                            "jtx_database"
                    )
                        .addMigrations(MIGRATION_1_2, MIGRATION_12_13, MIGRATION_18_19, MIGRATION_30_31)

                        // Wipes and rebuilds instead of migrating if no Migration object.
                        // Migration is not part of this lesson. You can learn more about
                        // migration with Room in this blog post:
                        // https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929
                        .fallbackToDestructiveMigration()
                        //This Callback is executed on create and on open. On create the local collection must be initialized.
                        .addCallback( object: Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                val cv = ContentValues()
                                cv.put(COLUMN_COLLECTION_ID, "1")
                                cv.put(COLUMN_COLLECTION_ACCOUNT_TYPE, ICalCollection.LOCAL_ACCOUNT_TYPE)
                                cv.put(COLUMN_COLLECTION_ACCOUNT_NAME, context.getString(R.string.default_local_account_name))
                                cv.put(COLUMN_COLLECTION_DISPLAYNAME, context.getString(R.string.default_local_collection_name))
                                cv.put(COLUMN_COLLECTION_URL, ICalCollection.LOCAL_COLLECTION_URL)
                                cv.put(COLUMN_COLLECTION_SUPPORTSVJOURNAL, 1)
                                cv.put(COLUMN_COLLECTION_SUPPORTSVEVENT, 1)
                                cv.put(COLUMN_COLLECTION_SUPPORTSVTODO, 1)
                                cv.put(COLUMN_COLLECTION_READONLY, 0)
                                db.insert(TABLE_NAME_COLLECTION, SQLiteDatabase.CONFLICT_IGNORE, cv)
                                super.onCreate(db)
                            }

                            override fun onOpen(db: SupportSQLiteDatabase) {
                                /*
                                val cv = ContentValues()
                                cv.put(COLUMN_COLLECTION_ID, "1")
                                cv.put(COLUMN_COLLECTION_ACCOUNT_TYPE, ICalCollection.LOCAL_ACCOUNT_TYPE)
                                cv.put(COLUMN_COLLECTION_ACCOUNT_NAME, context.getString(R.string.default_local_account_name))
                                cv.put(COLUMN_COLLECTION_DISPLAYNAME, context.getString(R.string.default_local_collection_name))
                                cv.put(COLUMN_COLLECTION_URL, ICalCollection.LOCAL_COLLECTION_URL)
                                cv.put(COLUMN_COLLECTION_SUPPORTSVJOURNAL, 1)
                                cv.put(COLUMN_COLLECTION_SUPPORTSVEVENT, 1)
                                cv.put(COLUMN_COLLECTION_SUPPORTSVTODO, 1)
                                cv.put(COLUMN_COLLECTION_READONLY, 0)
                                db.insert(TABLE_NAME_COLLECTION, SQLiteDatabase.CONFLICT_IGNORE, cv)
                                super.onOpen(db)
                                 */
                            }
                        })
                        // see https://developer.android.com/topic/performance/sqlite-performance-best-practices#consider-without
                        .setJournalMode(JournalMode.AUTOMATIC)
                        .build()

                    // Assign INSTANCE to the newly created database.
                    INSTANCE = instance
                }
                // Return instance; smart cast to be non-null.
                return instance
            }
        }


        /**
         * Switches the internal implementation with an empty in-memory database.
         *
         * @param context The context.
         */
        @VisibleForTesting
        fun switchToInMemory(context: Context) {
            INSTANCE = getInMemoryDB(context)
        }

        /**
         * returns an empty in-memory database.
         * @param context The context.
         * @return ICalDatabase as an in-memory database
         */
        private fun getInMemoryDB(context: Context): ICalDatabase {

                return Room.inMemoryDatabaseBuilder(context, ICalDatabase::class.java)
                    .allowMainThreadQueries()
                    .build()
        }
    }
}