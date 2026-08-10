package org.librespeed.speedtest.data

import android.os.Build
import org.librespeed.speedtest.BuildConfig

object ClientInfo {

    val client: String = "LibreSpeed Android ${BuildConfig.VERSION_NAME}"

    val userAgent: String =
        "LibreSpeed-Android/${BuildConfig.VERSION_NAME} (SDK ${Build.VERSION.SDK_INT}; Android ${Build.VERSION.RELEASE})"

}
