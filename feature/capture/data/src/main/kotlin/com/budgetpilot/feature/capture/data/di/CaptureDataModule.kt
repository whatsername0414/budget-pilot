package com.budgetpilot.feature.capture.data.di

import com.budgetpilot.core.data.connectivity.NetworkConnectivityObserver
import com.budgetpilot.feature.capture.data.FileReceiptImageStore
import com.budgetpilot.feature.capture.data.StubReceiptExtractor
import com.budgetpilot.feature.capture.domain.ConnectivityObserver
import com.budgetpilot.feature.capture.domain.ReceiptExtractor
import com.budgetpilot.feature.capture.domain.ReceiptImageStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Binds [ReceiptExtractor] straight to the [StubReceiptExtractor] rather than
 * through `ExtractionRouter` — the router's other dependencies (a cloud
 * extractor, `CloudAiPolicy`) don't exist until Phase 3/6, so there's nothing
 * to route between yet.
 */
val captureDataModule =
    module {
        single<ReceiptExtractor> { StubReceiptExtractor() }
        single<ReceiptImageStore> { FileReceiptImageStore(androidContext()) }
        single<ConnectivityObserver> {
            val observer = get<NetworkConnectivityObserver>()
            ConnectivityObserver { observer.isOnline() }
        }
    }
