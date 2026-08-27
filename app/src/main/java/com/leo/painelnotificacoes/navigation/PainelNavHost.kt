package com.leo.painelnotificacoes.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.leo.painelnotificacoes.AppContainer
import com.leo.painelnotificacoes.ui.group.GroupDetailScreen
import com.leo.painelnotificacoes.ui.group.GroupDetailViewModel
import com.leo.painelnotificacoes.ui.home.HomeScreen
import com.leo.painelnotificacoes.ui.home.HomeViewModel
import com.leo.painelnotificacoes.ui.settings.SettingsScreen
import com.leo.painelnotificacoes.ui.settings.SettingsViewModel

private const val ARG_PACKAGE_NAME = "packageName"
private const val ARG_APP_NAME = "appName"
private const val ROUTE_HOME = "home"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_GROUP = "group/{$ARG_PACKAGE_NAME}/{$ARG_APP_NAME}"

@Composable
fun PainelNavHost(
    container: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = ROUTE_HOME) {
        composable(ROUTE_HOME) {
            val viewModel = viewModel<HomeViewModel>(
                factory = viewModelFactory {
                    initializer { HomeViewModel(container.notificationRepository) }
                },
            )
            HomeScreen(
                viewModel = viewModel,
                onGroupClick = { packageName, appName ->
                    navController.navigate("group/${Uri.encode(packageName)}/${Uri.encode(appName)}")
                },
                onSettingsClick = { navController.navigate(ROUTE_SETTINGS) },
            )
        }
        composable(
            route = ROUTE_GROUP,
            arguments = listOf(
                navArgument(ARG_PACKAGE_NAME) { type = NavType.StringType },
                navArgument(ARG_APP_NAME) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString(ARG_PACKAGE_NAME).orEmpty()
            val appName = backStackEntry.arguments?.getString(ARG_APP_NAME).orEmpty()
            val viewModel = viewModel<GroupDetailViewModel>(
                key = packageName,
                factory = viewModelFactory {
                    initializer {
                        GroupDetailViewModel(
                            packageName = packageName,
                            initialAppName = appName,
                            repository = container.notificationRepository,
                            settingsRepository = container.settingsRepository,
                            summarizationManager = container.summarizationManager,
                            geminiCloudSummarizer = container.geminiCloudSummarizer,
                        )
                    }
                },
            )
            GroupDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(ROUTE_SETTINGS) {
            val viewModel = viewModel<SettingsViewModel>(
                factory = viewModelFactory {
                    initializer { SettingsViewModel(container.settingsRepository) }
                },
            )
            SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
