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
    version = 41,
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

        private val MIGRATION_40_41 = object : Migration(40, 41) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Clean up orphaned icalobjects that reference non-existent collectionIds.
                // This is necessary because the subsequent table recreation logic will fail
                // if foreign key violations exist.
                db.execSQL("DELETE FROM $TABLE_NAME_ICALOBJECT WHERE $COLUMN_ICALOBJECT_COLLECTIONID NOT IN (SELECT $COLUMN_COLLECTION_ID FROM $TABLE_NAME_COLLECTION)")

                // 2. Perform the schema changes previously handled by AutoMigration 40->41:
                // Make `created` and `lastModified` columns nullable in `icalobject`.
                db.execSQL("DROP VIEW IF EXISTS `ical4list`")
                db.execSQL("DROP VIEW IF EXISTS `collectionsView`")

                db.execSQL("CREATE TABLE IF NOT EXISTS `_new_icalobject` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `module` TEXT NOT NULL, `component` TEXT NOT NULL, `summary` TEXT, `description` TEXT, `dtstart` INTEGER, `dtstarttimezone` TEXT, `dtend` INTEGER, `dtendtimezone` TEXT, `status` TEXT, `xstatus` TEXT, `classification` TEXT, `url` TEXT, `contact` TEXT, `geolat` REAL, `geolong` REAL, `location` TEXT, `locationaltrep` TEXT, `geofenceRadius` INTEGER, `percent` INTEGER, `priority` INTEGER, `due` INTEGER, `duetimezone` TEXT, `completed` INTEGER, `completedtimezone` TEXT, `duration` TEXT, `uid` TEXT NOT NULL, `created` INTEGER, `dtstamp` INTEGER NOT NULL, `lastmodified` INTEGER, `sequence` INTEGER, `rrule` TEXT, `exdate` TEXT, `rdate` TEXT, `recurid` TEXT, `recuridtimezone` TEXT, `rstatus` TEXT, `color` INTEGER, `collectionId` INTEGER NOT NULL, `dirty` INTEGER NOT NULL, `deleted` INTEGER NOT NULL, `filename` TEXT, `etag` TEXT, `scheduletag` TEXT, `flags` INTEGER, `subtasksExpanded` INTEGER, `subnotesExpanded` INTEGER, `parentsExpanded` INTEGER, `attachmentsExpanded` INTEGER, `sortIndex` INTEGER, `isAlarmNotificationActive` INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(`collectionId`) REFERENCES `collection`(`_id`) ON UPDATE NO ACTION ON DELETE CASCADE )")

                db.execSQL("INSERT INTO `_new_icalobject` (`_id`,`module`,`component`,`summary`,`description`,`dtstart`,`dtstarttimezone`,`dtend`,`dtendtimezone`,`status`,`xstatus`,`classification`,`url`,`contact`,`geolat`,`geolong`,`location`,`locationaltrep`,`geofenceRadius`,`percent`,`priority`,`due`,`duetimezone`,`completed`,`completedtimezone`,`duration`,`uid`,`created`,`dtstamp`,`lastmodified`,`sequence`,`rrule`,`exdate`,`rdate`,`recurid`,`recuridtimezone`,`rstatus`,`color`,`collectionId`,`dirty`,`deleted`,`filename`,`etag`,`scheduletag`,`flags`,`subtasksExpanded`,`subnotesExpanded`,`parentsExpanded`,`attachmentsExpanded`,`sortIndex`,`isAlarmNotificationActive`) SELECT `_id`,`module`,`component`,`summary`,`description`,`dtstart`,`dtstarttimezone`,`dtend`,`dtendtimezone`,`status`,`xstatus`,`classification`,`url`,`contact`,`geolat`,`geolong`,`location`,`locationaltrep`,`geofenceRadius`,`percent`,`priority`,`due`,`duetimezone`,`completed`,`completedtimezone`,`duration`,`uid`,`created`,`dtstamp`,`lastmodified`,`sequence`,`rrule`,`exdate`,`rdate`,`recurid`,`recuridtimezone`,`rstatus`,`color`,`collectionId`,`dirty`,`deleted`,`filename`,`etag`,`scheduletag`,`flags`,`subtasksExpanded`,`subnotesExpanded`,`parentsExpanded`,`attachmentsExpanded`,`sortIndex`,`isAlarmNotificationActive` FROM `icalobject`")

                db.execSQL("DROP TABLE `icalobject`")
                db.execSQL("ALTER TABLE `_new_icalobject` RENAME TO `icalobject`")

                db.execSQL("CREATE INDEX IF NOT EXISTS `index_icalobject__id` ON `icalobject` (`_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_icalobject_collectionId` ON `icalobject` (`collectionId`)")

                // Re-create views
                db.execSQL("CREATE VIEW `ical4list` AS SELECT DISTINCT main_icalobject._id, main_icalobject.module, main_icalobject.component, main_icalobject.summary, main_icalobject.description, main_icalobject.location, main_icalobject.geolat, main_icalobject.geolong, main_icalobject.url, main_icalobject.contact, main_icalobject.dtstart, main_icalobject.dtstarttimezone, main_icalobject.dtend, main_icalobject.dtendtimezone, main_icalobject.status, main_icalobject.xstatus, main_icalobject.classification, main_icalobject.percent, main_icalobject.priority, main_icalobject.due, main_icalobject.duetimezone, main_icalobject.completed, main_icalobject.completedtimezone, main_icalobject.duration, main_icalobject.created, main_icalobject.dtstamp, main_icalobject.lastmodified, main_icalobject.sequence, main_icalobject.uid, main_icalobject.rrule, main_icalobject.recurid, collection.color as colorCollection, main_icalobject.color as colorItem, main_icalobject.collectionId, collection.accountname, collection.displayname, main_icalobject.deleted, CASE WHEN collection.accounttype = 'LOCAL' THEN 0 WHEN main_icalobject.recurid IS NOT NULL THEN (SELECT series.dirty FROM icalobject series WHERE series.recurid IS NULL AND series.uid = main_icalobject.uid) ELSE main_icalobject.dirty END as uploadPending, CASE WHEN main_icalobject._id IN (SELECT sub_rel.icalObjectId FROM relatedto sub_rel INNER JOIN icalobject sub_ical on sub_rel.text = sub_ical.uid AND sub_ical.module = 'JOURNAL' AND sub_rel.reltype = 'PARENT') THEN 1 ELSE 0 END as isChildOfJournal, CASE WHEN main_icalobject._id IN (SELECT sub_rel.icalObjectId FROM relatedto sub_rel INNER JOIN icalobject sub_ical on sub_rel.text = sub_ical.uid AND sub_ical.module = 'NOTE' AND sub_rel.reltype = 'PARENT') THEN 1 ELSE 0 END as isChildOfNote, CASE WHEN main_icalobject._id IN (SELECT sub_rel.icalObjectId FROM relatedto sub_rel INNER JOIN icalobject sub_ical on sub_rel.text = sub_ical.uid AND sub_ical.module = 'TODO' AND sub_rel.reltype = 'PARENT') THEN 1 ELSE 0 END as isChildOfTodo, (SELECT group_concat(sub.text, '|||') FROM (SELECT * FROM category ORDER BY text) as sub WHERE main_icalobject._id = sub.icalObjectId) as categories, (SELECT group_concat(sub.text, '|||') FROM (SELECT * FROM resource ORDER BY text) as sub WHERE main_icalobject._id = sub.icalObjectId) as resources, (SELECT count(*) FROM icalobject sub_icalobject INNER JOIN relatedto sub_relatedto ON sub_icalobject._id = sub_relatedto.icalObjectId AND sub_icalobject.component = 'VTODO' AND sub_relatedto.text = main_icalobject.uid AND sub_relatedto.reltype = 'PARENT' AND sub_icalobject.deleted = 0 AND sub_icalobject.rrule IS NULL) as numSubtasks, (SELECT count(*) FROM icalobject sub_icalobject INNER JOIN relatedto sub_relatedto ON sub_icalobject._id = sub_relatedto.icalObjectId AND sub_icalobject.component = 'VJOURNAL' AND sub_relatedto.text = main_icalobject.uid AND sub_relatedto.reltype = 'PARENT' AND sub_icalobject.deleted = 0 AND sub_icalobject.rrule IS NULL) as numSubnotes, (SELECT count(*) FROM attachment WHERE icalObjectId = main_icalobject._id  ) as numAttachments, (SELECT count(*) FROM attendee WHERE icalObjectId = main_icalobject._id  ) as numAttendees, (SELECT count(*) FROM comment WHERE icalObjectId = main_icalobject._id  ) as numComments, (SELECT count(*) FROM relatedto WHERE icalObjectId = main_icalobject._id  ) as numRelatedTodos, (SELECT count(*) FROM resource WHERE icalObjectId = main_icalobject._id  ) as numResources, (SELECT count(*) FROM alarm WHERE icalObjectId = main_icalobject._id  ) as numAlarms, (SELECT uri FROM attachment WHERE icalObjectId = main_icalobject._id AND (fmttype LIKE 'audio/%' OR fmttype LIKE 'video/%') LIMIT 1 ) as audioAttachment, collection.readonly as isReadOnly, main_icalobject.subtasksExpanded, main_icalobject.subnotesExpanded, main_icalobject.parentsExpanded, main_icalobject.attachmentsExpanded, main_icalobject.sortIndex FROM icalobject main_icalobject INNER JOIN collection collection ON main_icalobject.collectionId = collection._id WHERE main_icalobject.deleted = 0 AND (main_icalobject.rrule IS NULL OR (main_icalobject.dtstart IS NULL AND main_icalobject.rrule IS NOT NULL))")
                db.execSQL("CREATE VIEW `collectionsView` AS SELECT _id, url, displayname, description, owner, ownerdisplayname, color, supportsVEVENT, supportsVTODO, supportsVJOURNAL, accountname, accounttype, syncversion, readonly, lastsync, (SELECT count(*) FROM icalobject WHERE icalobject.collectionId = collection._id AND module = 'JOURNAL' AND deleted = 0) as numJournals, (SELECT count(*) FROM icalobject WHERE icalobject.collectionId = collection._id AND module = 'NOTE' AND deleted = 0) as numNotes, (SELECT count(*) FROM icalobject WHERE icalobject.collectionId = collection._id AND module = 'TODO' AND deleted = 0) as numTodos FROM collection")
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
                        .addMigrations(MIGRATION_1_2, MIGRATION_12_13, MIGRATION_18_19, MIGRATION_30_31, MIGRATION_40_41)

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