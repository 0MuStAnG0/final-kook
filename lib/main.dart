import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:provider/provider.dart';

import 'screens/main_shell.dart';
import 'screens/wallet_screen.dart';
import 'screens/inbox_screen.dart';
import 'screens/chat_screen.dart';
import 'screens/settings_screen.dart';
import 'screens/store_application_screen.dart';
import 'screens/auth_screen.dart';
import 'state/app_state.dart';
import 'theme/app_theme.dart';
import 'widgets/responsive_wrapper.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(
    ChangeNotifierProvider(
      create: (_) => AppStateManager(),
      child: const KookFlutterApp(),
    ),
  );
}

class KookFlutterApp extends StatelessWidget {
  const KookFlutterApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final state = Provider.of<AppStateManager>(context);

    return MaterialApp(
      title: 'کوک | شبکه اجتماعی و فروشگاه آنلاین',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      darkTheme: AppTheme.darkTheme,
      themeMode: state.isDarkMode ? ThemeMode.dark : ThemeMode.light,
      locale: const Locale('fa', 'IR'),
      supportedLocales: const [
        Locale('fa', 'IR'),
        Locale('en', 'US'),
      ],
      localizationsDelegates: const [
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      builder: (context, child) {
        return Directionality(
          textDirection: TextDirection.rtl,
          child: ResponsiveWrapper(child: child ?? const SizedBox()),
        );
      },
      initialRoute: '/',
      routes: {
        '/': (_) => const MainShell(),
        '/wallet': (_) => const WalletScreen(),
        '/inbox': (_) => const InboxScreen(),
        '/chat': (_) => const ChatScreen(),
        '/settings': (_) => const SettingsScreen(),
        '/store_apply': (_) => const StoreApplicationScreen(),
        '/auth': (_) => const AuthScreen(),
      },
    );
  }
}
