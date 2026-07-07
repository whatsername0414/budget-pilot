package com.budgetpilot.feature.capture.data.di

import com.budgetpilot.core.data.connectivity.NetworkConnectivityObserver
import com.budgetpilot.core.data.preferences.UserPreferences
import com.budgetpilot.feature.capture.data.FileReceiptImageStore
import com.budgetpilot.feature.capture.data.MlKitOcrLineRecognizer
import com.budgetpilot.feature.capture.data.MlKitReceiptExtractor
import com.budgetpilot.feature.capture.data.OcrLineRecognizer
import com.budgetpilot.feature.capture.data.RoomExtractionCache
import com.budgetpilot.feature.capture.data.VisionLlmExtractor
import com.budgetpilot.feature.capture.domain.CloudAiPolicy
import com.budgetpilot.feature.capture.domain.ConnectivityObserver
import com.budgetpilot.feature.capture.domain.ExtractionCache
import com.budgetpilot.feature.capture.domain.ExtractionRouter
import com.budgetpilot.feature.capture.domain.PhReceiptParser
import com.budgetpilot.feature.capture.domain.ReceiptExtractor
import com.budgetpilot.feature.capture.domain.ReceiptImageStore
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val CLOUD_EXTRACTOR = named("cloudExtractor")
private val ON_DEVICE_EXTRACTOR = named("onDeviceExtractor")

/**
 * `CloudAiPolicy` SAM-adapts `:core:data`'s DataStore-backed `UserPreferences` (P3.6) the same
 * way `ConnectivityObserver` below adapts `NetworkConnectivityObserver` — `:core:data` must not
 * depend on this feature's domain module, so the adaptation lives here instead. Privacy mode
 * ON (`isCloudAiAllowedSnapshot() == false`) already routes to on-device unconditionally via
 * [ExtractionRouter]'s existing `useCloud` check (P6.1: no router change needed).
 */
val captureDataModule =
    module {
        single<ExtractionCache> { RoomExtractionCache(get()) }
        single<ReceiptExtractor>(CLOUD_EXTRACTOR) {
            VisionLlmExtractor(llmClient = get(), promptRepository = get(), extractionCache = get())
        }
        single { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
        single<OcrLineRecognizer> { MlKitOcrLineRecognizer(get()) }
        single { PhReceiptParser(clock = get()) }
        single<ReceiptExtractor>(ON_DEVICE_EXTRACTOR) { MlKitReceiptExtractor(recognizer = get(), parser = get()) }
        single<CloudAiPolicy> {
            val preferences = get<UserPreferences>()
            CloudAiPolicy { preferences.isCloudAiAllowedSnapshot() }
        }
        single<ReceiptExtractor> {
            ExtractionRouter(
                cloud = get(CLOUD_EXTRACTOR),
                onDevice = get(ON_DEVICE_EXTRACTOR),
                connectivity = get(),
                preferences = get(),
            )
        }
        single<ReceiptImageStore> { FileReceiptImageStore(androidContext()) }
        single<ConnectivityObserver> {
            val observer = get<NetworkConnectivityObserver>()
            ConnectivityObserver { observer.isOnline() }
        }
    }
