package at.techbee.jtx.ui.presets

import at.techbee.jtx.BuildConfig
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredListSetting
import at.techbee.jtx.database.locals.StoredResource
import kotlinx.serialization.Serializable


@Serializable
data class PresetData(
    val jtxVersionName: String = BuildConfig.VERSION_NAME,
    val jtxVersionCode: Int = BuildConfig.VERSION_CODE,
    val storedCategories: List<StoredCategory>,
    val storedResources: List<StoredResource>,
    val storedStatuses: List<ExtendedStatus>,
    val storedListSettings: List<StoredListSetting>
)