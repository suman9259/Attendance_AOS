package com.scharfesicht.attendencesystem.app.navigation

import androidx.core.splashscreen.SplashScreen
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.scharfesicht.attendencesystem.features.splash.DummyScreen

/**
 * Splash navigation graph containing all pre-authentication screens
 */
fun NavGraphBuilder.splashNavGraph(navManager: NavManager) {
    navigation(
        startDestination = ScreenRoutes.Splash.route,
        route = RoutesConst.SPLASH_GRAPH
    ) {

        composable(route = ScreenRoutes.Splash.route) {
            DummyScreen(navManager)
        }

    }
}