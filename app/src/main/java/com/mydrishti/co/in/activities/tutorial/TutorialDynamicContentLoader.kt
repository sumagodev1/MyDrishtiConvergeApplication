package com.mydrishti.co.`in`.activities.tutorial

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages dynamic content loading for tutorial system
 * Supports loading content from various sources with caching
 */
class TutorialDynamicContentLoader(private val context: Context) {
    
    private val contentCache = ConcurrentHashMap<String, CachedContent>()
    private val loadingJobs = ConcurrentHashMap<String, Job>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var cacheExpirationTime = 24 * 60 * 60 * 1000L // 24 hours
    private var networkTimeoutMs = 10000L // 10 seconds
    private var maxCacheSize = 50
    
    /**
     * Loads content asynchronously
     */
    suspend fun loadContentAsync(
        contentKey: String,
        source: ContentSource,
        fallbackToCache: Boolean = true
    ): ContentResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check if already loading
                loadingJobs[contentKey]?.let { job ->
                    if (job.isActive) {
                        job.join()
                        return@withContext getCachedContent(contentKey) ?: ContentResult.Error("Loading failed")
                    }
                }
                
                // Check cache first
                getCachedContent(contentKey)?.let { cached ->
                    if (!cached.isExpired()) {
                        if (TutorialConstants.DEBUG_MODE_ENABLED) {
                            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Content loaded from cache: $contentKey")
                        }
                        return@withContext ContentResult.Success(cached.content)
                    }
                }
                
                // Load from source
                val loadingJob = async {
                    when (source) {
                        ContentSource.ASSETS -> loadFromAssets(contentKey)
                        ContentSource.NETWORK -> loadFromNetwork(contentKey)
                        ContentSource.CACHE -> getCachedContent(contentKey)?.let { 
                            ContentResult.Success(it.content) 
                        } ?: ContentResult.Error("Not found in cache")
                    }
                }
                
                loadingJobs[contentKey] = loadingJob
                
                val result = loadingJob.await()
                loadingJobs.remove(contentKey)
                
                // Cache successful results
                if (result is ContentResult.Success) {
                    cacheContent(contentKey, result.content)
                }
                
                // Fallback to cache if loading failed
                if (result is ContentResult.Error && fallbackToCache) {
                    getCachedContent(contentKey)?.let { cached ->
                        if (TutorialConstants.DEBUG_MODE_ENABLED) {
                            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Falling back to cached content: $contentKey")
                        }
                        return@withContext ContentResult.Success(cached.content)
                    }
                }
                
