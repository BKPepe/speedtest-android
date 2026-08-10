package org.librespeed.speedtest.engine

import android.content.Context
import com.fdossena.speedtest.core.Speedtest
import com.fdossena.speedtest.core.config.SpeedtestConfig
import com.fdossena.speedtest.core.config.TelemetryConfig
import com.fdossena.speedtest.core.serverSelector.TestPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume

class TestEngine(private val context: Context) {

    private var speedtest: Speedtest? = null

    data class Discovery(val servers: List<TestPoint>, val selected: TestPoint?)

    /**
     * Loads the server list (remote first, bundled fallback), pings all servers and picks the best one.
     * [onListLoaded] gets the full list before any pinging starts.
     */
    suspend fun discover(
        customServers: List<TestPoint>,
        onListLoaded: (List<TestPoint>) -> Unit = {}
    ): Discovery = withContext(Dispatchers.IO) {
        val st = newSpeedtest(telemetryEnabled = false)
        customServers.forEach { runCatching { st.addTestPoint(it) } }
        var loaded = false
        val data = readAsset("ServerList.json")?.trim()
        if (data != null) {
            loaded = if (data.startsWith("\"") || data.startsWith("'")) {
                st.loadServerList(data.substring(1, data.length - 1))
            } else {
                runCatching { st.addTestPoints(JSONArray(data)); true }.getOrDefault(false)
            }
        }
        if (!loaded) {
            readAsset("ServerListFallback.json")?.let {
                runCatching { st.addTestPoints(JSONArray(it)); loaded = true }
            }
        }
        if (!loaded && customServers.isEmpty()) throw IOException("Failed to load the server list")
        onListLoaded(st.testPoints.toList())
        val selected = suspendCancellableCoroutine { continuation ->
            st.selectServer(object : Speedtest.ServerSelectedHandler() {
                override fun onServerSelected(server: TestPoint?) {
                    continuation.resume(server)
                }
            })
            continuation.invokeOnCancellation { runCatching { st.abort() } }
        }
        Discovery(st.testPoints.toList(), selected)
    }

    /** Prepares a fresh test run against an already known server, without re-pinging everything. */
    fun prepare(servers: List<TestPoint>, selected: TestPoint, telemetryEnabled: Boolean, singleConnection: Boolean = false) {
        val st = newSpeedtest(telemetryEnabled, singleConnection)
        st.addTestPoints(servers.toTypedArray())
        st.setSelectedServer(selected)
        speedtest = st
    }

    fun start(handler: Speedtest.SpeedtestHandler) {
        speedtest?.start(handler)
    }

    fun abort() {
        runCatching { speedtest?.abort() }
    }

    private fun newSpeedtest(telemetryEnabled: Boolean, singleConnection: Boolean = false): Speedtest {
        val st = Speedtest()
        val configJson = runCatching { JSONObject(readAsset("SpeedtestConfig.json") ?: "{}") }.getOrDefault(JSONObject())
        if (singleConnection) {
            configJson.put("dl_parallelStreams", 1)
            configJson.put("ul_parallelStreams", 1)
        }
        runCatching { st.setSpeedtestConfig(SpeedtestConfig(configJson)) }
        val telemetryJson = if (telemetryEnabled) {
            JSONObject().put("telemetryLevel", TelemetryConfig.LEVEL_FULL)
        } else {
            readAsset("TelemetryConfig.json")?.let { runCatching { JSONObject(it) }.getOrNull() } ?: JSONObject()
        }
        runCatching { st.setTelemetryConfig(TelemetryConfig(telemetryJson)) }
        return st
    }

    private fun readAsset(name: String): String? = try {
        context.assets.open(name).bufferedReader().use { it.readText() }
    } catch (_: Exception) {
        null
    }

}
