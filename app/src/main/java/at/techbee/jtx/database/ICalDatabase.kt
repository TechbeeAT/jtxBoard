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
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.database.views.ICal4ViewNote


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
        Attachment::class],
    views = [
        ICal4List::class,
        ICal4ViewNote::class,   // delete next time!
        CollectionsView::class],
    version = 13,
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
        AutoMigration (from = 12, to = 13),  // view updates
    ]
)
//@TypeConverters(Converters::class)
abstract class ICalDatabase : RoomDatabase() {

    /**
     * Connects the database to the DAO.
     */
    abstract val iCalDatabaseDao: ICalDatabaseDao


    @DeleteColumn(tableName = TABLE_NAME_ALARM, columnName = "trigger")
    class AutoMigration2to3: AutoMigrationSpec

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
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE `contact`")
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
                        .addMigrations(MIGRATION_1_2)

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
         *
         * @param context The context.
         * @return ICalDatabase as an in-memory database
         */
        @VisibleForTesting
        fun getInMemoryDB(context: Context): ICalDatabase {

                return Room.inMemoryDatabaseBuilder(context, ICalDatabase::class.java)
                    .allowMainThreadQueries()
                    .build()
        }
    }
}