package com.scharfesicht.attendencesystem.app.navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultNavManager @Inject constructor() : NavManager {

    private var navController: NavController? = null

    // Reactive navigation states
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 10)
    override val navigationEvents: Flow<NavigationEvent> = _navigationEvents.asSharedFlow()

    private val _isNavigating = MutableStateFlow(false)
    override val isNavigating: StateFlow<Boolean> = _isNavigating.asStateFlow()

    private val _currentRoute = MutableStateFlow<String?>(null)
    override val currentRoute: StateFlow<String?> = _currentRoute.asStateFlow()

    private val guards = mutableListOf<NavigationGuard>()
    private var analyticsProvider: NavigationAnalytics? = null
    private var analyticsEnabled = false

    override fun setNavController(controller: NavController) {
        this.navController = controller
        // Observe route changes
        controller.addOnDestinationChangedListener { _, destination, _ ->
            _currentRoute.value = destination.route
            if (analyticsEnabled && destination.route != null) {
                analyticsProvider?.logScreenView(destination.route!!, destination.javaClass.simpleName)
            }
        }
    }

    override fun clearNavController() {
        navController = null
        _currentRoute.value = null
    }

    override fun getParentEntry(
        currentEntry: NavBackStackEntry,
        parentRoute: String
    ): NavBackStackEntry? {
        return try {
            navController?.getBackStackEntry(parentRoute)
        } catch (e: Exception) {
            Log.w("NavManager", "Parent entry not found for route: $parentRoute", e)
            null
        }
    }


    override fun navigate(route: String) {
        safeNavigate {
            navController?.navigate(route)
            _navigationEvents.tryEmit(NavigationEvent.Navigated(route))
        }
    }

    override fun navigateAndPopUpTo(route: String, popUpTo: String, inclusive: Boolean) {
        safeNavigate {
            navController?.navigate(route) {
                popUpTo(popUpTo) { this.inclusive = inclusive }
                launchSingleTop = true
            }
            _navigationEvents.tryEmit(NavigationEvent.Navigated(route))
        }
    }

    override fun navigateToRoot(route: String) {
        safeNavigate {
            navController?.navigate(route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            _navigationEvents.tryEmit(NavigationEvent.BackStackCleared)
        }
    }

    @SuppressLint("UnsafeImplicitIntentLaunch")
    override fun navigateBack(): Boolean {
        return safeNavigate {
            val controller = navController ?: return false
            val fromRoute = controller.currentBackStackEntry?.destination?.route
            if (controller.previousBackStackEntry != null) {
                controller.popBackStack()
            } else {
                val context = controller.context
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
            _navigationEvents.tryEmit(NavigationEvent.BackPressed(fromRoute))
            true
        } ?: false
    }

    override fun navigateSingleTop(route: String) {
        safeNavigate {
            navController?.navigate(route, androidx.navigation.navOptions {
                launchSingleTop = true
            })
            _navigationEvents.tryEmit(NavigationEvent.Navigated(route))
        }
    }

    override fun navigationForBottomBar(route: String) {
        safeNavigate {
            val current = navController?.currentBackStackEntry?.destination?.route
            if (current == route) return
            if (current == ScreenRoutes.Home.route) {
                navController?.navigate(route)
            } else {
                navController?.navigate(route) {
                    popUpTo(ScreenRoutes.Home.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
            _navigationEvents.tryEmit(NavigationEvent.Navigated(route))
        }
    }

    override fun <T : Any> navigateWithTypedArgs(route: String, args: T) {
        // Convert typed args to a query string using reflection
        val argsMap = args::class.members
            .filterIsInstance<kotlin.reflect.KProperty1<T, *>>()
            .associate { it.name to (it.get(args)?.toString() ?: "") }
        navigateWithArgs(route, argsMap)
    }

    override fun navigateWithArgs(route: String, args: Map<String, Any>) {
        safeNavigate {
            val query = args.entries.joinToString("&") { "${it.key}=${it.value}" }
            val fullRoute = if (query.isNotEmpty()) "$route?$query" else route
            navController?.navigate(fullRoute)
            _navigationEvents.tryEmit(NavigationEvent.Navigated(fullRoute))
        }
    }

    override fun navigateForResult(route: String, resultKey: String) {
        safeNavigate {
            navController?.currentBackStackEntry?.savedStateHandle?.remove<Any>(resultKey)
            navController?.navigate(route)
        }
    }

    override fun <T : Any> getResult(resultKey: String): T? {
        return navController?.previousBackStackEntry?.savedStateHandle?.get<T>(resultKey)
    }

    override fun <T : Any> observeResult(resultKey: String): Flow<T?> {
        return navController?.currentBackStackEntry?.savedStateHandle
            ?.getStateFlow<T?>(resultKey, null)
            ?: emptyFlow()
    }

    override fun clearResult(resultKey: String) {
        navController?.currentBackStackEntry?.savedStateHandle?.remove<Any>(resultKey)
    }

    override fun addGuard(guard: NavigationGuard) {
        guards.add(guard)
        guards.sortByDescending { it.priority }
    }

    override fun removeGuard(guard: NavigationGuard) {
        guards.remove(guard)
    }

    override fun handleDeepLink(uri: Uri): Boolean {
        return navController?.handleDeepLink(Intent(Intent.ACTION_VIEW, uri)) ?: false
    }

    override fun canHandleDeepLink(uri: Uri): Boolean {
        return try {
            navController?.graph?.hasDeepLink(uri) == true
        } catch (e: Exception) {
            false
        }
    }

    override fun <T : Any> setResult(resultKey: String, result: T) {
        safeNavigate {
            navController?.previousBackStackEntry
                ?.savedStateHandle
                ?.set(resultKey, result)
        }
    }

    override fun getCurrentRoute(): String? = _currentRoute.value

    override fun canNavigateBack(): Boolean {
        return navController?.previousBackStackEntry != null
    }

    @SuppressLint("RestrictedApi")
    override fun getBackStackCount(): Int {
        return try {
            navController?.currentBackStack?.value?.size ?: 0
        } catch (e: Exception) {
            Log.e("NavManager", "Error getting back stack count", e)
            0
        }
    }


    override fun clearBackStack() {
        safeNavigate {
            navController?.popBackStack(0, true)
            _navigationEvents.tryEmit(NavigationEvent.BackStackCleared)
        }
    }

    override fun popBackStack(route: String, inclusive: Boolean): Boolean {
        return navController?.popBackStack(route, inclusive) ?: false
    }

    override fun navigateIfNotCurrent(route: String) {
        if (getCurrentRoute() != route) navigate(route)
    }

    override fun navigateIfCondition(route: String, condition: () -> Boolean) {
        if (condition()) navigate(route)
    }

    override fun enableAnalytics(enabled: Boolean) {
        analyticsEnabled = enabled
    }

    override fun setAnalyticsProvider(provider: NavigationAnalytics) {
        analyticsProvider = provider
    }

    // ---------- Utility ----------
    private inline fun <T> safeNavigate(block: () -> T): T? {
        return try {
            _isNavigating.value = true
            val result = block()
            _isNavigating.value = false
            result
        } catch (e: Exception) {
            _isNavigating.value = false
            Log.e("NavManager", "Navigation failed", e)
            navController?.context?.let {
                Toast.makeText(it, "Navigation failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            _navigationEvents.tryEmit(NavigationEvent.NavigationFailed("Unknown", e.message ?: "Error"))
            null
        }
    }
}
