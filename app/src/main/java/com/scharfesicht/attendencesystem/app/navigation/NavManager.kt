package com.scharfesicht.attendencesystem.app.navigation

import android.net.Uri
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Enhanced Navigation Manager Interface
 * Provides type-safe, observable, and production-ready navigation
 */
interface NavManager {

    // Navigation State
    val navigationEvents: Flow<NavigationEvent>
    val isNavigating: StateFlow<Boolean>
    val currentRoute: StateFlow<String?>

    // Lifecycle
    fun setNavController(controller: NavController)
    fun clearNavController()

    fun getParentEntry(
        currentEntry: NavBackStackEntry,
        parentRoute: String
    ) : NavBackStackEntry?

    // Basic Navigation
    fun navigate(route: String)
    fun navigateAndPopUpTo(route: String, popUpTo: String, inclusive: Boolean = true)
    fun navigateToRoot(route: String)
    fun navigateBack(): Boolean

    // Advanced Navigation
    fun navigateSingleTop(route: String)
    fun navigationForBottomBar(route: String)

    // Type-Safe Navigation with Arguments
    fun navigateWithArgs(route: String, args: Map<String, Any>)
    fun <T : Any> navigateWithTypedArgs(route: String, args: T)

    // Result Handling (Type-Safe)
    fun navigateForResult(route: String, resultKey: String)
    fun <T : Any> setResult(resultKey: String, result: T)
    fun <T : Any> getResult(resultKey: String): T?
    fun <T : Any> observeResult(resultKey: String): Flow<T?>
    fun clearResult(resultKey: String)

    // Navigation Guards
    fun addGuard(guard: NavigationGuard)
    fun removeGuard(guard: NavigationGuard)

    // Deep Linking
    fun handleDeepLink(uri: Uri): Boolean
    fun canHandleDeepLink(uri: Uri): Boolean

    // Back Stack Management
    fun getCurrentRoute(): String?
    fun canNavigateBack(): Boolean
    fun getBackStackCount(): Int
    fun clearBackStack()
    fun popBackStack(route: String, inclusive: Boolean = false): Boolean

    // Conditional Navigation
    fun navigateIfNotCurrent(route: String)
    fun navigateIfCondition(route: String, condition: () -> Boolean)

    // Analytics & Logging
    fun enableAnalytics(enabled: Boolean)
    fun setAnalyticsProvider(provider: NavigationAnalytics)
}

/**
 * Navigation Events for observing navigation state
 */
sealed class NavigationEvent {
    data class Navigated(val route: String, val timestamp: Long = System.currentTimeMillis()) : NavigationEvent()
    data class NavigationFailed(val route: String, val error: String) : NavigationEvent()
    data class BackPressed(val fromRoute: String?) : NavigationEvent()
    data class DeepLinkHandled(val uri: Uri) : NavigationEvent()
    object BackStackCleared : NavigationEvent()
}

/**
 * Navigation Guard for conditional navigation
 */
interface NavigationGuard {
    suspend fun canNavigate(from: String?, to: String): NavigationGuardResult
    val priority: Int get() = 0 // Higher priority executes first
}

/**
 * Result of navigation guard check
 */
sealed class NavigationGuardResult {
    object Allow : NavigationGuardResult()
    data class Deny(val reason: String) : NavigationGuardResult()
    data class Redirect(val alternativeRoute: String) : NavigationGuardResult()
}

/**
 * Analytics provider interface
 */
interface NavigationAnalytics {
    fun logScreenView(screenName: String, screenClass: String)
    fun logNavigationEvent(event: String, params: Map<String, Any>)
}

/**
 * Navigation options builder
 */
data class NavOptions(
    val popUpTo: String? = null,
    val inclusive: Boolean = true,
    val singleTop: Boolean = false,
    val restoreState: Boolean = false,
    val saveState: Boolean = true
)

/**
 * Extension function for building NavOptions
 */
inline fun navOptions(builder: NavOptionsBuilder.() -> Unit): NavOptions {
    return NavOptionsBuilder().apply(builder).build()
}

class NavOptionsBuilder {
    private var popUpTo: String? = null
    private var inclusive: Boolean = true
    private var singleTop: Boolean = false
    private var restoreState: Boolean = false
    private var saveState: Boolean = true

    fun popUpTo(route: String, inclusive: Boolean = true) {
        this.popUpTo = route
        this.inclusive = inclusive
    }

    fun launchSingleTop() {
        this.singleTop = true
    }

    fun restoreState(restore: Boolean = true) {
        this.restoreState = restore
    }

    fun saveState(save: Boolean = true) {
        this.saveState = save
    }

    fun build() = NavOptions(popUpTo, inclusive, singleTop, restoreState, saveState)
}