                result
            } catch (e: Exception) {
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error loading content: $contentKey", e)
                }
                
                // Try fallback to cache
                if (fallbackToCache) {
                    getCachedContent(contentKey)?.let { cached ->
                        return@withContext ContentResult.Success(cached.content)
                    }
                }
                
                ContentResult.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Loads content from assets
     */
    private suspend fun loadFromAssets(contentKey: String): ContentResult {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "tutorial/$contentKey.json"
                val inputStream = context.assets.open(fileName)
                val content = inputStream.bufferedReader().use { it.readText() }
                
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.d(TutorialConstants.DEBUG_LOG_TAG, "Content loaded from assets: $contentKey")
                }
                
                ContentResult.Success(content)
            } catch (e: IOException) {
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.w(TutorialConstants.DEBUG_LOG_TAG, "Content not found in assets: $contentKey")
                }
                
                ContentResult.Error("Asset not found: $contentKey")
            }
        }
    }
    
    /**
     * Loads content from network
     */
    private suspend fun loadFromNetwork(contentKey: String): ContentResult {
        return withContext(Dispatchers.IO) {
            try {
                val url = buildNetworkUrl(contentKey)
                val connection = URL(url).openConnection() as HttpURLConnection
                
                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = networkTimeoutMs.toInt()
                    readTimeout = networkTimeoutMs.toInt()
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "MyDrishti-Tutorial/1.0")
                }
                
                val responseCode = connection.responseCode
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val content = connection.inputStream.bufferedReader().use { it.readText() }
                    
                    if (TutorialConstants.DEBUG_MODE_ENABLED) {
                        Log.d(TutorialConstants.DEBUG_LOG_TAG, "Content loaded from network: $contentKey")
                    }
                    
                    ContentResult.Success(content)
                } else {
                    if (TutorialConstants.DEBUG_MODE_ENABLED) {
                        Log.w(TutorialConstants.DEBUG_LOG_TAG, "Network error for $contentKey: $responseCode")
                    }
                    
                    ContentResult.Error("Network error: $responseCode")
                }
            } catch (e: Exception) {
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.e(TutorialConstants.DEBUG_LOG_TAG, "Network loading failed for $contentKey", e)
                }
                
                ContentResult.Error("Network error: ${e.message}")
            }
        }
    }
    
    /**
     * Builds network URL for content key
     */
    private fun buildNetworkUrl(contentKey: String): String {
        // This would be configured based on your server setup
        val baseUrl = "https://api.mydrishti.com/tutorial/content"
        return "$baseUrl/$contentKey.json"
    }
    
    /**
     * Caches content
     */
    private fun cacheContent(contentKey: String, content: String) {
        // Manage cache size
        if (contentCache.size >= maxCacheSize) {
            removeOldestCacheEntry()
        }
        
        val cachedContent = CachedContent(
            content = content,
            timestamp = System.currentTimeMillis(),
            expirationTime = cacheExpirationTime
        )
        
        contentCache[contentKey] = cachedContent
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Content cached: $contentKey")
        }
    }
    
    /**
     * Gets cached content
     */
    private fun getCachedContent(contentKey: String): CachedContent? {
        return contentCache[contentKey]
    }
    
    /**
     * Removes oldest cache entry
     */
    private fun removeOldestCacheEntry() {
        val oldestEntry = contentCache.entries.minByOrNull { it.value.timestamp }
        oldestEntry?.let { entry ->
            contentCache.remove(entry.key)
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Removed oldest cache entry: ${entry.key}")
            }
        }
    }
    
    /**
     * Preloads content for better performance
     */
    fun preloadContent(contentKeys: List<String>, source: ContentSource = ContentSource.ASSETS) {
        coroutineScope.launch {
            contentKeys.forEach { contentKey ->
                try {
                    loadContentAsync(contentKey, source, fallbackToCache = false)
                } catch (e: Exception) {
                    if (TutorialConstants.DEBUG_MODE_ENABLED) {
                        Log.w(TutorialConstants.DEBUG_LOG_TAG, "Preload failed for $contentKey", e)
                    }
                }
            }
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Preloading completed for ${contentKeys.size} items")
            }
        }
    }
    
    /**
     * Loads localized content
     */
    suspend fun loadLocalizedContent(
        contentKey: String,
        locale: String,
        source: ContentSource = ContentSource.ASSETS
    ): ContentResult {
        val localizedKey = "${contentKey}_$locale"
        
        // Try localized version first
        val localizedResult = loadContentAsync(localizedKey, source, fallbackToCache = false)
        
        if (localizedResult is ContentResult.Success) {
            return localizedResult
        }
        
        // Fallback to default language
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Falling back to default language for: $contentKey")
        }
        
        return loadContentAsync(contentKey, source, fallbackToCache = true)
    }
    
    /**
     * Parses JSON content into tutorial steps
     */
    fun parseContentAsSteps(content: String): List<TutorialStep> {
        val steps = mutableListOf<TutorialStep>()
        
        try {
            val jsonObject = JSONObject(content)
            
            if (jsonObject.has("steps")) {
                val stepsArray = jsonObject.getJSONArray("steps")
                
                for (i in 0 until stepsArray.length()) {
                    val stepJson = stepsArray.getJSONObject(i)
                    val step = parseStepFromJson(stepJson)
                    
                    if (step != null) {
                        steps.add(step)
                    }
                }
            }
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Parsed ${steps.size} steps from content")
            }
            
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error parsing content as steps", e)
            }
        }
        
        return steps
    }
    
    /**
     * Parses a single step from JSON
     */
    private fun parseStepFromJson(stepJson: JSONObject): TutorialStep? {
        return try {
            TutorialStep(
                id = stepJson.getString("id"),
                title = stepJson.getString("title"),
                description = stepJson.getString("description"),
                targetViewId = stepJson.optString("targetViewId", null),
                animationType = AnimationType.valueOf(
                    stepJson.optString("animationType", "FADE_SCALE")
                ),
                duration = stepJson.optLong("duration", 3000L),
                explanatoryText = stepJson.optString("explanatoryText", null),
                isSkippable = stepJson.optBoolean("isSkippable", true),
                validationRequired = stepJson.optBoolean("validationRequired", false),
                order = stepJson.optInt("order", Int.MAX_VALUE)
            )
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error parsing step from JSON", e)
            }
            null
        }
    }
    
    /**
     * Parses content as localized strings
     */
    fun parseContentAsStrings(content: String): Map<String, String> {
        val strings = mutableMapOf<String, String>()
        
        try {
            val jsonObject = JSONObject(content)
            
            if (jsonObject.has("strings")) {
                val stringsObject = jsonObject.getJSONObject("strings")
                val keys = stringsObject.keys()
                
                while (keys.hasNext()) {
                    val key = keys.next()
                    strings[key] = stringsObject.getString(key)
                }
            }
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Parsed ${strings.size} strings from content")
            }
            
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error parsing content as strings", e)
            }
        }
        
        return strings
    }
    
    /**
     * Clears cache
     */
    fun clearCache() {
        contentCache.clear()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Content cache cleared")
        }
    }
    
    /**
     * Clears expired cache entries
     */
    fun clearExpiredCache() {
        val expiredKeys = contentCache.entries
            .filter { it.value.isExpired() }
            .map { it.key }
        
        expiredKeys.forEach { key ->
            contentCache.remove(key)
        }
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Cleared ${expiredKeys.size} expired cache entries")
        }
    }
    
    /**
     * Gets cache statistics
     */
    fun getCacheStatistics(): Map<String, Any> {
        val totalEntries = contentCache.size
        val expiredEntries = contentCache.values.count { it.isExpired() }
        val validEntries = totalEntries - expiredEntries
        
        return mapOf(
            "total_entries" to totalEntries,
            "valid_entries" to validEntries,
            "expired_entries" to expiredEntries,
            "cache_hit_ratio" to if (totalEntries > 0) validEntries.toDouble() / totalEntries else 0.0,
            "max_cache_size" to maxCacheSize,
            "cache_expiration_time_ms" to cacheExpirationTime
        )
    }
    
    /**
     * Configures cache settings
     */
    fun configureCacheSettings(
        maxSize: Int = maxCacheSize,
        expirationTimeMs: Long = cacheExpirationTime,
        networkTimeoutMs: Long = this.networkTimeoutMs
    ) {
        this.maxCacheSize = maxSize
        this.cacheExpirationTime = expirationTimeMs
        this.networkTimeoutMs = networkTimeoutMs
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Cache settings configured: maxSize=$maxSize, expiration=${expirationTimeMs}ms")
        }
    }
    
    /**
     * Cancels all loading operations
     */
    fun cancelAllLoading() {
        loadingJobs.values.forEach { job ->
            job.cancel()
        }
        loadingJobs.clear()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "All loading operations cancelled")
        }
    }
    
    /**
     * Cleanup method
     */
    fun cleanup() {
        cancelAllLoading()
        coroutineScope.cancel()
        contentCache.clear()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Dynamic content loader cleaned up")
        }
    }
}

/**
 * Represents cached content with expiration
 */
data class CachedContent(
    val content: String,
    val timestamp: Long,
    val expirationTime: Long
) {
    fun isExpired(): Boolean {
        return System.currentTimeMillis() - timestamp > expirationTime
    }
}

/**
 * Result of content loading operation
 */
sealed class ContentResult {
    data class Success(val content: String) : ContentResult()
    data class Error(val message: String) : ContentResult()
}